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
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;
import fr.ludos.game.GameTeamController;


public final class ManhuntTeamController extends GameTeamController {
	public Team hunterTeam;
	public Team preyTeam;

	private final Set<Player> selectedHunters;
	public Set<Player> getSelectedHunters() {
		return selectedHunters;
	}
	private final Player selectedPrey;
	public Player getSelectedPrey() {
		return selectedPrey;
	}


	public ManhuntTeamController(ManhuntGame game, @Nullable Set<Player> players, @Nullable Player prey) {
		super(game);

		Set<Player> finalPlayers = new HashSet<>();
		if (players == null) {
			finalPlayers.addAll(Bukkit.getOnlinePlayers());
		} else {
			finalPlayers.addAll(players.stream()
				.filter(p -> p.isOnline())
				.collect(Collectors.toSet())
			);
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


		joinPrey(selectedPrey);

		for (Player hunter : selectedHunters) {
			if (hunter == null) continue;
			joinHunter(hunter);
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


	public Set<Player> getTeamHunters() {
		if (hunterTeam == null) return Set.of();
		return hunterTeam.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(p -> p != null)
			.collect(Collectors.toSet());
	}
	public Player getTeamPrey() {
		if (preyTeam == null) return null;
		return preyTeam.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(p -> p != null)
			.findFirst().get();
	}


	@Override
	public Collection<Team> getTeams() {
		return List.of(hunterTeam, preyTeam);
	}
	@Override
	public Collection<LivingEntity> getEntities() {
		Set<LivingEntity> entities = new HashSet<>();

		Player prey = getTeamPrey();
		if (prey != null) {
			entities.add(prey);
		}

		entities.addAll(getTeamHunters());

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
					Game.stopCurrentGame();
				}
			}.runTaskLater(getPlugin(), 0);
		}
	}


	@Override
	public void joinPlayer(Player player) {
		joinHunter(player);
	}

	private void joinAnyPlayer(Player player, Location location) {
		player.teleport(location);
		player.setBedSpawnLocation(location, true);
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
	}

	public void joinPrey(Player player) {
		Location gameLocation = getGame().getGameAreaController().pickRandom(0.0, 0.2);

		joinAnyPlayer(player, gameLocation);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 40, 0, false, true));

		preyTeam.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());


		player.showTitle(Title.title(
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

	public void joinHunter(Player player) {
		Set<Player> hunters = getTeamHunters();
		GameAreaController areaController = getGame().getGameAreaController();

		Location hunterLocation;
		if (!hunters.isEmpty()) {
			Player teammate = hunters.iterator().next();
			Location teammateLocation = teammate.getLocation();
			hunterLocation = areaController.constrain(Utility.getGroundedLocationAround(teammateLocation, 2, 10, teammateLocation));
		} else {
			hunterLocation = areaController.pickRandom(0.3, 0.7);
		}

		joinAnyPlayer(player, hunterLocation);

		hunterTeam.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());


		player.showTitle(Title.title(
			Component.text("You are a ")
			.append(Component.text("Hunter")
				.color(NamedTextColor.RED)),
			Component.text("Go and seek ")
			.append(Component.text(getTeamPrey().getName())
				.color(NamedTextColor.BLUE)),
			Title.Times.times(
				Duration.ofMillis(500),
				Duration.ofMillis(3500),
				Duration.ofMillis(1000)
			)
		));
	}


	@Override
	public void discardPlayer(Player player) {
		hunterTeam.removeEntry(player.getName());
		preyTeam.removeEntry(player.getName());

		player.setGameMode(GameMode.SPECTATOR);

		Player prey = getTeamPrey();
		if (prey != null) {
			player.teleport(prey.getLocation());
		}
	}

	// @Override
	// public void updatePlayerTeam(Player player) {
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