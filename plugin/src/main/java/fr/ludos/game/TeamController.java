package fr.ludos.game;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public abstract class TeamController implements Listener {

	protected final Game game;
	protected final Scoreboard scoreboard;


	public TeamController(Game game, Scoreboard scoreboard) {
		this.game = game;
		this.scoreboard = scoreboard;
		Bukkit.getPluginManager().registerEvents(this, game.getPlugin());
	}

	public void stop() {
		HandlerList.unregisterAll(this);
	}


	public abstract Collection<Player> getPlayers();
	public abstract Collection<Team> getTeams();

	public boolean areAllies(HumanEntity player1, HumanEntity player2) {
		var player1Team = scoreboard.getEntryTeam(player1.getName());
		var player2Team = scoreboard.getEntryTeam(player2.getName());
		var res = player1Team != null && player1Team.equals(player2Team);
		return res;
	}

	public Set<Player> getEnemies(Player player) {
		return getPlayers().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}

	// public abstract void updatePlayerTeam(Player player);

	// @EventHandler
	// public void onPlayerJoin(PlayerJoinEvent event) {
	// 	updatePlayerTeam(event.getPlayer());
	// }

	// @EventHandler
	// public void onPlayerRespawn(PlayerRespawnEvent event)  {
	// 	updatePlayerTeam(event.getPlayer());
	// }
}
