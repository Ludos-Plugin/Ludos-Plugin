package fr.ludos.game.teamController;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.game.Game;
import fr.ludos.game.TwoStepGameProcessBase;



public abstract class GameTeamController extends TwoStepGameProcessBase {
	private final Game game;
	public final Game getGame() {
		return game;
	}

	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final GameJoinOption joinOption;


	public GameTeamController(Game game, GameJoinOption joinOption) {
		this.game = game;
		this.joinOption = joinOption;
	}
	public GameTeamController(Game game) {
		this(game, GameJoinOption.auto);
	}


	public abstract Collection<LivingEntity> getEntities();
	public abstract Collection<Team> getTeams();

	public Collection<OfflinePlayer> getPlayers() {
		return getEntities().stream()
			.filter(e -> e instanceof OfflinePlayer)
			.map(e -> (OfflinePlayer)e)
			.collect(Collectors.toSet());
	}

	public boolean areAllies(String entityName1, String entityName2) {
		if (entityName1 == null || entityName2 == null) return false;
		if (entityName1.equals(entityName2)) return true;

		Scoreboard scoreboard = game.getScoreboard();

		var entity1Team = scoreboard.getEntryTeam(entityName1);
		var entity2Team = scoreboard.getEntryTeam(entityName2);

		return entity1Team != null && entity1Team.equals(entity2Team);
	}
	public boolean areAllies(LivingEntity entity1, LivingEntity entity2) {
		return areAllies(entity1.getName(), entity2.getName());
	}
	public boolean areAllies(OfflinePlayer player1, OfflinePlayer player2) {
		return areAllies(player1.getName(), player2.getName());
	}

	public Set<LivingEntity> getEnemies(Player player) {
		return getEntities().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}
	public Set<OfflinePlayer> getEnemyPlayers(OfflinePlayer player) {
		return getPlayers().stream()
			.filter(other -> ! areAllies(player, other))
			.collect(Collectors.toSet());
	}

	public final void addPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (joinOption == GameJoinOption.none && onlinePlayer != null) {
			onlinePlayer.sendMessage("Joining is not enabled for this game session.");
			return;
		}
		joinPlayer(player);
	}
	protected abstract void joinPlayer(OfflinePlayer player);
	protected abstract void discardPlayer(OfflinePlayer player);
	public abstract void removePlayer(OfflinePlayer player);

	public final void tryJoinPlayer(OfflinePlayer player) {
		if (joinOption == GameJoinOption.auto) {
			joinPlayer(player);
		}
		else {
			discardPlayer(player);
		}
	}
}
