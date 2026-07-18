package fr.ludos.core.game.teamController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.ludos.core.Utility;
import fr.ludos.core.area.Area;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameProcessBase;



public abstract class GameTeamController extends GameProcessBase {
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
		this(game, GroupConfigMap.GAME_JOIN.getGroupConfig(game.getGroup()));
	}

	public abstract Collection<Team> getTeams();

	public final Set<Entity> getEntities() {
		Set<Entity> all = new HashSet<>();
		for (Team team : getTeams()) {
			all.addAll(Utility.getTeamEntities(team).toList());
		}
		return all;
	}
	public final Stream<LivingEntity> getLivingEntitiesStream() {
		return getEntities().stream()
			.filter(e -> e instanceof LivingEntity)
			.map(e -> (LivingEntity) e);
	}
	public final Set<LivingEntity> getLivingEntities() {
		return getLivingEntitiesStream().collect(Collectors.toSet());
	}
	public final Stream<OfflinePlayer> getPlayersStream() {
		return getEntities().stream()
			.filter(e -> e instanceof OfflinePlayer)
			.map(e -> (OfflinePlayer) e);
	}
	public final Set<OfflinePlayer> getPlayers() {
		return getPlayersStream().collect(Collectors.toSet());
	}
	public final Stream<Player> getOnlinePlayersStream() {
		return Utility.getOnline(getPlayers().stream());
	}
	public final Set<Player> getOnlinePlayers() {
		return getOnlinePlayersStream().collect(Collectors.toSet());
	}
	public final Stream<Player> getAlivePlayersStream() {
		return getOnlinePlayersStream()
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL && ! p.isDead());
	}
	public final Set<Player> getAlivePlayers() {
		return getAlivePlayersStream().collect(Collectors.toSet());
	}

	public final Stream<Entity> getTeamEntitiesStream(@Nullable Team team) {
		if (team == null) return Stream.of();
		return Utility.getTeamEntities(team);
	}
	public final Set<Entity> getTeamEntities(Team team) {
		return getTeamEntitiesStream(team)
			.collect(Collectors.toSet());
	}
	public final Stream<OfflinePlayer> getTeamPlayersStream(Team team) {
		return team.getEntries().stream()
			.map(Bukkit::getOfflinePlayer)
			.filter(Objects::nonNull);
	}
	public final Set<OfflinePlayer> getTeamPlayers(Team team) {
		return getTeamPlayersStream(team)
			.collect(Collectors.toSet());
	}
	public final Stream<Player> getTeamOnlinePlayersStream(Team team) {
		return Utility.getOnline(getTeamPlayersStream(team));
	}
	public final Set<Player> getTeamOnlinePlayers(Team team) {
		return getTeamOnlinePlayersStream(team)
			.collect(Collectors.toSet());
	}
	public final Stream<Player> getTeamAlivePlayersStream(Team team) {
		return getTeamOnlinePlayersStream(team)
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL && ! p.isDead());
	}
	public final Set<Player> getTeamAlivePlayers(Team team) {
		return getTeamAlivePlayersStream(team)
			.collect(Collectors.toSet());
	}

	public final boolean contains(OfflinePlayer player) {
		return getPlayers().contains(player);
	}

	public boolean areAllies(String entityName1, String entityName2) {
		if (entityName1 == null || entityName2 == null) return false;
		if (entityName1.equals(entityName2)) return true;

		Scoreboard scoreboard = game.getScoreboard();

		var entity1Team = scoreboard.getEntryTeam(entityName1);
		var entity2Team = scoreboard.getEntryTeam(entityName2);

		return entity1Team != null && entity1Team.equals(entity2Team);
	}
	public boolean areEntitiesAllies(Entity entity1, Entity entity2) {
		return areAllies(entity1.getName(), entity2.getName());
	}
	public boolean arePlayersAllies(OfflinePlayer player1, OfflinePlayer player2) {
		return areAllies(player1.getName(), player2.getName());
	}

	public Predicate<OfflinePlayer> isPlayerAllyOfEntity(Entity entity) {
		return (p) -> areAllies(p.getName(), entity.getName());
	}
	public Predicate<OfflinePlayer> isPlayerEnemyOfEntity(Entity entity) {
		return Predicate.not(isPlayerAllyOfEntity(entity));
	}

	public Predicate<Entity> isEntityAllyOfEntity(Entity entity) {
		return (p) -> areAllies(p.getName(), entity.getName());
	}
	public Predicate<Entity> isEntityEnemyOfEntity(Entity entity) {
		return Predicate.not(isEntityAllyOfEntity(entity));
	}

	public Predicate<OfflinePlayer> isPlayerAllyOfPlayer(OfflinePlayer player) {
		return (p) -> arePlayersAllies(p, player);
	}
	public Predicate<OfflinePlayer> isPlayerEnemyOfPlayer(OfflinePlayer player) {
		return Predicate.not(isPlayerAllyOfPlayer(player));
	}

	public Predicate<Entity> isEntityAllyOfPlayer(OfflinePlayer player) {
		return (p) -> areAllies(p.getName(), player.getName());
	}
	public Predicate<Entity> isEntityEnemyOfPlayer(OfflinePlayer player) {
		return Predicate.not(isEntityAllyOfPlayer(player));
	}

	public final Player pickRandomPlayer() {
		List<Player> players = getAlivePlayersStream().toList();
		if (players.isEmpty()) return null;
		return players.get(game.random.nextInt(players.size()));
	}

	public final Location getLocationAroundTeammate(Team team) {
		return getLocationAroundTeammate(
			team,
			(area) -> area.pickRandom(0.3, 0.7)
		);
	}
	public final Location getLocationAroundTeammate(Team team, Function<Area, Location> noPlayerFallback) {
		return getLocationAroundTeammate(
			team,
			noPlayerFallback,
			() -> getGame().getWorldManager().getWorld().getSpawnLocation()
		);
	}
	public final Location getLocationAroundTeammate(Team team, Function<Area, Location> noPlayerFallback, Supplier<Location> noAreaFallback) {
		Set<Player> players = getTeamAlivePlayers(team);
		Area area = getGame().getWorldManager().getArea();

		if (! players.isEmpty()) {
			Player teammate = players.iterator().next();
			Location teammateLocation = teammate.getLocation();
			Location aroundLocation = Utility.getLocationAround(teammateLocation, 2, 10, teammateLocation);
			return area != null
				? area.constrain(aroundLocation)
				: aroundLocation;
		} else {
			return area != null
				? noPlayerFallback.apply(area)
				: noAreaFallback.get();
		}
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
