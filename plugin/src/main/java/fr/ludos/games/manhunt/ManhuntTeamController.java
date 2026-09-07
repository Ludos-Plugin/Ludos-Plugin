package fr.ludos.games.manhunt;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.core.Utility;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

/**
 * Controller for {@link ManhuntGame} teams, handling team selection and setup.
 */
public final class ManhuntTeamController extends GameTeamController {
	public Team hunterTeam;
	public Team preyTeam;
	public Team spectatorTeam;

	private final Set<OfflinePlayer> selectedPlayers;
	private final OfflinePlayer selectedPrey;

	private OfflinePlayer prey;
	public OfflinePlayer getPrey() {
		return prey;
	}

	public ManhuntTeamController(ManhuntGame game, @Nullable Set<OfflinePlayer> players, @Nullable OfflinePlayer prey) {
		super(game);

		this.selectedPlayers = players;
		this.selectedPrey = prey;
	}

	@Override
	protected void onStart() {
		super.onStart();

		Scoreboard scoreboard = game().scoreboard();

		hunterTeam = scoreboard.getTeam("Hunters");
		if (hunterTeam == null) {
			hunterTeam = scoreboard.registerNewTeam("Hunters");
			hunterTeam.color(NamedTextColor.RED);
			hunterTeam.setAllowFriendlyFire(false);
		}

		preyTeam = scoreboard.getTeam("Prey");
		if (preyTeam == null) {
			preyTeam = scoreboard.registerNewTeam("Prey");
			preyTeam.color(NamedTextColor.BLUE);
			preyTeam.setAllowFriendlyFire(false);
		}

		spectatorTeam = scoreboard.getTeam("Spectators");
		if (spectatorTeam == null) {
			spectatorTeam = scoreboard.registerNewTeam("Spectators");
			spectatorTeam.color(NamedTextColor.GRAY);
			spectatorTeam.setAllowFriendlyFire(false);
		}


		Set<Player> finalHunters = game().group().getOnlinePlayers();
		if (selectedPlayers != null && ! selectedPlayers.isEmpty()) {
			finalHunters = finalHunters.stream()
				.filter(p -> selectedPlayers.contains(p))
				.collect(Collectors.toSet());

			if (finalHunters.isEmpty()) {
				throw new IllegalArgumentException("No players available (Check if the configured players are online)");
			}
		}

		prey = selectedPrey;
		if (prey == null) {
			prey = finalHunters.iterator().next();
		}
		else if (! prey.isOnline()) {
			throw new IllegalArgumentException("Configured Prey is not online");
		}

		finalHunters.remove(prey);


		joinPrey(prey);

		for (Player player : game().group().getOnlinePlayers()) {
			if (player == prey) continue;
			if (finalHunters.contains(player)) {
				joinHunter(player);
			} else {
				joinSpectator(player);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		getTeamAlivePlayersStream(preyTeam)
			.forEach(this::saveSurvivalRecord);

		if (preyTeam != null) {
			preyTeam.unregister();
			preyTeam = null;
		}

		if (hunterTeam != null) {
			hunterTeam.unregister();
			hunterTeam = null;
		}
	}

	@Override
	public Collection<Team> getTeams() {
		return Set.of(hunterTeam, preyTeam);
	}

	private void saveSurvivalRecord(Player player) {
		ConfigurationSection preyData = game().ludos().getGameData(player, game().builder());
		ManhuntTimer timer = ((ManhuntGame)game()).timer;
		Duration newRecord = timer.getDuration();
		String newRecordString = timer.formatDuration(newRecord);
		Duration oldRecord = ManhuntGame.SURVIVAL_TIME.getOrDefault(preyData);
		String oldRecordString = oldRecord != null
			? timer.formatDuration(oldRecord)
			: null;

		if (oldRecord == null || newRecord.compareTo(oldRecord) > 0) {
			ManhuntGame.SURVIVAL_TIME.set(newRecord, preyData);
			game().ludos().savePlayersConfig();

			player.sendMessage(Component.text("New Record!")
				.color(NamedTextColor.GOLD)
				.decorate(TextDecoration.BOLD)
			);
		}

		Component timeMessage =
			Component.text("You survived ")
				.append(Component.text(newRecordString).color(NamedTextColor.GOLD))
				.append(Component.text('!'));
		if (oldRecordString != null) {
			timeMessage = timeMessage
				.append(Component.text(" Previous Best : "))
				.append(Component.text(oldRecordString).color(NamedTextColor.BLUE));
		}

		player.sendMessage(timeMessage.color(NamedTextColor.GREEN));
	}


	private String compilePreyNames() {
		List<String> preys = getTeamPlayers(preyTeam).stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toList());
		boolean isLong = preys.size() > 3;

		StringBuilder builder = new StringBuilder();

		if (isLong) {
			builder.append(preys.get(0));
			builder.append(", ");
			builder.append(preys.get(1));
			builder.append(", ");
			builder.append(preys.get(2));
			builder.append("...");
		}
		else {
			builder.append(String.join(", ", preys));
		}

		return builder.toString();
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (game().worldManager().isLobbyStarted()) return;

		Player player = event.getPlayer();
		if (! game().group().isPlayer(player)) return;

		if (! preyTeam.hasEntry(player.getName())) {
			Utility.onDeathSpectate(event, 5.0f, plugin(), () -> {
				for (SpecialItem.Events<?> item : game().getActiveItems()) {
					item.refreshPlayerInventory(player);
				}
			});
			return;
		}
		Utility.onDeathSpectate(event, plugin());

		game().group().sendMessage(Component.text("Prey " + player.getName() + " Slain!")); // TODO: Translate
		preyTeam.removeEntry(player.getName());

		if (preyTeam.getSize() == 0) {
			game().group().sendMessage(Component.text("All Prey Dead! End of Game!")); // TODO: Translate
			game().scheduleEndGame(5);
		}

		saveSurvivalRecord(player);
	}


	@Override
	public void joinPlayer(OfflinePlayer player) {
		if (preyTeam.hasPlayer(player)) return;
		joinHunter(player);
	}

	public void joinPrey(OfflinePlayer player) {
		preyTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) {
			throw new IllegalArgumentException("Prey offline : " + player.getName());
		}

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SURVIVAL);

