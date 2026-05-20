package fr.ludos.game.manhunt;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.Utility;
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.teamController.GameTeamController;
import fr.ludos.item.SpecialItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;


public final class ManhuntTeamController extends GameTeamController {
	public Team hunterTeam;
	public Team preyTeam;
	public Team spectatorTeam;

	private final OfflinePlayer selectedPrey;
	public OfflinePlayer getSelectedPrey() {
		return selectedPrey;
	}


	public ManhuntTeamController(ManhuntGame game, @Nullable Set<OfflinePlayer> players, @Nullable OfflinePlayer prey) {
		super(game, GroupConfigs.getGameJoinOption(game.getGroup().getConfig()));

		Set<Player> finalPlayers = game.getGroup().getOnlinePlayers();
		if (players != null) {
			finalPlayers = finalPlayers.stream()
				.filter(p -> players.contains(p))
				.collect(Collectors.toSet());

			if (finalPlayers.isEmpty()) {
				throw new IllegalArgumentException("No players available (Check if the configured players are online)");
			}
		}

		if (prey == null) {
			OfflinePlayer[] playersArray = finalPlayers.toArray(new OfflinePlayer[finalPlayers.size()]);
			prey = playersArray[ new Random().nextInt(playersArray.length) ];
		}
		else if (!prey.isOnline()) {
			throw new IllegalArgumentException("Configured Prey is not online");
		}

		finalPlayers.remove(prey);

		this.selectedPrey = prey;
	}

	@Override
	protected void onSetup() {
		Scoreboard scoreboard = getGame().getScoreboard();

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
		joinPrey(selectedPrey);

		Set<Player> hunters = getGame().getGroup().getOnlinePlayers();
		hunters.remove(selectedPrey);

		for (OfflinePlayer hunter : hunters) {
			if (hunter == null) continue;

			Player onlineHunter = hunter.getPlayer();
			if (onlineHunter == null) continue;

			joinHunter(onlineHunter);
		}
	}

	@Override
	protected void onSetdown() {
		if (preyTeam != null) {
			preyTeam.unregister();
			preyTeam = null;
		}

		if (hunterTeam != null) {
			hunterTeam.unregister();
			hunterTeam = null;
		}
	}


	public Set<OfflinePlayer> getHunters() {
		if (hunterTeam == null) return Set.of();
		return hunterTeam.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.collect(Collectors.toSet());
	}
	public Set<Player> getOnlineHunters() {
		if (hunterTeam == null) return Set.of();
		return hunterTeam.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.map(OfflinePlayer::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		}
	public Set<OfflinePlayer> getPrey() {
		if (preyTeam == null) return Set.of();
		return preyTeam.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.collect(Collectors.toSet());
	}
	public Set<Player> getOnlinePrey() {
		if (preyTeam == null) return Set.of();
		return preyTeam.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.map(OfflinePlayer::getPlayer)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}


	@Override
	public Collection<Team> getTeams() {
		return List.of(hunterTeam, preyTeam);
	}
	@Override
	public Collection<LivingEntity> getEntities() {
		Set<LivingEntity> entities = new HashSet<>();

		entities.addAll(getOnlinePrey());
		entities.addAll(getOnlineHunters());

		return entities;
	}

	private String compilePreyNames() {
		List<String> preys = getPrey().stream()
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
		Player player = event.getPlayer();
		if (! getGame().getGroup().isPlayer(player)) return;


		if (! isStarted()) {
			event.setCancelled(true);
			event.getPlayer().setHealth(20);
		}

		if (! preyTeam.hasEntry(player.getName())) {
			Utility.onDeathSpectate(event, 5, getPlugin(), () -> {
				for (SpecialItem.Events<?> item : getGame().getActiveItems()) {
					item.updateItemInInventory(player);
				}
			});
			return;
		}

		Bukkit.getServer().broadcast(Component.text("Prey " + player.getName() + " Slain!")); // TODO: Translate
		preyTeam.removeEntry(player.getName());

		if (preyTeam.getSize() == 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().sendMessage(Component.text("All Prey Dead! End of Game!")); // TODO: Translate
					getGame().stop();
				}
			}.runTaskLater(getPlugin(), 0);
		}
	}


	@Override
	public void joinPlayer(OfflinePlayer player) {
		if (preyTeam.hasPlayer(player)) return;
		joinHunter(player);
	}

	private void joinAnyPlayer(Player player, Location location) {
		player.teleport(location);
		player.setBedSpawnLocation(location, true);
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
	}

	public void joinPrey(OfflinePlayer player) {
		Location gameLocation = getGame().getAreaController().pickRandom(0.0, 0.2);

		preyTeam.addEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(getGame().getScoreboard());

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

		Set<Player> hunters = getOnlineHunters();
		GameAreaController areaController = getGame().getAreaController();

		Location hunterLocation;
		if (!hunters.isEmpty()) {
			Player teammate = hunters.iterator().next();
			Location teammateLocation = teammate.getLocation();
			hunterLocation = areaController.constrain(Utility.getGroundedLocationAround(teammateLocation, 2, 10, teammateLocation));
		} else {
			hunterLocation = areaController.pickRandom(0.3, 0.7);
		}

		hunterTeam.addEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer, hunterLocation);
		onlinePlayer.setScoreboard(getGame().getScoreboard());

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

		Location gameLocation = getGame().getAreaController().pickRandom(0.0, 1.0);

		spectatorTeam.addEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(gameLocation);
		onlinePlayer.setBedSpawnLocation(gameLocation, true);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		onlinePlayer.setScoreboard(getGame().getScoreboard());

		Optional<Player> prey = getOnlinePrey().stream().findFirst();
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
			onlinePlayer.teleport(getGame().getWorldController().getReturnLocation());
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