package fr.ludos.game.arena;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.EnumUtils;
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
import fr.ludos.game.GameJoinOption;
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

	public static final String teamAKey = "teamA";
	public static final String teamAPath = ID + '.' + teamAKey;
	public static final String teamBKey = "teamB";
	public static final String teamBPath = ID + '.' + teamBKey;
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

	private final Scoreboard scoreboard;
	private final ArenaTeamController teamController;
	private final WorldBorderAreaController areaController;
	private final Builder builder;
	private final ArenaWaveController waveController;
	private final ArenaLoadoutService loadoutService;

	private int currentRound = 0;
	private int teamARoundWins = 0;
	private int teamBRoundWins = 0;
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
		this.teamController = new ArenaTeamController(this, selectedTeams.teamA(), selectedTeams.teamB(), builder.getJoinOption());
		this.areaController = new WorldBorderAreaController(this, builder.getLocation(), builder.getArea());
		this.waveController = new ArenaWaveController(this);
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
		world.setTime(1000);
		world.setStorm(false);
		world.setThundering(false);

		if (isWaveMode()) {
			waveController.start();
		}
		else {
			startNextRound();
		}
		Bukkit.broadcast(Component.text("Arena game started"));
	}

	@Override
	protected void onGameStop() {
		if (waveController.isStarted()) {
			waveController.stop();
		}
		cancelRoundTasks();
		areaController.resetBorder();
		Bukkit.broadcast(Component.text("Arena game ended"));
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		return isArenaPlayer(player);
	}

	public boolean isWaveMode() {
		return builder.getMode() == ArenaModeOption.waves;
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
		if (event.getTo() == null) return;

		if (
			event.getFrom().getX() == event.getTo().getX() &&
			event.getFrom().getY() == event.getTo().getY() &&
			event.getFrom().getZ() == event.getTo().getZ()
		) return;

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (isWaveMode()) return;
		if (!preparationPhase) return;
		if (!(event.getEntity() instanceof Player player)) return;
		if (!isArenaPlayer(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (isWaveMode()) return;
		if (!(event.getEntity() instanceof Player player)) return;
		if (!isArenaPlayer(player)) return;
		event.setCancelled(true);
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

		teamController.resetRoundPlayers();
		teleportArenaPlayersForRound();

		for (Player player : getArenaPlayers()) {
			resetArenaPlayerState(player);
			player.getInventory().clear();
			player.getInventory().addItem(Ludos.createGuidebook());
		}

		startPreparationCountdown();
	}

	private void startPreparationCountdown() {
		if (preparationTask != null) {
			preparationTask.cancel();
			preparationTask = null;
		}

		preparationTask = new BukkitRunnable() {
			private int ticksLeft = PREP_TICKS;

			@Override
			public void run() {
				int secondsLeft = Math.max(0, ticksLeft / 20);
				for (Player player : getArenaPlayers()) {
					player.showTitle(Title.title(
						Component.text("Round " + currentRound).color(NamedTextColor.WHITE),
						Component.text("Fight starts in " + secondsLeft + "s").color(NamedTextColor.WHITE),
						Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(750), Duration.ofMillis(100))
					));
				}

				if (ticksLeft <= 0) {
					cancel();
					preparationTask = null;
					startCombatPhase();
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

		if (roundCheckTask != null) {
			roundCheckTask.cancel();
			roundCheckTask = null;
		}
		roundCheckTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::evaluateRoundState, 0L, ROUND_CHECK_PERIOD_TICKS);
	}

	private void evaluateRoundState() {
		int aliveA = teamController.getAliveTeamAPlayers().size();
		int aliveB = teamController.getAliveTeamBPlayers().size();

		if (aliveA > 0 && aliveB > 0) return;

		if (roundCheckTask != null) {
			roundCheckTask.cancel();
			roundCheckTask = null;
		}

		if (aliveA > aliveB) {
			teamARoundWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Team A").color(NamedTextColor.BLUE));
		}
		else if (aliveB > aliveA) {
			teamBRoundWins++;
			Bukkit.broadcast(Component.text("Round " + currentRound + " won by Team B").color(NamedTextColor.RED));
		}
		else {
			Bukkit.broadcast(Component.text("Round " + currentRound + " is a draw").color(NamedTextColor.WHITE));
		}

		Bukkit.getScheduler().runTaskLater(getPlugin(), this::startNextRound, 20L);
	}

	private void endArenaMatch() {
		cancelRoundTasks();

		Component result;
		if (teamARoundWins > teamBRoundWins) {
			result = Component.text("Arena finished: Team A wins " + teamARoundWins + " - " + teamBRoundWins)
				.color(NamedTextColor.BLUE);
		}
		else if (teamBRoundWins > teamARoundWins) {
			result = Component.text("Arena finished: Team B wins " + teamBRoundWins + " - " + teamARoundWins)
				.color(NamedTextColor.RED);
		}
		else {
			result = Component.text("Arena finished: Draw " + teamARoundWins + " - " + teamBRoundWins)
				.color(NamedTextColor.WHITE);
		}

		Bukkit.broadcast(result);
		Game.stopCurrentGame();
	}

	private void cancelRoundTasks() {
		if (preparationTask != null) {
			preparationTask.cancel();
			preparationTask = null;
		}
		if (roundCheckTask != null) {
			roundCheckTask.cancel();
			roundCheckTask = null;
		}
	}

	public void resetArenaPlayerState(Player player) {
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
		player.setFoodLevel(20);
		player.setSaturation(20f);
		player.setFireTicks(0);
		player.setGameMode(GameMode.SURVIVAL);
		player.setVelocity(new Vector(0, 0, 0));
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

		Location spawnA = areaController.constrain(center.clone().add(-radius, 0, 0));
		Location spawnB = areaController.constrain(center.clone().add(radius, 0, 0));

		moveToHighestGround(spawnA);
		moveToHighestGround(spawnB);

		for (Player player : teamController.getTeamAPlayers()) {
			Location target = spawnA.clone();
			target.setDirection(spawnB.toVector().subtract(spawnA.toVector()));
			player.teleport(target);
		}
		for (Player player : teamController.getTeamBPlayers()) {
			Location target = spawnB.clone();
			target.setDirection(spawnA.toVector().subtract(spawnB.toVector()));
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
		return teamController.getTeamAPlayers().contains(player) || teamController.getTeamBPlayers().contains(player);
	}

	public Set<Player> getArenaPlayers() {
		Set<Player> players = new HashSet<>();
		players.addAll(teamController.getTeamAPlayers());
		players.addAll(teamController.getTeamBPlayers());
		return players;
	}

	@Nullable
	public Player pickRandomArenaPlayer() {
		List<Player> alive = getArenaPlayers().stream()
			.filter(Player::isOnline)
			.filter(p -> !p.isDead())
			.filter(p -> p.getGameMode() == GameMode.SURVIVAL)
			.toList();

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

	private record TeamSelection(Set<Player> teamA, Set<Player> teamB) { }

	public static class Builder extends Game.Builder {
		private static final String allOption = "all";

		public static final List<String> areaOptions = Arrays.stream(WorldBorderAreaOption.values())
			.map(WorldBorderAreaOption::name)
			.collect(Collectors.toList());
		public static final List<String> locationOptions = Arrays.stream(WorldBorderLocationOption.values())
			.map(WorldBorderLocationOption::name)
			.collect(Collectors.toList());
		public static final List<String> modeOptions = Arrays.stream(ArenaModeOption.values())
			.map(ArenaModeOption::name)
			.collect(Collectors.toList());
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

		public Set<String> getTeamANames() {
			return getPlugin().getConfig().getStringList(teamAPath).stream().collect(Collectors.toSet());
		}

		public void setTeamANames(Set<String> players) {
			List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
			getPlugin().getConfig().set(teamAPath, value);
			getPlugin().saveConfig();
		}

		public Set<String> getTeamBNames() {
			return getPlugin().getConfig().getStringList(teamBPath).stream().collect(Collectors.toSet());
		}

		public void setTeamBNames(Set<String> players) {
			List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
			getPlugin().getConfig().set(teamBPath, value);
			getPlugin().saveConfig();
		}

		public ArenaModeOption getMode() {
			String modeString = getPlugin().getConfig().getString(modePath);
			return Arrays.stream(ArenaModeOption.values())
				.filter(o -> o.name().equalsIgnoreCase(modeString))
				.findFirst()
				.orElse(ArenaModeOption.duel);
		}

		public void setMode(ArenaModeOption mode) {
			String value = mode == null ? null : mode.name();
			getPlugin().getConfig().set(modePath, value);
			getPlugin().saveConfig();
		}

		public int getRounds() {
			return Math.max(1, getPlugin().getConfig().getInt(roundsPath, 3));
		}

		public void setRounds(int rounds) {
			getPlugin().getConfig().set(roundsPath, Math.max(1, rounds));
			getPlugin().saveConfig();
		}

		public WorldBorderAreaOption getArea() {
			String areaString = getPlugin().getConfig().getString(areaPath);
			return Arrays.stream(WorldBorderAreaOption.values())
				.filter(o -> o.name().equalsIgnoreCase(areaString))
				.findFirst()
				.orElse(WorldBorderAreaOption.small);
		}

		public void setArea(WorldBorderAreaOption area) {
			String value = area == null ? null : area.name();
			getPlugin().getConfig().set(areaPath, value);
			getPlugin().saveConfig();
		}

		public WorldBorderLocationOption getLocation() {
			String locationString = getPlugin().getConfig().getString(locationPath);
			return Arrays.stream(WorldBorderLocationOption.values())
				.filter(o -> o.name().equalsIgnoreCase(locationString))
				.findFirst()
				.orElse(WorldBorderLocationOption.here);
		}

		public void setLocation(WorldBorderLocationOption location) {
			String value = location == null ? null : location.name();
			getPlugin().getConfig().set(locationPath, value);
			getPlugin().saveConfig();
		}

		public GameJoinOption getJoinOption() {
			String joinString = getPlugin().getConfig().getString(joinPath);
			return Arrays.stream(GameJoinOption.values())
				.filter(o -> o.name().equalsIgnoreCase(joinString))
				.findFirst()
				.orElse(GameJoinOption.none);
		}

		public void setJoinOption(GameJoinOption join) {
			String value = join == null ? null : join.name();
			getPlugin().getConfig().set(joinPath, value);
			getPlugin().saveConfig();
		}

		@Nullable
		public Set<Player> getChosenTeamA() {
			Set<String> names = getTeamANames();
			if (names.isEmpty()) return null;

			return names.stream()
				.map(Bukkit::getPlayerExact)
				.filter(p -> p != null && p.isOnline())
				.collect(Collectors.toSet());
		}

		@Nullable
		public Set<Player> getChosenTeamB() {
			Set<String> names = getTeamBNames();
			if (names.isEmpty()) return null;

			return names.stream()
				.map(Bukkit::getPlayerExact)
				.filter(p -> p != null && p.isOnline())
				.collect(Collectors.toSet());
		}

		public TeamSelection resolveTeamSelection() {
			Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
			ArenaModeOption mode = getMode();
			if (mode == ArenaModeOption.waves) {
				if (onlinePlayers.isEmpty()) {
					throw new IllegalArgumentException("At least 1 online player is required for Arena waves");
				}
			}
			else if (onlinePlayers.size() < 2) {
				throw new IllegalArgumentException("At least 2 online players are required for Arena");
			}

			Set<Player> configuredA = getChosenTeamA();
			Set<Player> configuredB = getChosenTeamB();

			if (configuredA == null) configuredA = new HashSet<>();
			if (configuredB == null) configuredB = new HashSet<>();

			configuredA.retainAll(onlinePlayers);
			configuredB.retainAll(onlinePlayers);
			configuredB.removeAll(configuredA);

			if (mode == ArenaModeOption.waves) {
				Set<Player> teamA = configuredA.isEmpty() ? new HashSet<>(onlinePlayers) : new HashSet<>(configuredA);
				if (teamA.isEmpty()) {
					throw new IllegalArgumentException("No players selected for Arena waves");
				}

				return new TeamSelection(teamA, Set.of());
			}

			if (mode == ArenaModeOption.duel) {
				Player teamAPlayer = chooseOne(configuredA.isEmpty() ? onlinePlayers : configuredA, null);
				Player teamBPlayer = chooseOne(configuredB.isEmpty() ? onlinePlayers : configuredB, teamAPlayer);

				if (teamAPlayer == null || teamBPlayer == null) {
					throw new IllegalArgumentException("Could not resolve duel players for Arena");
				}

				Set<Player> teamA = new HashSet<>();
				teamA.add(teamAPlayer);
				Set<Player> teamB = new HashSet<>();
				teamB.add(teamBPlayer);
				return new TeamSelection(teamA, teamB);
			}

			Set<Player> teamA = new HashSet<>(configuredA);
			Set<Player> teamB = new HashSet<>(configuredB);
			Set<Player> remaining = new HashSet<>(onlinePlayers);
			remaining.removeAll(teamA);
			remaining.removeAll(teamB);

			if (teamA.isEmpty() && teamB.isEmpty()) {
				List<Player> all = new ArrayList<>(remaining);
				Collections.shuffle(all);
				for (int i = 0; i < all.size(); i++) {
					if (i % 2 == 0) teamA.add(all.get(i));
					else teamB.add(all.get(i));
				}
			}
			else {
				for (Player player : remaining) {
					if (teamA.size() <= teamB.size()) teamA.add(player);
					else teamB.add(player);
				}
			}

			if (teamA.isEmpty() || teamB.isEmpty()) {
				throw new IllegalArgumentException("Both Arena teams must contain at least one online player");
			}

			return new TeamSelection(teamA, teamB);
		}

		@Nullable
		private Player chooseOne(Set<Player> candidates, @Nullable Player excluded) {
			List<Player> filtered = candidates.stream()
				.filter(Player::isOnline)
				.filter(p -> excluded == null || !p.equals(excluded))
				.collect(Collectors.toList());
			if (filtered.isEmpty()) return null;

			Collections.shuffle(filtered);
			return filtered.get(0);
		}

		private String namesToDisplay(Set<String> names) {
			if (names == null || names.isEmpty()) return "Auto";
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

			String arg = args[0];
			ArenaGameConfigs option = Arrays.stream(ArenaGameConfigs.values())
				.filter(o -> o.name().equalsIgnoreCase(arg))
				.findFirst()
				.orElse(null);
			if (option == null) return false;

			return handleConfigsCommand(sender, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, String[] args, ArenaGameConfigs config) {
			switch (config) {
				case teamA:
					if (args.length == 0) {
						sender.sendMessage(namesToDisplay(getTeamANames()));
						return true;
					}
					if (allOption.equalsIgnoreCase(args[0])) {
						setTeamANames(null);
						sender.sendMessage("Arena Team A reset to auto");
						return true;
					}
					setTeamANames(new HashSet<>(Arrays.asList(args)));
					sender.sendMessage("Arena Team A set to " + namesToDisplay(getTeamANames()));
					return true;

				case teamB:
					if (args.length == 0) {
						sender.sendMessage(namesToDisplay(getTeamBNames()));
						return true;
					}
					if (allOption.equalsIgnoreCase(args[0])) {
						setTeamBNames(null);
						sender.sendMessage("Arena Team B reset to auto");
						return true;
					}
					setTeamBNames(new HashSet<>(Arrays.asList(args)));
					sender.sendMessage("Arena Team B set to " + namesToDisplay(getTeamBNames()));
					return true;

				case mode:
					if (args.length == 0) {
						sender.sendMessage(getMode().name());
						return true;
					}
					ArenaModeOption mode = Arrays.stream(ArenaModeOption.values())
						.filter(m -> m.name().equalsIgnoreCase(args[0]))
						.findFirst().orElse(null);
					if (mode == null) return false;
					setMode(mode);
					sender.sendMessage("Arena mode set to " + mode.name());
					return true;

				case rounds:
					if (args.length == 0) {
						sender.sendMessage(Integer.toString(getRounds()));
						return true;
					}
					int rounds;
					try {
						rounds = Integer.parseInt(args[0]);
					}
					catch (NumberFormatException e) {
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
					WorldBorderAreaOption area = Arrays.stream(WorldBorderAreaOption.values())
						.filter(a -> a.name().equalsIgnoreCase(args[0]))
						.findFirst().orElse(null);
					if (area == null) return false;
					setArea(area);
					sender.sendMessage("Arena area set to " + area.name());
					return true;

				case location:
					if (args.length == 0) {
						sender.sendMessage(getLocation().name());
						return true;
					}
					WorldBorderLocationOption location = Arrays.stream(WorldBorderLocationOption.values())
						.filter(l -> l.name().equalsIgnoreCase(args[0]))
						.findFirst().orElse(null);
					if (location == null) return false;
					setLocation(location);
					sender.sendMessage("Arena location set to " + location.name());
					return true;

				case join:
					if (args.length == 0) {
						sender.sendMessage(getJoinOption().name());
						return true;
					}
					GameJoinOption join = Arrays.stream(GameJoinOption.values())
						.filter(j -> j.name().equalsIgnoreCase(args[0]))
						.findFirst().orElse(null);
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

			String arg = args[0];
			if (!EnumUtils.isValidEnum(ArenaGameConfigs.class, arg)) return null;
			ArenaGameConfigs config = ArenaGameConfigs.valueOf(arg);

			return handleConfigsTabComplete(Arrays.copyOfRange(args, 1, args.length), config);
		}

		private List<String> handleConfigsTabComplete(String[] args, ArenaGameConfigs config) {
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();

			switch (config) {
				case teamA:
				case teamB:
					if (args.length == 1) {
						allPlayers.add(allOption);
					}
					return allPlayers;
				case mode:
					if (args.length == 1) return modeOptions;
					break;
				case rounds:
					if (args.length == 1) return List.of("1", "3", "5", "7");
					break;
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
			return new ArenaGame(this);
		}
	}
}
