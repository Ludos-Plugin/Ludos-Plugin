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
	private final Set<Player> selectedPlayers;

	private Team playersTeam;
	private Team spectatorsTeam;


	public RaidTeamController(RaidGame game, @Nullable Set<OfflinePlayer> selectedPlayers) {
		super(game);

		Set<Player> online = game.getGroup().getOnlinePlayers();
		if (online.size() < 2) {
			throw new IllegalArgumentException("At least 2 online players are required for Arena");
		}

		Set<Player> configuredPlayers;
		if (selectedPlayers == null || selectedPlayers.isEmpty()) {
			configuredPlayers = online;
		} else {
			configuredPlayers = Utility.getOnline(selectedPlayers).collect(Collectors.toSet());
		}

		this.selectedPlayers = configuredPlayers;
	}

	@Override
	public Collection<Team> getTeams() {
		return Set.of(playersTeam, spectatorsTeam);
	}

	@Override
	protected void onStart() {
		super.onStart();

		Scoreboard scoreboard = getGame().getScoreboard();

		playersTeam = createOrGetTeam(scoreboard, "ArenaTeam", NamedTextColor.BLUE, false);
		spectatorsTeam = createOrGetTeam(scoreboard, "ArenaSpectators", NamedTextColor.GRAY, true);


		for (Player player : getGame().getGroup().getOnlinePlayers()) {
			if (selectedPlayers.contains(player)) {
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

		Location teammateLocation = Utility.snapToHighestY(getLocationAroundTeammate(team), true);

		String name = player.getName();
		playersTeam.removeEntry(name);
		spectatorsTeam.removeEntry(name);
		team.addEntry(name);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		onlinePlayer.teleport(teammateLocation);

		onlinePlayer.setScoreboard(getGame().getScoreboard());
	}

	public void joinAnyPlayer(Player player) {
		player.setScoreboard(getGame().getScoreboard());

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

		SpecialItem.Events.refreshPlayerInventory(getGame(), onlinePlayer);
	}

	public void joinSpectator(OfflinePlayer player) {
		if (player == null) return;
		if (getPlayers().contains(player)) return;

		moveToTeam(player, spectatorsTeam);

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		joinAnyPlayer(onlinePlayer);
		onlinePlayer.setGameMode(GameMode.SPECTATOR);
	}

	@Override
	protected void discardPlayer(OfflinePlayer player) {
		joinSpectator(player);
	}

	@Override
	public void removePlayer(OfflinePlayer player) {
		playersTeam.removeEntry(player.getName());
		spectatorsTeam.removeEntry(player.getName());

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			SpecialItem.Events.removeFromPlayerInventory(getGame(), onlinePlayer);
			onlinePlayer.teleport(getGame().getWorldManager().getReturnLocation());
		}
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (!getPlayers().contains(player)) return;

		Utility.onDeathSpectate(event, getPlugin());
	}
}
