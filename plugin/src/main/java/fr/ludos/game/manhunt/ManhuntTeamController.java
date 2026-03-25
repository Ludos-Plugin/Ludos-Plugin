package fr.ludos.game.manhunt;

import java.util.Optional;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Random;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.ludos.Utility;
import fr.ludos.command.ludos.GameSubcommand;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;
import fr.ludos.game.GameTeamController;


public final class ManhuntTeamController extends GameTeamController {
	public Team hunterTeam;
	public Team preyTeam;
	public Team spectatorTeam;

	private final Set<Player> selectedHunters;
	public Set<Player> getSelectedHunters() {
		return selectedHunters;
	}
	private final Player selectedPrey;
	public Player getSelectedPrey() {
		return selectedPrey;
	}


	public ManhuntTeamController(ManhuntGame game, @Nullable Set<Player> players, @Nullable Player prey) {
		super(game, game.getManhuntBuilder().getJoinOption(game.getGroup().getConfig()));

		Set<Player> finalPlayers;
		if (players == null) {
			finalPlayers = game.getGroup().getOnlinePlayers().stream()
				.filter(p -> p.isOnline())
				.collect(Collectors.toSet());
		} else {
			finalPlayers = players.stream()
				.filter(p -> p.isOnline())
				.collect(Collectors.toSet());
		}

		if (finalPlayers.isEmpty()) {
			throw new IllegalArgumentException("No players available (Check if the configured players are online)");
		}

		if (prey == null) {
			Player[] playersArray = finalPlayers.toArray(new Player[finalPlayers.size()]);
			prey = playersArray[ new Random().nextInt(playersArray.length) ];
		}
		else if (!prey.isOnline()) {
			throw new IllegalArgumentException("Configured Prey is not online");
		}

		finalPlayers.remove(prey);

		this.selectedHunters = finalPlayers;
		this.selectedPrey = prey;
	}

	@Override
	protected void onStart() {
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

		joinPrey(selectedPrey);

		for (Player hunter : selectedHunters) {
			if (hunter == null) continue;
			joinHunter(hunter);
		}

		Set<Player> spectators = getGame().getGroup().getOnlinePlayers().stream()
			.filter(p -> p.isOnline())
			.filter(p -> p != getSelectedPrey())
			.filter(p -> !selectedHunters.contains(p))
			.collect(Collectors.toSet());

		for (Player spectator : spectators) {
			joinSpectator(spectator);
		}
	}

	@Override
	protected void onStop() {
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
			.filter(p -> p != null)
			.collect(Collectors.toSet());
	}
	public Set<Player> getOnlineHunters() {
		return getHunters().stream()
			.filter(p -> p.isOnline())
			.map(p -> p.getPlayer())
			.filter(p -> p != null)
			.collect(Collectors.toSet());
	}
	public OfflinePlayer getPrey() {
		if (preyTeam == null) return null;
		return preyTeam.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.filter(p -> p != null)
			.findFirst().get();
	}
	public Player getOnlinePrey() {
		OfflinePlayer prey = getPrey();
		if (prey == null || !prey.isOnline()) return null;
		return prey.getPlayer();
	}


	@Override
	public Collection<Team> getTeams() {
		return List.of(hunterTeam, preyTeam);
	}
	@Override
	public Collection<LivingEntity> getEntities() {
		Set<LivingEntity> entities = new HashSet<>();

		Player prey = getOnlinePrey();
		if (prey != null) {
			entities.add(prey);
		}

		entities.addAll(getOnlineHunters());

		return entities;
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (! preyTeam.hasEntry(player.getName())) {
			Utility.onDeathSpectate(player, 5, getPlugin());
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
			.append(Component.text(getPrey().getName())
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

		Player prey = getOnlinePrey();
		if (prey != null) {
			onlinePlayer.teleport(prey.getLocation());
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
			onlinePlayer.teleport(getGame().getAreaController().getReturnLocation());
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