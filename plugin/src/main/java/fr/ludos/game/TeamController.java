package fr.ludos.game;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


public abstract class TeamController implements Listener {
	private boolean started = false;
	protected final Game game;
	protected final Scoreboard scoreboard;


	public TeamController(Game game, Scoreboard scoreboard) {
		this.game = game;
		this.scoreboard = scoreboard;
	}

	public final void start() {
		if (started) return;
		started = true;

		Bukkit.getPluginManager().registerEvents(this, game.getPlugin());

		onStart();
	}
	protected void onStart() { }

	public final void stop() {
		if (! started) return;
		started = false;

		HandlerList.unregisterAll(this);

		onStop();
	}
	protected void onStop() { }


	public abstract Collection<LivingEntity> getEntities();
	public abstract Collection<Team> getTeams();

	public Collection<Player> getPlayers() {
		return getEntities().stream()
			.filter(e -> e instanceof Player)
			.map(e -> (Player)e)
			.collect(Collectors.toSet());
	}

	public boolean areAllies(LivingEntity entity1, LivingEntity entity2) {
		if (entity1 == null || entity2 == null) return false;
		if (entity1.equals(entity2)) return true;

		var entity1Team = scoreboard.getEntryTeam(entity1.getName());
		var entity2Team = scoreboard.getEntryTeam(entity2.getName());

		return entity1Team != null && entity1Team.equals(entity2Team);
	}

	public Set<LivingEntity> getEnemies(Player player) {
		return getEntities().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}
	public Set<Player> getEnemyPlayers(Player player) {
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