		placePrey(onlinePlayer);

		onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1, false, false));
		onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 40, 0, false, true));


		onlinePlayer.showTitle(Title.title(
			Component.text("You are the ")
				.append(Component.text("Prey")
					.color(NamedTextColor.BLUE)
				),
			Component.text("Run for your life"),
			Title.Times.times(
				Duration.ofMillis(500),
				Duration.ofMillis(3500),
				Duration.ofMillis(1000)
			)
		));
	}

	public void joinHunter(OfflinePlayer player) {
		if (hunterTeam.hasPlayer(player) || preyTeam.hasPlayer(player)) return;

		hunterTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SURVIVAL);

		placeHunter(onlinePlayer);

		onlinePlayer.showTitle(Title.title(
			Component.text("You are a ")
			.append(Component.text("Hunter")
				.color(NamedTextColor.RED)),
			Component.text("Go and seek ")
			.append(Component.text(compilePreyNames())
				.color(NamedTextColor.BLUE)),
			Title.Times.times(
				Duration.ofMillis(500),
				Duration.ofMillis(3500),
				Duration.ofMillis(1000)
			)
		));
	}

	public void joinSpectator(OfflinePlayer player) {
		if (hunterTeam.hasPlayer(player) || preyTeam.hasPlayer(player)) return;

		spectatorTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		placeSpectator(onlinePlayer);
	}

	private void joinAnyPlayer(Player player) {
		player.setScoreboard(game().scoreboard());

		Utility.resetPlayer(player);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
	}

	@Override
	public void discardPlayer(OfflinePlayer player) {
		hunterTeam.removePlayer(player);
		preyTeam.removePlayer(player);

		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		hunterTeam.removePlayer(player);
		preyTeam.removePlayer(player);
		spectatorTeam.removePlayer(player);
	}

	@Override
	public void placePlayer(OfflinePlayer player) {
		if (hunterTeam.hasPlayer(player)) {
			placeHunter(player.getPlayer());
		}
		else if (preyTeam.hasPlayer(player)) {
			placePrey(player.getPlayer());
		}
		else {
			placeSpectator(player.getPlayer());
		}
	}

	public void placePrey(Player player) {
		Location preyLocation = Utility.snapToHighestY(
			game().worldManager().getArea()
				.pickRandom(0.4, 0.8),
			true
		);

		player.teleport(preyLocation);
		player.setBedSpawnLocation(preyLocation, true);
	}

	public void placeHunter(Player player) {
		Location hunterLocation = Utility.snapToHighestY(
			getLocationAroundTeammate(
				hunterTeam,
				(area) -> area.pickRandom(0, 0.3)
			),
			true
		);

		player.teleport(hunterLocation);
		player.setBedSpawnLocation(hunterLocation, true);
	}

	public void placeSpectator(Player player) {
		Optional<Player> prey = getTeamOnlinePlayers(preyTeam).stream().findFirst();
		if (prey.isPresent()) {
			player.teleport(prey.get().getLocation());
		}
	}
}