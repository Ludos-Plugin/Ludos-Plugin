package fr.ludos.game.alien;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.game.Game;
import fr.ludos.game.GameTeamController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public final class AlienTeamController extends GameTeamController {
	public Team survivorTeam;
	public Team spectatorTeam;

	private final Set<Player> selectedPlayers;
	private final Player selectedPrey;

	public Set<Player> getSelectedHunters() {
		return selectedPlayers;
	}

	public Player getSelectedPrey() {
		return selectedPrey;
	}

	public AlienTeamController(AlienGame game, @Nullable Set<Player> players, @Nullable Player prey) {
		super(game, fr.ludos.game.GameJoinOption.auto);

		Set<Player> finalPlayers = new HashSet<>();
		if (players == null) {
			finalPlayers.addAll(Bukkit.getOnlinePlayers());
		} else {
			finalPlayers.addAll(players.stream().filter(Player::isOnline).collect(Collectors.toSet()));
		}

		if (finalPlayers.isEmpty()) {
			throw new IllegalArgumentException("No players available (Check if the configured players are online)");
		}

		if (prey == null) {
			Player[] playersArray = finalPlayers.toArray(new Player[0]);
			prey = playersArray[new Random().nextInt(playersArray.length)];
		} else if (!prey.isOnline()) {
			throw new IllegalArgumentException("Configured player is not online");
		}

		this.selectedPlayers = finalPlayers;
		this.selectedPrey = prey;
	}

	@Override
	protected void onStart() {
		Scoreboard scoreboard = getGame().getScoreboard();

		survivorTeam = scoreboard.getTeam("AlienSurvivors");
		if (survivorTeam == null) {
			survivorTeam = scoreboard.registerNewTeam("AlienSurvivors");
			survivorTeam.color(NamedTextColor.AQUA);
			survivorTeam.setAllowFriendlyFire(false);
		}

		spectatorTeam = scoreboard.getTeam("AlienSpectators");
		if (spectatorTeam == null) {
			spectatorTeam = scoreboard.registerNewTeam("AlienSpectators");
			spectatorTeam.color(NamedTextColor.GRAY);
			spectatorTeam.setAllowFriendlyFire(false);
		}

		for (Player player : selectedPlayers) {
			joinPlayer(player);
		}
	}

	@Override
	protected void onStop() {
		if (survivorTeam != null) {
			survivorTeam.unregister();
			survivorTeam = null;
		}
		if (spectatorTeam != null) {
			spectatorTeam.unregister();
			spectatorTeam = null;
		}
	}

	public Set<Player> getAllPlayers() {
		if (survivorTeam == null) {
			return Set.of();
		}
		return survivorTeam.getEntries().stream()
				.map(Bukkit::getPlayerExact)
				.filter(p -> p != null)
				.collect(Collectors.toSet());
	}

	public Set<Player> getLivingPlayers() {
		return getAllPlayers().stream()
				.filter(p -> p.getGameMode() != GameMode.SPECTATOR)
				.collect(Collectors.toSet());
	}

	@Override
	public Collection<Team> getTeams() {
		return List.of(survivorTeam);
	}

	@Override
	public Collection<LivingEntity> getEntities() {
		return new HashSet<>(getAllPlayers());
	}

	@Override
	public void joinPlayer(Player player) {
		if (survivorTeam.hasEntry(player.getName())) {
			return;
		}

		Location location = getGame().getGameAreaController().getCenter();
		player.teleport(location);
		player.setBedSpawnLocation(location, true);
		player.setGameMode(GameMode.SURVIVAL);
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));

		survivorTeam.addEntry(player.getName());
		player.setScoreboard(getGame().getScoreboard());

		player.showTitle(Title.title(
				Component.text("You are a ").append(Component.text("Survivor").color(NamedTextColor.AQUA)),
				Component.text("Complete the quests and survive"),
				Title.Times.times(
						Duration.ofMillis(500),
						Duration.ofMillis(3500),
						Duration.ofMillis(1000))));
	}

	public void joinSpectator(Player player) {
		if (survivorTeam.hasEntry(player.getName())) {
			survivorTeam.removeEntry(player.getName());
		}

		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(getGame().getGameAreaController().getCenter());

		if (spectatorTeam != null) {
			spectatorTeam.addEntry(player.getName());
		}
		player.setScoreboard(getGame().getScoreboard());
	}

	@Override
	public void discardPlayer(Player player) {
		joinSpectator(player);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!survivorTeam.hasEntry(player.getName())) {
			return;
		}

		Bukkit.broadcast(Component.text(player.getName() + " was killed by the Alien!").color(NamedTextColor.RED));

		player.setGameMode(GameMode.SPECTATOR);
		survivorTeam.removeEntry(player.getName());

		if (spectatorTeam != null) {
			spectatorTeam.addEntry(player.getName());
		}

		if (getLivingPlayers().isEmpty()) {
			Bukkit.broadcast(Component.text("All survivors are dead! End of Game!").color(NamedTextColor.RED));
			Game.stopCurrentGame();
		}
	}
}