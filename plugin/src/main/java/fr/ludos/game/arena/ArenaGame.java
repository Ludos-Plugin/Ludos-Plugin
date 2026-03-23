package fr.ludos.game.arena;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.Utility;
import fr.ludos.game.GameJoinOption;
import fr.ludos.game.GameTeamController;
import fr.ludos.game.worldborder.WorldBorderAreaController;
import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class ArenaGame extends Game {
	public static final String ID = "arena";

	public static final String primaryTeamKey = "team1";
	public static final String primaryTeamPath = ID + '.' + primaryTeamKey;
	public static final String secondaryTeamKey = "team2";
	public static final String secondaryTeamPath = ID + '.' + secondaryTeamKey;
	public static final String modeKey = "mode";
	public static final String modePath = ID + '.' + modeKey;
	public static final String roundsKey = "rounds";
	public static final String roundsPath = ID + '.' + roundsKey;

	public static final String areaKey = "area";
	public static final String areaPath = ID + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = ID + '.' + locationKey;
	public static final String joinKey = "join";
	public static final String joinPath = ID + '.' + joinKey;

	private static final int PREP_TICKS = 20 * 10;
	private static final int ROUND_CHECK_PERIOD_TICKS = 10;
	private static final Title.Times PREP_TITLE_TIMES = Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(750), Duration.ofMillis(100));

	private final Scoreboard scoreboard;
	private final ArenaTeamController teamController;
	private final WorldBorderAreaController areaController;
	protected final Builder builder;
	private final ArenaLoadoutService loadoutService;

	private int currentRound = 0;
	private int primaryTeamRoundWins = 0;
	private int secondaryTeamRoundWins = 0;
	private boolean preparationPhase = false;

	private BukkitTask preparationTask;
	private BukkitTask roundCheckTask;

	@Override
	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public ArenaTeamController getGameTeamController() {
		return teamController;
	}

	@Override
	public WorldBorderAreaController getGameAreaController() {
		return areaController;
	}

	protected ArenaGame(Builder builder) {
		super(builder);
		this.builder = builder;

		TeamSelection selectedTeams = builder.resolveTeamSelection();

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();

		this.teamController = new ArenaTeamController(this, selectedTeams.primaryTeam(), selectedTeams.secondaryTeam(), builder.getJoinOption());
		this.areaController = new WorldBorderAreaController(this, builder.getLocation(), builder.getArea());
		this.loadoutService = new ArenaLoadoutService(this);
	}

	@Override
	protected void onGameInit() {
		Location base = pickGameBaseLocation();
		areaController.setup(base);
	}

	@Override
	protected void onGameStart() {
		World world = areaController.getWorld();
		Game.worldInitialization(world);
		startNextRound();
		Bukkit.broadcast(Component.text("Arena game started"));
	}

	@Override
	protected void onGameStop() {
		cancelRoundTasks();
		areaController.resetBorder();
		Bukkit.broadcast(Component.text("Arena game ended"));
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		return isArenaPlayer(player);
	}

	public boolean isWaveMode() {
		return builder.getMode().isWaves();
	}

	public int getConfiguredRounds() {
		return builder.getRounds();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (isWaveMode()) return;
		if (!preparationPhase) return;

		Player player = event.getPlayer();

		if (!isArenaPlayer(player)) return;

		if (isSamePosition(event.getFrom(), event.getTo())) return;

		event.setTo(event.getFrom());
	}

	public static boolean isSamePosition(Location from, Location to) {
		return from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (isWaveMode()) return;
		if (!isArenaPlayer(event.getPlayer())) return;

		Bukkit.getScheduler().runTask(getPlugin(), this::evaluateRoundState);
	}

	private void startNextRound() {
		if (currentRound >= builder.getRounds()) {
			endArenaMatch();
			return;
		}

		currentRound++;
		preparationPhase = true;

		for (Player player : teamController.getPlayers()) {
			Game.joinAnyPlayer(player, null);
		}
		teleportArenaPlayersForRound();

		for (Player player : getArenaPlayers()) {
			Game.joinAnyPlayer(player, null);
			player.getInventory().addItem(Ludos.createGuidebook());
		}

		startPreparationCountdown();
	}

	private void startPreparationCountdown() {
		preparationTask = startPreparationCountdownTask(
			"Round",
			() -> currentRound,
			"Fight starts in",
			this::startCombatPhase
		);
	}

	public void showPreparationTitle(String phaseName, int phaseNumber, String countdownPrefix, int secondsLeft) {
		for (Player player : getArenaPlayers()) {
			player.showTitle(Title.title(
				Component.text(phaseName + " " + phaseNumber).color(NamedTextColor.WHITE),
				Component.text(countdownPrefix + " " + secondsLeft + "s").color(NamedTextColor.WHITE),
				PREP_TITLE_TIMES
			));
		}
	}

	public BukkitTask startPreparationCountdownTask(String phaseName, IntSupplier phaseNumberSupplier, String countdownPrefix, Runnable onComplete) {

		return new BukkitRunnable() {
			private int ticksLeft = PREP_TICKS;

			@Override
			public void run() {
				int secondsLeft = Math.max(0, ticksLeft / 20);
				showPreparationTitle(phaseName, phaseNumberSupplier.getAsInt(), countdownPrefix, secondsLeft);

				if (ticksLeft <= 0) {
					cancel();
					onComplete.run();
					return;
				}

				ticksLeft -= 20;
			}
		}.runTaskTimer(getPlugin(), 0L, 20L);
	}

	private void startCombatPhase() {
		preparationPhase = false;

		for (Player player : getArenaPlayers()) {
			applyArenaCombatLoadout(player);
		}

		Utility.cancelTask(preparationTask);

		roundCheckTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::evaluateRoundState, 0L, ROUND_CHECK_PERIOD_TICKS);
	}

	private void evaluateRoundState() {
		int alivePrimary = teamController.getAliveCombatPlayers(0).size();
		int aliveSecondary = teamController.getAliveCombatPlayers(1).size();

		if (alivePrimary > 0 && aliveSecondary > 0) return;

		Utility.cancelTask(roundCheckTask);

		if (alivePrimary > aliveSecondary) {
			primaryTeamRoundWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Combat Team 1").color(NamedTextColor.BLUE));
		}
		else if (aliveSecondary > alivePrimary) {
			secondaryTeamRoundWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Combat Team 2").color(NamedTextColor.RED));
		}
		else {
			Bukkit.broadcast(Component.text("Round " + currentRound + " is a draw").color(NamedTextColor.WHITE));
		}

		Bukkit.getScheduler().runTaskLater(getPlugin(), this::startNextRound, 20L);
	}

	private void endArenaMatch() {
		cancelRoundTasks();

		Component result;
		if (primaryTeamRoundWins > secondaryTeamRoundWins) {
			result = Component.text("Arena finished: Combat Team 1 wins " + primaryTeamRoundWins + " - " + secondaryTeamRoundWins)
				.color(NamedTextColor.BLUE);
		}
		else if (secondaryTeamRoundWins > primaryTeamRoundWins) {
			result = Component.text("Arena finished: Combat Team 2 wins " + secondaryTeamRoundWins + " - " + primaryTeamRoundWins)
				.color(NamedTextColor.RED);
		}
		else {
			result = Component.text("Arena finished: Draw " + primaryTeamRoundWins + " - " + secondaryTeamRoundWins)
				.color(NamedTextColor.WHITE);
		}

		Bukkit.broadcast(result);
		Game.stopCurrentGame();
	}

	private void cancelRoundTasks() {
		preparationTask = Utility.cancelTask(preparationTask);
		roundCheckTask = Utility.cancelTask(roundCheckTask);
	}

	public void teleportArenaPlayersForRound() {
		if (isWaveMode()) {
			teleportWavePlayers();
			return;
		}

		teleportTeamsToRoundSpawns();
	}

	private void teleportWavePlayers() {
		Location center = areaController.getCenter();
		int radius = Math.max(4, areaController.getAreaDiameter() / 6);

		for (Player player : getArenaPlayers()) {
			Location spawn = areaController.constrain(center.clone().add(
				new Vector(
					ThreadLocalRandom.current().nextDouble(-radius, radius),
					0,
					ThreadLocalRandom.current().nextDouble(-radius, radius)
				)
			));
			moveToHighestGround(spawn);
			player.teleport(spawn);
		}
	}

	private void teleportTeamsToRoundSpawns() {
		Location center = areaController.getCenter();
		int radius = Math.max(8, areaController.getAreaDiameter() / 4);

		Location primarySpawn = areaController.constrain(center.clone().add(-radius, 0, 0));
		Location secondarySpawn = areaController.constrain(center.clone().add(radius, 0, 0));

		moveToHighestGround(primarySpawn);
		moveToHighestGround(secondarySpawn);

		for (Player player : teamController.getCombatPlayers(0)) {
			Location target = primarySpawn.clone();
			target.setDirection(secondarySpawn.toVector().subtract(primarySpawn.toVector()));
			player.teleport(target);
		}
		for (Player player : teamController.getCombatPlayers(1)) {
			Location target = secondarySpawn.clone();
			target.setDirection(primarySpawn.toVector().subtract(secondarySpawn.toVector()));
			player.teleport(target);
		}
	}

	public void moveToHighestGround(Location location) {
		World world = location.getWorld();
		if (world == null) return;
		location.setY(world.getHighestBlockYAt(location) + 1.0);
	}

	public void applyArenaCombatLoadout(Player player) {
		loadoutService.applyCombatLoadout(player);
	}

	public boolean isArenaPlayer(Player player) {
		return teamController.getPlayers().contains(player);
	}

	public Set<Player> getArenaPlayers() {
		return teamController.getArenaPlayers(teamController);
	}

	public List<Player> getAliveArenaPlayers() {
		return teamController.getAliveArenaPlayers(teamController);
	}

	@Nullable
	public Player pickRandomArenaPlayer() {
		List<Player> alive = getAliveArenaPlayers();

		if (alive.isEmpty()) return null;
		return alive.get(ThreadLocalRandom.current().nextInt(alive.size()));
	}

	private Location pickGameBaseLocation() {
		Player candidate = getArenaPlayers().stream().findFirst().orElse(null);
		if (candidate == null) {
			candidate = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
		}
		if (candidate == null) {
			throw new IllegalStateException("No players available to setup arena location");
		}

		return candidate.getLocation();
	}

	private record TeamSelection(Set<Player> primaryTeam, Set<Player> secondaryTeam) { }

	public static class Builder extends Game.Builder {
		private static final String allOption = "all";
		private static final List<String> teamPaths = List.of(primaryTeamPath, secondaryTeamPath);

		public static final List<String> areaOptions = Arrays.stream(WorldBorderAreaOption.values())
			.map(WorldBorderAreaOption::name)
			.collect(Collectors.toList());
		public static final List<String> locationOptions = Arrays.stream(WorldBorderLocationOption.values())
			.map(WorldBorderLocationOption::name)
			.collect(Collectors.toList());
		public static final List<String> modeOptions = ArenaModeOption.getOptions();
		public static final List<String> joinOptions = GameJoinOption.getOptions();

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Arena")
				.color(NamedTextColor.WHITE)
				.decoration(TextDecoration.BOLD, true);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Rounds based PvP arena with role repick between rounds.");
		}

		public Set<String> getTeamNames(int teamIndex) {
			String path = getTeamPath(teamIndex);
			return new HashSet<>(getPlugin().getConfig().getStringList(path));
		}

		public void setTeamNames(int teamIndex, Set<String> players) {
			String path = getTeamPath(teamIndex);
			List<String> value = players == null ? null : new ArrayList<>(players);
			saveConfigValue(path, value);
		}

		public ArenaModeOption getMode() {
			String value = getPlugin().getConfig().getString(modePath);
			return ArenaModeOption.fromConfig(value, ArenaModeOption.duel);
		}

		public void setMode(ArenaModeOption mode) {
			saveConfigValue(modePath, mode == null ? null : mode.getId());
		}

		public int getRounds() {
			return Math.max(1, getPlugin().getConfig().getInt(roundsPath, 3));
		}

		public void setRounds(int rounds) {
			saveConfigValue(roundsPath, Math.max(1, rounds));
		}

		public WorldBorderAreaOption getArea() {
			return readEnum(areaPath, WorldBorderAreaOption.class, WorldBorderAreaOption.small);
		}

		public void setArea(WorldBorderAreaOption area) {
			saveEnumValue(areaPath, area);
		}

		public WorldBorderLocationOption getLocation() {
			return readEnum(locationPath, WorldBorderLocationOption.class, WorldBorderLocationOption.here);
		}

		public void setLocation(WorldBorderLocationOption location) {
			saveEnumValue(locationPath, location);
		}

		public GameJoinOption getJoinOption() {
			return readEnum(joinPath, GameJoinOption.class, GameJoinOption.none);
		}

		public void setJoinOption(GameJoinOption join) {
			saveEnumValue(joinPath, join);
		}

		private void saveConfigValue(String path, Object value) {
			getPlugin().getConfig().set(path, value);
			getPlugin().saveConfig();
		}

		private void saveEnumValue(String path, Enum<?> value) {
			saveConfigValue(path, value == null ? null : value.name());
		}

			public TeamSelection resolveTeamSelection() {
			Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
			ArenaModeOption mode = getMode();

			validateOnlinePlayersCount(onlinePlayers, mode);

			Set<Player> configuredPrimary = getChosenTeam(0);
			Set<Player> configuredSecondary = getChosenTeam(1);

			if (configuredPrimary == null) configuredPrimary = new HashSet<>();
			if (configuredSecondary == null) configuredSecondary = new HashSet<>();

			configuredPrimary.retainAll(onlinePlayers);
			configuredSecondary.retainAll(onlinePlayers);
			configuredSecondary.removeAll(configuredPrimary);

			if (mode.isWaves()) return resolveWaveTeamSelection(onlinePlayers, configuredPrimary);
			if (mode.isDuel()) return resolveDuelTeamSelection(onlinePlayers, configuredPrimary, configuredSecondary);

			return resolveMultiTeamSelection(onlinePlayers, configuredPrimary, configuredSecondary);
		}

		private void validateOnlinePlayersCount(Set<Player> onlinePlayers, ArenaModeOption mode) {
			if (mode.isWaves()) {
				if (onlinePlayers.isEmpty()) {
					throw new IllegalArgumentException("At least 1 online player is required for Arena waves");
				}
			} else if (onlinePlayers.size() < 2) {
				throw new IllegalArgumentException("At least 2 online players are required for Arena");
			}
		}

		private TeamSelection resolveWaveTeamSelection(Set<Player> onlinePlayers, Set<Player> configuredPrimary) {
			Set<Player> primaryTeam = configuredPrimary.isEmpty() ? new HashSet<>(onlinePlayers) : new HashSet<>(configuredPrimary);
			if (primaryTeam.isEmpty()) throw new IllegalArgumentException("No players selected for Arena waves");

			return new TeamSelection(primaryTeam, Set.of());
		}

		private TeamSelection resolveDuelTeamSelection(Set<Player> onlinePlayers, Set<Player> configuredPrimary, Set<Player> configuredSecondary) {
			Player p1 = chooseOne(configuredPrimary.isEmpty() ? onlinePlayers : configuredPrimary, null);
			Player p2 = chooseOne(configuredSecondary.isEmpty() ? onlinePlayers : configuredSecondary, p1);
			if (p1 == null || p2 == null) throw new IllegalArgumentException("Could not resolve duel players for Arena");

			return new TeamSelection(Set.of(p1), Set.of(p2));
		}

		private TeamSelection resolveMultiTeamSelection(Set<Player> onlinePlayers, Set<Player> configuredPrimary, Set<Player> configuredSecondary) {
			Set<Player> primaryTeam = new HashSet<>(configuredPrimary);
			Set<Player> secondaryTeam = new HashSet<>(configuredSecondary);
			Set<Player> remaining = new HashSet<>(onlinePlayers);

			remaining.removeAll(primaryTeam);
			remaining.removeAll(secondaryTeam);

			GameTeamController.distributePlayersInTeams(
				remaining,
				List.of(primaryTeam, secondaryTeam),
				true,
				primaryTeam.isEmpty() && secondaryTeam.isEmpty()
			);

			if (primaryTeam.isEmpty() || secondaryTeam.isEmpty()) {
				throw new IllegalArgumentException("Both Arena teams must contain at least one online player");
			}
			return new TeamSelection(primaryTeam, secondaryTeam);
		}

		private String getTeamPath(int teamIndex) {
			if (teamIndex < 0 || teamIndex >= teamPaths.size()) {
				throw new IllegalArgumentException("Invalid team index: " + teamIndex);
			}
			return teamPaths.get(teamIndex);
		}

		private <T extends Enum<T>> T readEnum(String path, Class<T> enumType, T fallback) {
			String value = getPlugin().getConfig().getString(path);
			if (value == null) {
				return fallback;
			}

			for (T constant : enumType.getEnumConstants()) {
				if (constant.name().equalsIgnoreCase(value)) {
					return constant;
				}
			}

			return fallback;
		}

		private <T extends Enum<T>> java.util.Optional<T> parseEnumIgnoreCase(Class<T> enumType, String value) {
			if (value == null) return java.util.Optional.empty();

			for (T constant : enumType.getEnumConstants()) {
				if (constant.name().equalsIgnoreCase(value)) {
					return java.util.Optional.of(constant);
				}
			}

			return java.util.Optional.empty();
		}

		@Nullable
		private Set<Player> getChosenTeam(int teamIndex) {
			Set<String> names = getTeamNames(teamIndex);
			if (names.isEmpty()) return null;

			return names.stream()
				.map(Bukkit::getPlayerExact)
				.filter(p -> p != null && p.isOnline())
				.collect(Collectors.toSet());
		}

		@Nullable
		private Player chooseOne(Set<Player> candidates, @Nullable Player excluded) {
			List<Player> filtered = candidates.stream()
				.filter(Player::isOnline)
				.filter(p -> excluded == null || !p.equals(excluded))
				.collect(Collectors.toList());

			if (filtered.isEmpty()) return null;

			return filtered.get(ThreadLocalRandom.current().nextInt(filtered.size()));
		}

		private String teamNameToDisplay(Set<String> names) {
			return names.stream().sorted().collect(Collectors.joining(" "));
		}

		@Override
		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game " + getId() + " config <config> [value]");

			for (ArenaGameConfigs config : ArenaGameConfigs.values()) {
				usage.append("\n  ").append(config.name()).append(" ").append(config.getUsage());
			}

			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) return false;

			ArenaGameConfigs option = parseEnumIgnoreCase(ArenaGameConfigs.class, args[0]).orElse(null);
			if (option == null) return false;

			return handleConfigsCommand(sender, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, String[] args, ArenaGameConfigs config) {
			switch (config) {
				case team1:
					if (args.length == 0) {
						sender.sendMessage(teamNameToDisplay(getTeamNames(0)));
						return true;
					}
					if (allOption.equalsIgnoreCase(args[0])) {
						setTeamNames(0, null);
						sender.sendMessage("Arena team1 reset to auto");
						return true;
					}
					setTeamNames(0, new HashSet<>(Arrays.asList(args)));
					sender.sendMessage("Arena team1 set to " + teamNameToDisplay(getTeamNames(0)));
					return true;

				case team2:
					if (args.length == 0) {
						sender.sendMessage(teamNameToDisplay(getTeamNames(1)));
						return true;
					}
					if (allOption.equalsIgnoreCase(args[0])) {
						setTeamNames(1, null);
						sender.sendMessage("Arena team2 reset to auto");
						return true;
					}
					setTeamNames(1, new HashSet<>(Arrays.asList(args)));
					sender.sendMessage("Arena team2 set to " + teamNameToDisplay(getTeamNames(1)));
					return true;

				case mode:
					if (args.length == 0) {
						sender.sendMessage(getMode().getId());
						return true;
					}

					ArenaModeOption mode = ArenaModeOption.resolve(args[0]).orElse(null);

					if (mode == null) return false;

					setMode(mode);
					sender.sendMessage("Arena mode set to " + mode.getId());

					return true;

				case rounds:
					if (args.length == 0) {
						sender.sendMessage(Integer.toString(getRounds()));
						return true;
					}

					int rounds;

					try {
						rounds = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						return false;
					}

					setRounds(rounds);
					sender.sendMessage("Arena rounds set to " + getRounds());

					return true;

				case area:
					if (args.length == 0) {
						sender.sendMessage(getArea().name());
						return true;
					}

					WorldBorderAreaOption area = parseEnumIgnoreCase(WorldBorderAreaOption.class, args[0]).orElse(null);

					if (area == null) return false;

					setArea(area);
					sender.sendMessage("Arena area set to " + area.name());

					return true;

				case location:
					if (args.length == 0) {
						sender.sendMessage(getLocation().name());
						return true;
					}

					WorldBorderLocationOption location = parseEnumIgnoreCase(WorldBorderLocationOption.class, args[0]).orElse(null);

					if (location == null) return false;

					setLocation(location);
					sender.sendMessage("Arena location set to " + location.name());

					return true;

				case join:
					if (args.length == 0) {
						sender.sendMessage(getJoinOption().name());
						return true;
					}


					GameJoinOption join = parseEnumIgnoreCase(GameJoinOption.class, args[0]).orElse(null);

					if (join == null) return false;

					setJoinOption(join);
					sender.sendMessage("Arena join option set to " + join.name());

					return true;
			}

			return false;
		}

		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(ArenaGameConfigs.values())
					.map(ArenaGameConfigs::name)
					.collect(Collectors.toList());
			}

			ArenaGameConfigs config = parseEnumIgnoreCase(ArenaGameConfigs.class, args[0]).orElse(null);
			if (config == null) return null;

			return handleConfigsTabComplete(Arrays.copyOfRange(args, 1, args.length), config);
		}

		private List<String> handleConfigsTabComplete(String[] args, ArenaGameConfigs config) {
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();

			switch (config) {
				case team1:
				case team2:
					if (args.length == 1) {
						allPlayers.add(allOption);
					}
					return allPlayers;
				case mode:
					if (args.length == 1) return modeOptions;
					break;
				// case rounds:
				// 	if (args.length == 1) return List.of("1", "3", "5", "7");
				// 	break;
				case area:
					if (args.length == 1) return areaOptions;
					break;
				case location:
					if (args.length == 1) return locationOptions;
					break;
				case join:
					if (args.length == 1) return joinOptions;
					break;
			}

			return null;
		}

		@Override
		public ArenaGame build() {
			if (getMode().isWaves()) {
				return new ArenaWaveController(this);
			}
			return new ArenaGame(this);
		}
	}
}
