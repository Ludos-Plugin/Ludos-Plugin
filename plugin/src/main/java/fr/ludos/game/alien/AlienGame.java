package fr.ludos.game.alien;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;
import fr.ludos.game.GameTeamController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class AlienGame extends Game {
	public static final String id = "alien";

	public static final String playersKey = "players";
	public static final String playersPath = id + '.' + playersKey;
	public static final String preyKey = "prey";
	public static final String preyPath = id + '.' + preyKey;

	public static final String areaKey = "area";
	public static final String areaPath = id + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = id + '.' + locationKey;
	public static final String revealKey = "reveal";
	public static final String revealPath = id + '.' + revealKey;

	public static final String borderWorldUUIDKey = "borderWorldUUID";
	public static final String borderWorldUUIDPath = id + '.' + borderWorldUUIDKey;
	public static final String borderLocationKey = "borderLocation";
	public static final String borderLocationPath = id + '.' + borderLocationKey;
	public static final String borderSizeKey = "borderSize";
	public static final String borderSizePath = id + '.' + borderSizeKey;

	public static final String spawnLocationKey = "spawnLocation";
	public static final String spawnLocationPath = id + '.' + spawnLocationKey;

	public static final String alienSpawnDistanceKey = "alienSpawnDistance";
	public static final String alienSpawnDistancePath = id + '.' + alienSpawnDistanceKey;

	private final Scoreboard scoreboard;
	private final AlienTeamController teamController;
	private final AlienAreaController areaController;
	private final AlienTimer timer;
	private final Builder builder;

	private AlienMonster alien;
	private AlienQuestManager questManager;

	private BukkitTask saturationTask;

	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	@Override
	public AlienTeamController getTeamController() {
		return this.teamController;
	}

	@Override
	public AlienTeamController getGameTeamController() {
		return this.teamController;
	}

	@Override
	public AlienAreaController getGameAreaController() {
		return this.areaController;
	}

	public AlienTimer getAlienTimer() {
		return this.timer;
	}

	public AlienMonster getAlien() {
		return alien;
	}

	public Builder getAlienBuilder() {
		return this.builder;
	}

	@Nullable
	public static UUID getBorderWorldUID(JavaPlugin plugin) {
		String value = plugin.getConfig().getString(borderWorldUUIDPath);
		if (value == null) {
			return null;
		}
		return UUID.fromString(value);
	}

	@Nullable
	public static Location getBorderLocation(JavaPlugin plugin) {
		return plugin.getConfig().getLocation(borderLocationPath);
	}

	public static double getBorderSize(JavaPlugin plugin) {
		return plugin.getConfig().getDouble(borderSizePath);
	}

	public static void setCachedBorder(World world, JavaPlugin plugin) {
		FileConfiguration config = plugin.getConfig();

		if (world == null) {
			config.set(borderWorldUUIDPath, null);
			config.set(borderLocationPath, null);
			config.set(borderSizePath, null);
			plugin.saveConfig();
			return;
		}

		WorldBorder border = world.getWorldBorder();
		config.set(borderWorldUUIDPath, world.getUID().toString());
		config.set(borderLocationPath, border.getCenter());
		config.set(borderSizePath, border.getSize());
		plugin.saveConfig();
	}

	public static void resetBorder(JavaPlugin plugin) {
		Location location = AlienGame.getBorderLocation(plugin);
		if (location == null) {
			return;
		}

		UUID worldUid = AlienGame.getBorderWorldUID(plugin);
		if (worldUid == null) {
			return;
		}

		World world = Bukkit.getWorld(worldUid);
		if (world == null) {
			return;
		}

		double size = AlienGame.getBorderSize(plugin);
		WorldBorder border = world.getWorldBorder();
		border.setCenter(location);
		border.setSize(size, 0);

		AlienGame.setCachedBorder(null, plugin);
	}

	protected AlienGame(Builder builder) {
		super(builder);

		this.builder = builder;
		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new AlienTeamController(this, builder.getChosenPlayers(), builder.getChosenPrey());
		this.areaController = new AlienAreaController(this, builder.getLocation());
		this.timer = new AlienTimer(this, builder.getReveal());
	}

	@Override
	protected void onGameInit() {
		Location base = builder.getSpawnLocation();
		if (base == null) {
			Player anchor = teamController.getSelectedPrey();
			if (anchor == null) {
				throw new IllegalStateException("No player available to initialize Alien game.");
			}
			base = anchor.getLocation();
		}

		areaController.setup(base);
	}

	@Override
	protected void onGameStart() {
		timer.start();

		Location teamSpawn = builder.getSpawnLocation();
		if (teamSpawn == null) {
			teamSpawn = areaController.getCenter();
		}
		teamSpawn = areaController.constrain(teamSpawn);

		Random rnd = new Random();
		int index = 0;
		for (Player player : teamController.getAllPlayers()) {
			Location tp = teamSpawn.clone();
			tp.setX(tp.getX() + (index % 2 == 0 ? 1 : -1) * rnd.nextInt(0, 2));
			tp.setZ(tp.getZ() + ((index / 2) % 2 == 0 ? 1 : -1) * rnd.nextInt(0, 2));
			tp.setY(tp.getWorld().getHighestBlockYAt(tp.getBlockX(), tp.getBlockZ()) + 1);

			player.teleport(tp);
			player.setBedSpawnLocation(tp, true);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			player.getInventory().clear();
			player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
			index++;
		}

		for (Player player : teamController.getAllPlayers()) {
			player.showTitle(Title.title(
					Component.text("The Alien tracks you"),
					Component.text("Survive together"),
					Title.Times.times(
							Duration.ofMillis(500),
							Duration.ofMillis(3500),
							Duration.ofMillis(1000))));
		}

		Location alienSpawnLocation = areaController.pickAlienSpawn(teamSpawn, builder.getAlienSpawnDistance());
		alien = AlienMonster.spawn(this, alienSpawnLocation);
		if (alien == null) {
			throw new IllegalStateException("Could not spawn alien.");
		}
		alien.startAI();

		questManager = new AlienQuestManager(this);
		questManager.start();

		teamSpawn.getWorld().setTime(18000);

		saturationTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : teamController.getAllPlayers()) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
			}
		}.runTaskTimer(getPlugin(), 400, 400);

		Bukkit.broadcast(Component.text("The Game of Alien started").color(NamedTextColor.RED));
	}

	@Override
	protected void onGameStop() {
		timer.stop();

		if (questManager != null) {
			questManager.stop();
			questManager = null;
		}

		if (alien != null) {
			alien.despawn();
			alien = null;
		}

		resetBorder(getPlugin());

		if (saturationTask != null) {
			saturationTask.cancel();
			saturationTask = null;
		}

		Bukkit.broadcast(Component.text("The Game of Alien ended").color(NamedTextColor.RED));
	}

	public void onAllQuestsCompleted() {
		if (alien != null) {
			alien.killInstantly();
		}

		for (Player player : teamController.getAllPlayers()) {
			player.showTitle(Title.title(
					Component.text("Victory").color(NamedTextColor.GREEN),
					Component.text("All quests completed"),
					Title.Times.times(
							Duration.ofMillis(300),
							Duration.ofMillis(3000),
							Duration.ofMillis(800))));
		}

		Bukkit.broadcast(
				Component.text("All quests completed! The Alien has been destroyed!").color(NamedTextColor.GREEN));

		new BukkitRunnable() {
			@Override
			public void run() {
				Game.stopCurrentGame();
			}
		}.runTaskLater(getPlugin(), 40L);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!isStarted()) {
			return;
		}

		Player player = event.getPlayer();
		if (!teamController.getAllPlayers().contains(player)) {
			player.setGameMode(GameMode.SPECTATOR);
			player.teleport(areaController.getCenter());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!isStarted()) {
			return;
		}

		if (teamController.getLivingPlayers().isEmpty()) {
			Bukkit.broadcast(Component.text("All players left the Alien game."));
			Game.stopCurrentGame();
		}
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		return true;
	}

	public static class Builder extends Game.Builder {
		private static final String allOption = "all";
		private static final String randomOption = "random";

		public static final List<String> locationOptions = Arrays.stream(AlienLocationOptions.values())
				.map(AlienLocationOptions::toString)
				.collect(Collectors.toList());

		public static final List<String> revealOptions = Arrays.stream(AlienRevealOptions.values())
				.map(AlienRevealOptions::toString)
				.collect(Collectors.toList());

		public String getPreyName() {
			return getPlugin().getConfig().getString(preyPath);
		}

		public void setPreyName(String prey) {
			getPlugin().getConfig().set(preyPath, prey);
			getPlugin().saveConfig();
		}

		public Set<String> getPlayerNames() {
			return getPlugin().getConfig().getStringList(playersPath).stream().collect(Collectors.toSet());
		}

		public void setPlayerNames(Set<String> players) {
			List<String> value = players == null ? null : players.stream().toList();
			getPlugin().getConfig().set(playersPath, value);
			getPlugin().saveConfig();
		}

		public AlienLocationOptions getLocation() {
			String locationString = getPlugin().getConfig().getString(locationPath);
			return Arrays.stream(AlienLocationOptions.values())
					.filter(o -> o.toString().equals(locationString))
					.findFirst()
					.orElse(AlienLocationOptions.here);
		}

		public void setLocation(AlienLocationOptions location) {
			getPlugin().getConfig().set(locationPath, location == null ? null : location.toString());
			getPlugin().saveConfig();
		}

		public AlienRevealOptions getReveal() {
			String revealString = getPlugin().getConfig().getString(revealPath);
			return Arrays.stream(AlienRevealOptions.values())
					.filter(o -> o.toString().equals(revealString))
					.findFirst()
					.orElse(AlienRevealOptions.three_minutes);
		}

		public void setReveal(AlienRevealOptions reveal) {
			getPlugin().getConfig().set(revealPath, reveal == null ? null : reveal.toString());
			getPlugin().saveConfig();
		}

		public Location getSpawnLocation() {
			return getPlugin().getConfig().getLocation(spawnLocationPath);
		}

		public void setSpawnLocation(Location loc) {
			getPlugin().getConfig().set(spawnLocationPath, loc);
			getPlugin().saveConfig();
		}

		public int getAlienSpawnDistance() {
			return Math.max(1, getPlugin().getConfig().getInt(alienSpawnDistancePath, 10));
		}

		public void setAlienSpawnDistance(int distance) {
			getPlugin().getConfig().set(alienSpawnDistancePath, Math.max(1, distance));
			getPlugin().saveConfig();
		}

		public Builder(JavaPlugin plugin) {
			super(plugin);
			resetBorder(plugin);
		}

		@Nullable
		public Set<Player> getChosenPlayers() {
			Set<String> playerNames = this.getPlayerNames();
			if (playerNames.isEmpty()) {
				return null;
			}

			return new HashSet<>(playerNames.stream()
					.map(Bukkit::getPlayerExact)
					.filter(p -> p != null)
					.collect(Collectors.toSet()));
		}

		@Nullable
		public Player getChosenPrey() {
			String preyName = this.getPreyName();
			if (preyName == null) {
				return null;
			}
			return Bukkit.getPlayerExact(preyName);
		}

		public String getPlayersString() {
			Set<String> playerNames = this.getPlayerNames();
			return playerNames.isEmpty() ? "All" : playerNames.stream().collect(Collectors.joining(" "));
		}

		public String getPreyString() {
			String preyName = this.getPreyName();
			return preyName == null ? "Random" : preyName;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Alien").color(NamedTextColor.RED);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Survive together against an invincible Alien.\n" +
					"Complete all quests to destroy it and win.");
		}

		@Override
		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game " + getId() + " config <config> [value]");
			for (AlienGameConfigs config : AlienGameConfigs.values()) {
				usage.append("\n  ").append(config.toString()).append(" ").append(config.getUsage());
			}
			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				return false;
			}

			String arg = args[0];
			AlienGameConfigs option = Arrays.stream(AlienGameConfigs.values())
					.filter(o -> o.toString().equals(arg))
					.findFirst()
					.orElse(null);

			if (option == null) {
				return false;
			}

			return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args,
				AlienGameConfigs config) {
			switch (config) {
				case players:
					if (args.length == 0) {
						sender.sendMessage(getPlayersString());
						return true;
					}
					if (args[0].equalsIgnoreCase(allOption)) {
						setPlayerNames(null);
						sender.sendMessage("All players included in the game");
						return true;
					}
					setPlayerNames(new HashSet<>(Arrays.asList(args)));
					sender.sendMessage(getPlayersString());
					return true;

				case prey:
					if (args.length == 0) {
						sender.sendMessage(getPreyString());
						return true;
					}
					String givenPreyName = args[0];
					if (givenPreyName.equalsIgnoreCase(randomOption)) {
						setPreyName(null);
						sender.sendMessage("Prey player set to Random");
						return true;
					}
					setPreyName(givenPreyName);
					sender.sendMessage(getPreyString());
					return true;

				case location:
					if (args.length == 0) {
						sender.sendMessage(this.getLocation().toString());
						return true;
					}
					String givenLocation = args[0];
					AlienLocationOptions locationOption = Arrays.stream(AlienLocationOptions.values())
							.filter(o -> o.toString().equals(givenLocation))
							.findFirst()
							.orElse(null);
					if (locationOption == null) {
						return false;
					}
					setLocation(locationOption);
					sender.sendMessage("Game location set to " + locationOption);
					return true;

				case spawn:
					if (args.length == 0) {
						Location loc = this.getSpawnLocation();
						sender.sendMessage(loc == null ? "Spawn location not set" : "Spawn location: " + loc);
						return true;
					}
					if (args[0].equalsIgnoreCase("here")) {
						if (!(sender instanceof Player player)) {
							sender.sendMessage("You must be a player to set spawn to here.");
							return true;
						}
						setSpawnLocation(player.getLocation());
						sender.sendMessage("Spawn location set to your current location.");
						return true;
					}
					return false;

				case reveal:
					if (args.length == 0) {
						sender.sendMessage(this.getReveal().toString());
						return true;
					}
					String givenReveal = args[0];
					AlienRevealOptions revealOption = Arrays.stream(AlienRevealOptions.values())
							.filter(o -> o.toString().equals(givenReveal))
							.findFirst()
							.orElse(null);
					if (revealOption == null) {
						return false;
					}
					setReveal(revealOption);
					sender.sendMessage("Alien timer set to " + revealOption);
					return true;

				case alien_spawn_distance:
					if (args.length == 0) {
						sender.sendMessage(String.valueOf(getAlienSpawnDistance()));
						return true;
					}
					try {
						int distance = Integer.parseInt(args[0]);
						setAlienSpawnDistance(distance);
						sender.sendMessage("Alien spawn distance set to " + getAlienSpawnDistance());
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
			}

			return false;
		}

		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(AlienGameConfigs.values()).map(AlienGameConfigs::toString).toList();
			}

			String arg = args[0];
			if (!EnumUtils.isValidEnum(AlienGameConfigs.class, arg)) {
				return null;
			}
			AlienGameConfigs config = AlienGameConfigs.valueOf(arg);

			return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
		}

		private List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label,
				String[] args, AlienGameConfigs config) {
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();

			switch (config) {
				case players:
					if (args.length == 1) {
						allPlayers.add(allOption);
					}
					return allPlayers;

				case prey:
					allPlayers.add(randomOption);
					return allPlayers;

				case location:
					return locationOptions;

				case spawn:
					return List.of("here");

				case reveal:
					return revealOptions;

				case alien_spawn_distance:
					return List.of("10", "20", "30", "40");
			}

			return null;
		}

		@Override
		public AlienGame build() {
			return new AlienGame(this);
		}
	}
}