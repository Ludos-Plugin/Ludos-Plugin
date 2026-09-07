package fr.ludos.games.raid;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Team controller for {@link RaidGame}.
 */
public final class RaidTeamController extends GameTeamController {
	private final Set<OfflinePlayer> selectedPlayers;

	private Team playersTeam;
	private Team spectatorsTeam;


	public RaidTeamController(RaidGame game, @Nullable Set<OfflinePlayer> selectedPlayers) {
		super(game);

		this.selectedPlayers = selectedPlayers;
	}

	@Override
	public Collection<Team> getTeams() {
		return Set.of(playersTeam, spectatorsTeam);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Scoreboard scoreboard = game().scoreboard();

		playersTeam = createOrGetTeam(scoreboard, "ArenaTeam", NamedTextColor.BLUE, false);
		spectatorsTeam = createOrGetTeam(scoreboard, "ArenaSpectators", NamedTextColor.GRAY, true);

		Set<Player> online = game().group().getOnlinePlayers();

		Set<Player> finalPlayers;
		if (selectedPlayers == null || selectedPlayers.isEmpty()) {
			finalPlayers = online;
		} else {
			finalPlayers = Utility.getOnline(selectedPlayers).collect(Collectors.toSet());
		}

		for (Player player : online) {
			if (finalPlayers.contains(player)) {
				moveToTeam(player, playersTeam);
			} else {
				moveToTeam(player, spectatorsTeam);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (playersTeam != null) {
			playersTeam.unregister();
		}
		playersTeam = null;

		if (spectatorsTeam != null) {
			spectatorsTeam.unregister();
		}
		spectatorsTeam = null;
	}

	private Team createOrGetTeam(Scoreboard scoreboard, String name, NamedTextColor color, boolean friendlyFire) {
		Team team = scoreboard.getTeam(name);
		if (team == null) {
			team = scoreboard.registerNewTeam(name);
		}
		team.color(color);
		team.setAllowFriendlyFire(friendlyFire);
		return team;
	}

	private void moveToTeam(OfflinePlayer player, Team team) {
		if (player == null || team == null) return;

		playersTeam.removePlayer(player);
		spectatorsTeam.removePlayer(player);
		team.addPlayer(player);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.setScoreboard(game().scoreboard());
	}

	public void joinAnyPlayer(Player player) {
		player.setScoreboard(game().scoreboard());

		Utility.resetPlayer(player);

		player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 3, 0, true, false, false));
	}

	@Override
	protected void joinPlayer(OfflinePlayer player) {
		if (player == null) return;
		if (getPlayers().contains(player)) return;

		moveToTeam(player, playersTeam);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SURVIVAL);

		SpecialItem.Events.refreshPlayerInventory(game(), onlinePlayer);

		placeActivePlayer(onlinePlayer);
	}

	public void joinSpectator(OfflinePlayer player) {
		if (player == null) return;
		if (getPlayers().contains(player)) return;

		moveToTeam(player, spectatorsTeam);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);

		placeSpectator(onlinePlayer);
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		playersTeam.removePlayer(player);

		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		playersTeam.removePlayer(player);
		spectatorsTeam.removePlayer(player);
	}

	@Override
	public void placePlayer(OfflinePlayer player) {
		if (player == null) return;

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		if (playersTeam.hasPlayer(onlinePlayer)) {
			placeActivePlayer(onlinePlayer);
		} else {
			placeSpectator(onlinePlayer);
		}
	}

	private void placeActivePlayer(Player player) {
		Location teammateLocation = Utility.snapToHighestY(getLocationAroundTeammate(playersTeam), true);

		player.teleport(teammateLocation);
	}
	private void placeSpectator(Player player) {
		player.teleport(Utility.snapToHighestY(getLocationAroundTeammate(playersTeam), true));
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!getPlayers().contains(player)) return;

		Utility.onDeathSpectate(event, plugin());
	}
}
