package fr.ludos.games.manhunt;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
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
import fr.ludos.core.area.Area;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

		Scoreboard scoreboard = game().getScoreboard();

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
	}

	@Override
	protected void onStart() {
		super.onStart();

		Set<Player> finalHunters = game().getGroup().getOnlinePlayers();
		if (selectedPlayers != null && ! selectedPlayers.isEmpty()) {
			finalHunters = finalHunters.stream()
				.filter(p -> selectedPlayers.contains(p))
				.collect(Collectors.toSet());

			if (finalHunters.isEmpty()) {
				throw new IllegalArgumentException("No players available (Check if the configured players are online)");
			}
		}

		if (selectedPrey == null) {
			prey = finalHunters.iterator().next();
		}
		else if (! selectedPrey.isOnline()) {
			throw new IllegalArgumentException("Configured Prey is not online");
		}

		finalHunters.remove(prey);


		joinPrey(prey);

		for (Player player : game().getGroup().getOnlinePlayers()) {
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
		if (game().getWorldManager().isLobbyStarted()) return;

		Player player = event.getPlayer();
		if (! game().getGroup().isPlayer(player)) return;

		if (! preyTeam.hasEntry(player.getName())) {
			Utility.onDeathSpectate(event, 5.0f, getPlugin(), () -> {
				for (SpecialItem.Events<?> item : game().getActiveItems()) {
					item.refreshPlayerInventory(player);
				}
			});
			return;
		}
		Utility.onDeathSpectate(event, getPlugin());

		Bukkit.getServer().broadcast(Component.text("Prey " + player.getName() + " Slain!")); // TODO: Translate
		preyTeam.removeEntry(player.getName());

		if (preyTeam.getSize() == 0) {
			Bukkit.getServer().sendMessage(Component.text("All Prey Dead! End of Game!")); // TODO: Translate
			game().scheduleEndGame(5);
		}


		ConfigurationSection preyData = game().ludos().getGameData(player, game().builder());
		ManhuntTimer timer = ((ManhuntGame)game()).timer;
		Duration newRecord = timer.getDuration();
		String newRecordString = timer.formatDuration(newRecord);
		Duration oldRecord = ManhuntGame.SURVIVAL_TIME.get(preyData);
		String oldRecordString = oldRecord != null
			? timer.formatDuration(oldRecord)
			: null;

		if (oldRecord == null || newRecord.compareTo(oldRecord) > 0) {
			ManhuntGame.SURVIVAL_TIME.set(newRecord, preyData);
			game().ludos().savePlayersConfig();
		}

		Component timeMessage =
			Component.text("You survived ")
				.append(Component.text(newRecordString).color(NamedTextColor.GOLD))
				.append(Component.text("!"));
		if (oldRecordString != null) {
			timeMessage = timeMessage
				.append(Component.text(" Previous Best : "))
				.append(Component.text(oldRecordString).color(NamedTextColor.GOLD));
		}

		player.sendMessage(timeMessage.color(NamedTextColor.GREEN));
	}


	@Override
	public void joinPlayer(OfflinePlayer player) {
		if (preyTeam.hasPlayer(player)) return;
		joinHunter(player);
	}

	private void joinAnyPlayer(Player player, Location location) {
		player.setScoreboard(game().getScoreboard());

		Utility.resetPlayer(player);

		player.teleport(location);
		player.setBedSpawnLocation(location, true);
		player.setGameMode(GameMode.SURVIVAL);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
	}

	public void joinPrey(OfflinePlayer player) {
		Area area = game().getWorldManager().getArea();
		Location gameLocation = area != null
			? Utility.snapToHighestY(area.pickRandom(0.0, 0.2), true)
			: game().getWorldManager().getWorld().getSpawnLocation();

		preyTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) {
			throw new IllegalArgumentException("Prey offline : " + player.getName());
		}

		joinAnyPlayer(onlinePlayer, gameLocation);

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

		Location hunterLocation = Utility.snapToHighestY(getLocationAroundTeammate(hunterTeam), true);

		hunterTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer, hunterLocation);

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

		Area area = game().getWorldManager().getArea();
		Location gameLocation = area != null
			? Utility.snapToHighestY(area.pickRandom(0.0, 1.0), true)
			: game().getWorldManager().getWorld().getSpawnLocation();

		spectatorTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(gameLocation);
		onlinePlayer.setBedSpawnLocation(gameLocation, true);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		onlinePlayer.setScoreboard(game().getScoreboard());

		Optional<Player> prey = getTeamOnlinePlayers(preyTeam).stream().findFirst();
		if (prey.isPresent()) {
			onlinePlayer.teleport(prey.get().getLocation());
		}
	}


	@Override
	public void discardPlayer(OfflinePlayer player) {
		hunterTeam.removeEntry(player.getName());
		preyTeam.removeEntry(player.getName());

		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		hunterTeam.removeEntry(player.getName());
		preyTeam.removeEntry(player.getName());
		spectatorTeam.removeEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			Utility.resetPlayer(onlinePlayer);
			onlinePlayer.teleport(game().getWorldManager().getReturnLocation());
		}
	}

	// @Override
	// public void updatePlayerTeam(OfflinePlayer player) {
	// 	String newPlayer = player.getName();
	// 	if (! huntersBound && ! preyTeam.hasEntry(newPlayer) && ! hunterTeam.hasEntry(newPlayer)) {
	// 		for (String hunterName : hunterTeam.getEntries()) {
	// 			var teammate = Bukkit.getPlayerExact(hunterName);
	// 			if (teammate == null) continue;

	// 			player.setBedSpawnLocation(teammate.getLocation());
	// 			player.teleport(teammate.getLocation());
	// 		}
	// 		hunterTeam.addEntry(newPlayer);
	// 	}
	// }
}