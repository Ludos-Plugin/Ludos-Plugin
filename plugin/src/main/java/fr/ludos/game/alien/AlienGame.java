package fr.ludos.game.alien;

import java.util.Optional;
import java.util.Iterator;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Ludos;
import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;
import fr.ludos.game.GameTeamController;
import fr.ludos.game.alien.AlienMonster;

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

	private final Scoreboard scoreboard;

	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private final AlienTeamController teamController;

	@Override
	public AlienTeamController getTeamController() {
		return this.teamController;
	}

	private final AlienTimer timer;

	private AlienMonster alien;

	private final Builder builder;

	public Builder getAlienBuilder() {
		return this.builder;
	}

	private Player prey;
	private Set<Player> hunters;

	private WorldBorder border;

	private Location lastPreyLocation = null;
	private BukkitTask actionBarTask;

	private BukkitTask saturationTask;

	@Nullable
	public static UUID getBorderWorldUID(JavaPlugin plugin) {
		String value = plugin.getConfig().getString(borderWorldUUIDPath);

		if (value == null)
			return null;
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
		if (location == null)
			return;
		World world = Bukkit.getWorld(AlienGame.getBorderWorldUID(plugin));
		if (world == null)
			return;

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

		timer = new AlienTimer(this, builder.getReveal());
	}

	@Override
	protected void onGameInit() {
		// Player prey = teamController.getSelectedPrey();
		// if (prey == null) {
		// throw new IllegalStateException("Prey player is null");
		// }

		// Location gameLocation = prey.getLocation();
		// areaController.setup(gameLocation);
	}

	@Override
	protected void onGameStart() {
		timer.start();

		// Envoyer le titre à la proie
		prey.showTitle(Title.title(
				Component.text("The alien tracks you"),
				Component.text("Run for your life"),
				Title.Times.times(
						Duration.ofMillis(500),
						Duration.ofMillis(3500),
						Duration.ofMillis(1000))));

		// Envoyer le même titre à tous les chasseurs
		for (Player hunter : hunters) {
			hunter.showTitle(Title.title(
					Component.text("The alien tracks you"),
					Component.text("Run for your life"),
					Title.Times.times(
							Duration.ofMillis(500),
							Duration.ofMillis(3500),
							Duration.ofMillis(1000))));
		}

		AlienLocationOptions locationOption = builder.getLocation();
		Location origin;

		if (builder.getSpawnLocation() != null) {
			// If a spawn location is configured, teleport all players there and use it as
			// origin
			origin = builder.getSpawnLocation();

			// Teleport prey and hunters to spawn with small offsets to avoid stacking
			Random rnd = new Random();
			int i = 0;
			for (Player p : Bukkit.getOnlinePlayers()) {
				Location tp = origin.clone();
				// spread players by a few blocks
				tp.setX(tp.getX() + (i % 2 == 0 ? 1 : -1) * (1 + rnd.nextInt(2)));
				tp.setZ(tp.getZ() + ((i / 2) % 2 == 0 ? 1 : -1) * (1 + rnd.nextInt(2)));
				tp.setY(tp.getWorld().getHighestBlockYAt(tp.getBlockX(), tp.getBlockZ()) + 1);
				p.teleport(tp);
				p.setGameMode(GameMode.SURVIVAL);
				p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), p.getHealth()));
				i++;
			}
		} else {
			origin = prey.getLocation();
			if (locationOption == AlienLocationOptions.random) {
				origin = Utility.getGroundedLocationAround(origin, 300, 2500, origin);
			}
		}

		// For testing: spawn the alien near the players (2 to 6 blocks from origin)
		Location alienSpawnLocation = Utility.getGroundedLocationAround(origin, 10, 10, origin);
		alien = AlienMonster.spawn(this, alienSpawnLocation);

		prey.getWorld().setTime(1000);

		actionBarTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (lastPreyLocation == null)
					return;

				for (Player player : Bukkit.getOnlinePlayers()) {
					player.sendActionBar(
							Component.text("Prey's location:")
									.append(Component.text(" X:" + lastPreyLocation.getBlockX())
											.color(NamedTextColor.RED))
									.append(Component.text(" Y:" + lastPreyLocation.getBlockY())
											.color(NamedTextColor.GREEN))
									.append(Component.text(" Z:" + lastPreyLocation.getBlockZ())
											.color(NamedTextColor.BLUE)));
				}
			}
		}.runTaskTimer(getPlugin(), 1, 1);

		saturationTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player hunter : hunters) {
					hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
				prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
			}
		}.runTaskTimer(getPlugin(), 400, 400);

		Bukkit.getServer().broadcast(Component.text("The Game of Alien started"));
	}

	@Override
	protected void onGameStop() {
		timer.stop();

		if (alien != null) {
			alien.despawn();
			alien = null;
		}

		resetBorder(getPlugin());

		if (actionBarTask != null) {
			actionBarTask.cancel();
			actionBarTask = null;
		}

		if (saturationTask != null) {
			saturationTask.cancel();
			saturationTask = null;
		}

		Bukkit.getServer().broadcast(Component.text("The Game of Alien ended"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Set<Player> hunters = teamController.getTeamHunters();
		Player prey = teamController.getTeamPrey();

		if (hunters.isEmpty() || prey == null) {
			timer.resume();
			return;
		}

		var player = event.getPlayer();

		if (!hunters.contains(player) && player != prey) {
			player.setGameMode(GameMode.SPECTATOR);

			player.teleport(prey.getLocation());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		var hunters = teamController.getTeamHunters();
		if (hunters.contains(player)) {
			hunters.remove(player);
		}

		var prey = teamController.getTeamPrey();

		if (hunters.isEmpty() || prey == null) {
			timer.pause();
		}
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		// if (teamController.preyTeam.getEntries().contains(player.getName())) {
		// return false;
		// }

		return true;
	}

	public static class Builder extends Game.Builder {
		private static final String allOption = "all";
		private static final String randomOption = "random";

		public static final List<String> locationOptions = Arrays.stream(AlienLocationOptions.values())
				.map(v -> v.toString())
				.collect(Collectors.toList());
		public static final List<String> revealOptions = Arrays.stream(AlienRevealOptions.values())
				.map(v -> v.toString())
				.collect(Collectors.toList());

		public String getPreyName() {
			return getPlugin().getConfig().getString(preyPath);
		}

		public void setPreyName(String prey) {
			String value = prey == null ? null : prey;
			getPlugin().getConfig().set(preyPath, value);
			getPlugin().saveConfig();
		}

		public Set<String> getPlayerNames() {
			return getPlugin().getConfig().getStringList(playersPath).stream()
					.collect(Collectors.toSet());
		}

		public void setPlayerNames(Set<String> players) {
			List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
			getPlugin().getConfig().set(playersPath, value);
			getPlugin().saveConfig();
		}

		public AlienLocationOptions getLocation() {
			String locationString = getPlugin().getConfig().getString(locationPath);
			return Arrays.stream(AlienLocationOptions.values()).filter(o -> o.toString().equals(locationString))
					.findFirst()
					.orElse(AlienLocationOptions.random);
		}

		public void setLocation(AlienLocationOptions location) {
			String value = location == null ? null : location.toString();
			getPlugin().getConfig().set(locationPath, value);
			getPlugin().saveConfig();
		}

		public AlienRevealOptions getReveal() {
			String revealString = getPlugin().getConfig().getString(revealPath);
			return Arrays.stream(AlienRevealOptions.values()).filter(o -> o.toString().equals(revealString)).findFirst()
					.orElse(AlienRevealOptions.three_minutes);
		}

		public void setReveal(AlienRevealOptions reveal) {
			String value = reveal == null ? null : reveal.toString();
			getPlugin().getConfig().set(revealPath, value);
			getPlugin().saveConfig();
		}

		public Location getSpawnLocation() {
			return getPlugin().getConfig().getLocation(spawnLocationPath);
		}

		public void setSpawnLocation(Location loc) {
			getPlugin().getConfig().set(spawnLocationPath, loc);
			getPlugin().saveConfig();
		}

		public Builder(JavaPlugin plugin) {
			super(plugin);

			resetBorder(plugin);
		}

		@Nullable
		public Set<Player> getChosenPlayers() {
			Set<String> playerNames = this.getPlayerNames();
			if (playerNames.isEmpty())
				return null;

			return new HashSet<>(
					playerNames.stream()
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
			return playerNames.isEmpty() ? "All"
					: playerNames.stream() // TODO: Translate
							.collect(Collectors.joining(" "));
		}

		public String getPreyString() {
			String preyName = this.getPreyName();
			return preyName == null ? "Random" : preyName; // TODO: Translate
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Alien")
					.color(NamedTextColor.RED);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("A game of hide and seek.\n" +
					"As the Prey, survive for as long as possible, while the Hunters try to find you.\n" +
					"The Hunters possess a Compass that will update regularly to point at the Prey's position.");
		}

		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game " + getId() + " config <config> [value]");

			for (AlienGameConfigs config : AlienGameConfigs.values()) {
				usage.append("\n  ").append(config.toString()).append(" ")
						.append(config.getUsage());
			}

			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				return false;
			}

			String arg = args[0];
			AlienGameConfigs option = Arrays.stream(AlienGameConfigs.values()).filter(o -> o.toString().equals(arg))
					.findFirst().orElse(null);
			if (option == null)
				return false;

			return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args,
				AlienGameConfigs config) {
			switch (config) {
				case players:
					if (args.length == 0) {
						// Field is left empty, send the current config
						sender.sendMessage(getPlayersString());
						return true;
					}

					if (args[0].equalsIgnoreCase(allOption)) {
						// Reset to default option
						setPlayerNames(null);

						sender.sendMessage("All players included in the game"); // TODO: Translate
						return true;
					}

					setPlayerNames(new HashSet<>(Arrays.asList(args)));

					sender.sendMessage(getPlayersString());
					return true;

				case prey:
					if (args.length == 0) {
						// Field is left empty, send the current config
						sender.sendMessage(getPreyString());
						return true;
					}

					String givenPreyName = args[0];

					if (givenPreyName.equalsIgnoreCase(randomOption)) {
						// Reset to default option
						setPreyName(null);

						sender.sendMessage("Prey player set to Random"); // TODO: Translate
						return true;
					}

					setPreyName(givenPreyName);

					sender.sendMessage(getPreyString());
					return true;

				case location:
					if (args.length == 0) {
						// Field is left empty, send the current config
						sender.sendMessage(this.getLocation().toString());
						return true;
					}

					String givenLocation = args[0];
					AlienLocationOptions locationOption = Arrays.stream(AlienLocationOptions.values())
							.filter(o -> o.toString().equals(givenLocation)).findFirst().orElse(null);
					if (locationOption == null)
						return false;

					setLocation(locationOption);

					sender.sendMessage("Game location set to " + locationOption.toString()); // TODO: Translate
					return true;

				case spawn:
					// spawn command: no args -> show current; 'here' -> set to sender's location
					// (player only)
					if (args.length == 0) {
						Location loc = this.getSpawnLocation();
						if (loc == null) {
							sender.sendMessage("Spawn location not set");
						} else {
							sender.sendMessage("Spawn location: " + loc.toString());
						}
						return true;
					}

					String arg0 = args[0];
					if (arg0.equalsIgnoreCase("here")) {
						if (!(sender instanceof Player)) {
							sender.sendMessage("You must be a player to set spawn to here.");
							return true;
						}

						Player player = (Player) sender;
						Location loc = player.getLocation();
						setSpawnLocation(loc);
						sender.sendMessage("Spawn location set to your current location.");
						return true;
					}

					return false;

			}

			return false;
		}

		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(AlienGameConfigs.values())
						.map(AlienGameConfigs::toString)
						.collect(Collectors.toList());
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
					// Options are : any enumeration of players, or all players
					if (args.length == 1) {
						allPlayers.add(allOption);
					}
					return allPlayers;

				case prey:
					// Options are : any single player, or a random player
					allPlayers.add(randomOption);
					return allPlayers;

				case location:
					// Options are : random, here
					return locationOptions;
				case spawn:
					// Options are : here
					return Arrays.asList("here");
				case reveal:
					// Options are : short, medium, long
					return revealOptions;
			}

			return null;
		}

		@Override
		public AlienGame build() {
			return new AlienGame(this);
		}
	}

	@Override
	public GameTeamController getGameTeamController() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getGameTeamController'");
	}

	@Override
	public GameAreaController getGameAreaController() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getGameAreaController'");
	}
}