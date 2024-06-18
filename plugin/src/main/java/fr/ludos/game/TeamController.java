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

import fr.ludos.Main;

public abstract class TeamController implements Listener {

	protected Scoreboard scoreboard;


	public TeamController(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}

	public void stop() {
		HandlerList.unregisterAll(this);
	}


	protected abstract Collection<Player> getPlayers();
	protected abstract Collection<Team> getTeams();

	public boolean areAllies(HumanEntity player1, HumanEntity player2) {
		var player1Team = scoreboard.getEntryTeam(player1.getName());
		var player2Team = scoreboard.getEntryTeam(player2.getName());
		var res = player1Team.equals(player2Team);
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
