package fr.ludos.games.sheepwars;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
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
import fr.ludos.core.game.teamController.TeamControllerUtility;
import fr.ludos.core.world.WorldManager;
import fr.ludos.games.raid.RaidGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Team controller for {@link RaidGame}.
 */
public final class SheepwarsTeamController extends GameTeamController {
	private final SheepwarsGame sheepwars;

	private Team redTeam;
	private Team blueTeam;
	private Team spectatorTeam;

	private final Set<OfflinePlayer> redPlayers;
	private final Set<OfflinePlayer> bluePlayers;

	public SheepwarsTeamController(SheepwarsGame game, @Nullable Set<OfflinePlayer> team1Players, @Nullable Set<OfflinePlayer> team2Players) {
		super(game);
		this.sheepwars = game;

		this.redPlayers = team1Players;
		this.bluePlayers = team2Players;
	}

	@Override
	protected void onStart() {
		Scoreboard scoreboard = game().scoreboard();

		redTeam = scoreboard.registerNewTeam("Team Red");
		redTeam.displayName(Component.text("Red").color(NamedTextColor.RED));
		redTeam.color(NamedTextColor.RED);
		redTeam.setAllowFriendlyFire(false);

		blueTeam = scoreboard.registerNewTeam("Team Blue");
		blueTeam.displayName(Component.text("Blue").color(NamedTextColor.BLUE));
		blueTeam.color(NamedTextColor.BLUE);
		blueTeam.setAllowFriendlyFire(false);

		spectatorTeam = scoreboard.getTeam("Spectators");
		if (spectatorTeam == null) {
			spectatorTeam = scoreboard.registerNewTeam("Spectators");
			spectatorTeam.color(NamedTextColor.GRAY);
			spectatorTeam.setAllowFriendlyFire(false);
		}

		Triple<Set<Player>, Set<Player>, Set<Player>> finalPlayers = TeamControllerUtility.splitPlayersInTwoTeams(game().group(), redPlayers, bluePlayers);

		for (Player redPlayer : finalPlayers.getLeft()) {
			joinActivePlayer(redPlayer, redTeam);
		}
		for (Player bluePlayer : finalPlayers.getMiddle()) {
			joinActivePlayer(bluePlayer, blueTeam);
		}
		for (Player spectatorPlayer : finalPlayers.getRight()) {
			joinActivePlayer(spectatorPlayer, spectatorTeam);
		}
	}

	@Override
	protected void onStop() {
		redTeam.unregister();
		blueTeam.unregister();
		spectatorTeam.unregister();
	}

	@Override
	public Collection<Team> getTeams() {
		return List.of(redTeam, blueTeam, spectatorTeam);
	}

	private Location getTeamLocation(Team team) {
		return team == blueTeam ? sheepwars.blueShip().getEntranceLocation() : sheepwars.redShip().getEntranceLocation();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		if (game().worldManager().isLobbyStarted()) return;
		if (! getPlayers().contains(player)) return;

		Team team = game().scoreboard().getEntryTeam(player.getName());
		if (team == null) return;

		game().group().sendMessage(
			Component.text(player.getName() + " from team ")
				.append(team.displayName())
				.append(Component.text(" has been eliminated!"))
				.color(NamedTextColor.GOLD)
		);

		Utility.onDeathSpectate(
			event,
			getLocationAroundTeammate(player, team, (area) -> getTeamLocation(team), () -> getTeamLocation(team)),
			plugin()
		);
		team.removeEntry(player.getName());

		if (team.getSize() == 0) {
			game().group().sendMessage(Component.text("All Players of Team ").append(team.displayName()).append(Component.text(" are Dead! End of Game!"))); // TODO: Translate
			game().scheduleEndGame(5);
		}
	}

	@Override
	protected void joinPlayer(OfflinePlayer player) {
		if (player == null) return;

		if (getPlayers().contains(player)) return;

		Set<Player> primaryTeamPlayers = getTeamOnlinePlayers(redTeam);
		Set<Player> secondaryTeamPlayers = getTeamOnlinePlayers(blueTeam);

		int primarySize = primaryTeamPlayers.size();
		int secondarySize = secondaryTeamPlayers.size();

		if (primarySize < secondarySize) {
			joinActivePlayer(player, redTeam);
		} else if (primarySize > secondarySize) {
			joinActivePlayer(player, blueTeam);
		} else {
			joinActivePlayer(player, game().random().nextFloat() < 0.5 ? redTeam : blueTeam);
		}
	}

	private void joinActivePlayer(OfflinePlayer player, Team destination) {
		if (player == null || destination == null) return;

		redTeam.removePlayer(player);
		blueTeam.removePlayer(player);
		spectatorTeam.removePlayer(player);

		destination.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());

		onlinePlayer.setGameMode(GameMode.SURVIVAL);

		Utility.resetPlayer(onlinePlayer);
		onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 3, 0, true, false, false));

		if (destination == blueTeam) {
			placeBluePlayer(onlinePlayer);
		} else {
			placeRedPlayer(onlinePlayer);
		}
	}

	public void joinSpectator(OfflinePlayer player) {
		if (player == null) return;

		redTeam.removePlayer(player);
		blueTeam.removePlayer(player);
		spectatorTeam.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());

		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		placeSpectator(onlinePlayer);
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		redTeam.removePlayer(player);
		blueTeam.removePlayer(player);
		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		redTeam.removePlayer(player);
		blueTeam.removePlayer(player);
		spectatorTeam.removePlayer(player);
	}


	@Override
	public void placePlayer(OfflinePlayer player) {
		if (redTeam.hasPlayer(player)) {
			placeRedPlayer(player);
		}
		else if (blueTeam.hasPlayer(player)) {
			placeBluePlayer(player);
		}
		else {
			placeSpectator(player);
		}
	}

	public void placeRedPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(
			sheepwars.redShip().getEntranceLocation()
				.setDirection(sheepwars.blueShip().getLocation().toVector().subtract(sheepwars.redShip().getLocation().toVector()))
		);
	}

	public void placeBluePlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(
			sheepwars.blueShip().getEntranceLocation()
				.setDirection(sheepwars.redShip().getLocation().toVector().subtract(sheepwars.blueShip().getLocation().toVector()))
		);
	}

	public void placeSpectator(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		WorldManager worldManager = game().worldManager();
		Area area = worldManager.getArea();

		Location center = area != null
			? area.getCenter()
			: worldManager.getWorld().getSpawnLocation();
		center = Utility.snapToHighestY(center, true);
		onlinePlayer.teleport(center, true);
	}
}