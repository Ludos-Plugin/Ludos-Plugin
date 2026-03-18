package fr.ludos.game.sheepwars;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Material;
import net.kyori.adventure.text.format.TextDecoration;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.game.TeamController;
import fr.ludos.item.sheep.AbstractSheep;
import fr.ludos.item.sheep.SheepRegistry;
import fr.ludos.listener.sheep.SheepDrop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;


public class SheepwarsGame extends Game {
	public static final String id = "sheepwars";

	public static final String playersKey = "players";
	public static final String playersPath = id + '.' + playersKey;
	public static final String teamsKey = "teams";
	public static final String teamsPath = id + '.' + teamsKey;
	public static final String areaKey = "area";
	public static final String areaPath = id + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = id + '.' + locationKey;

	public static final String borderWorldUUIDKey = "borderWorldUUID";
	public static final String borderWorldUUIDPath = id + '.' + borderWorldUUIDKey;
	public static final String borderLocationKey = "borderLocation";
	public static final String borderLocationPath = id + '.' + borderLocationKey;

	private SheepwarsTeamController teamController;
	private Scoreboard scoreboard;
	private final SheepRegistry sheepRegistry;

	private List<AbstractSheep> sheepList = null;
	private SheepDrop sheepDrop;

	private File path;

	public SheepwarsGame(Builder builder) {
		super(builder);
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		this.sheepRegistry = new SheepRegistry();
		this.path = new File(builder.getPlugin().getDataFolder(), "sheep.json");
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public TeamController getTeamController() {
		return teamController;
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		if (teamController == null) return false;
		return teamController.getSelectedPlayers().contains(player);
	}

	@Override
	protected void onInit() {
		FileConfiguration config = getBuilder().getPlugin().getConfig();

		Set<Player> players = null;
		List<String> playerNames = config.getStringList(playersPath);
		if (!playerNames.isEmpty()) {
			players = playerNames.stream()
				.map(Bukkit::getPlayerExact)
				.filter(player -> player != null)
				.collect(Collectors.toSet());
		}

		int teamCount = 2;
		String teamsStr = config.getString(teamsPath);
		if (teamsStr != null) {
			SheepwarsTeamOptions teamOption = EnumUtils.getEnum(SheepwarsTeamOptions.class, teamsStr);
			if (teamOption != null) {
				teamCount = teamOption.getTeamCount();
			}
		}

		try {
			teamController = new SheepwarsTeamController(this, players, teamCount);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to initialize team controller: " + e.getMessage());
		}

		JSONArray sheepJsonArray = new JSONArray();
		
		if (!path.exists()) {
			path.getParentFile().mkdirs();
			InputStream inputStream = JavaPlugin.getPlugin(Ludos.class).getResource("sheep.json");
			
			if (inputStream != null) {
				try (InputStreamReader reader = new InputStreamReader(inputStream)) {
					sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (path.exists() && sheepJsonArray.isEmpty()) {
			try (FileReader reader = new FileReader(path)) {
				sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// try (FileReader reader = new FileReader(path)) {
		// 	sheepJsonArray = (JSONArray) new JSONParser().parse(reader);
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

		Bukkit.getLogger().info("the cureent path where sheep.json finding : " + path.toString());

		sheepList =  SheepRegistry.loadFromJson(sheepJsonArray);

		// Register all sheep in the registry
		for (AbstractSheep sheep : sheepList) {
			sheepRegistry.register(sheep);
		}
	}

	@Override
	protected void onStart() {
		teamController.start();

		// Register the sheep registry (handles interaction events)
		Bukkit.getPluginManager().registerEvents(sheepRegistry, this.getBuilder().getPlugin());

		// Also register the first sheep for shared event handlers (damage, death, movement)
		if (!sheepList.isEmpty()) {
			Bukkit.getPluginManager().registerEvents(sheepList.get(0), this.getBuilder().getPlugin());
		}

		setupWorldBorder();

		for (Player player : teamController.getSelectedPlayers()) {
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(10);
			player.setSaturation(20);
			player.setScoreboard(scoreboard);
			
			player.getInventory().clear();

			for(AbstractSheep currentSheep : sheepList) {
				player.getInventory().addItem(currentSheep.createSheepItem(4));
			}

		}

		Bukkit.broadcast(
			Component.text("Sheepwars has started! Use your Sheep Wool wisely!")
				.color(NamedTextColor.GREEN)
		);

		// Start the wool drop timer (every 15 seconds)
		sheepDrop = new SheepDrop(getPlugin(), teamController.getSelectedPlayers(), sheepList);
		sheepDrop.start();
	}

	@Override
	protected void onStop() {
		// Stop the wool drop timer
		if (sheepDrop != null) {
			sheepDrop.stop();
			sheepDrop = null;
		}

		if (teamController != null) {
			teamController.stop();
		}

		// Unregister both sheep registry and shared event handlers
		HandlerList.unregisterAll(sheepRegistry);
		if (!sheepList.isEmpty()) {
			HandlerList.unregisterAll(sheepList.get(0));
		}

		resetWorldBorder();

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		}

		Bukkit.broadcast(
			Component.text("Sheepwars has ended!")
				.color(NamedTextColor.RED)
		);
	}

	private void setupWorldBorder() {
		FileConfiguration config = getBuilder().getPlugin().getConfig();

		String worldUUIDStr = config.getString(borderWorldUUIDPath);
		World world = null;

		if (worldUUIDStr != null) {
			try {
				UUID worldUUID = UUID.fromString(worldUUIDStr);
				world = Bukkit.getWorld(worldUUID);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		if (world == null) {
			world = Bukkit.getWorlds().get(0);
		}

		// WorldBorder border = world.getWorldBorder();
		
		// SheepwarsAreaOptions areaOption = SheepwarsAreaOptions.medium;
		// String areaStr = config.getString(areaPath);
		// if (areaStr != null) {
		// 	SheepwarsAreaOptions configArea = EnumUtils.getEnum(SheepwarsAreaOptions.class, areaStr);
		// 	if (configArea != null) {
		// 		areaOption = configArea;
		// 	}
		// }
		
		// Location centerLocation = world.getSpawnLocation();
		// String locationStr = config.getString(locationPath);
		// if ("here".equals(locationStr)) {
		// 	Optional<Player> firstPlayer = teamController.getSelectedPlayers().stream().findFirst();
		// 	if (firstPlayer.isPresent()) {
		// 		centerLocation = firstPlayer.get().getLocation();
		// 	}
		// } else if ("random".equals(locationStr)) {
		// 	centerLocation = Utility.getGroundedLocationAround(
		// 		world.getSpawnLocation(), 
		// 		100, 
		// 		1000, 
		// 		world.getSpawnLocation()
		// 	);
		// }
		
		// border.setCenter(centerLocation);
		// border.setSize(areaOption.getSize() * 2);
		// border.setWarningDistance(50);
		// border.setDamageAmount(1.0);
		// border.setDamageBuffer(10.0);

		// config.set(borderWorldUUIDPath, world.getUID().toString());
		// config.set(borderLocationPath, 
		// 	centerLocation.getX() + "," + 
		// 	centerLocation.getY() + "," + 
		// 	centerLocation.getZ()
		// );
		// getBuilder().getPlugin().saveConfig();
	}

	private void resetWorldBorder() {
		FileConfiguration config = getBuilder().getPlugin().getConfig();
		String worldUUIDStr = config.getString(borderWorldUUIDPath);

		if (worldUUIDStr != null) {
			try {
				UUID worldUUID = UUID.fromString(worldUUIDStr);
				World world = Bukkit.getWorld(worldUUID);
				if (world != null) {
					WorldBorder border = world.getWorldBorder();
					border.reset();
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (teamController != null && teamController.getSelectedPlayers().contains(player)) {
			player.setScoreboard(scoreboard);
		}

        // player.sendMessage(Component.text("Bienvenue sur le serveur Ludos!", NamedTextColor.AQUA, TextDecoration.BOLD));
        // ItemStack sheepItem = new ItemStack(Material.SHEEP_SPAWN_EGG, 1);

        // if (!player.getInventory().contains(sheepItem)) {
        //     player.getInventory().addItem(sheepItem);
        // } 

        // ItemMeta meta = sheepItem.getItemMeta();

        // List<Component> lore = meta.lore();
        // lore.add(Component.text("Gros mouton sa mère, хорошо, хорошо, хорошо", NamedTextColor.YELLOW, TextDecoration.BOLD));
        // meta.lore(lore);

        // sheepItem.setItemMeta(meta);
	}

	public static class Builder extends Game.Builder {

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Sheepwars");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("A team-based battle game where players use Cow Eggs to spawn allies and fight for victory!");
		}

		@Override
		public Game build() {
			return new SheepwarsGame(this);
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 2) {
				sender.sendMessage(Component.text("Usage: /" + label + " game config " + getGameConfigUsage(sender, command, label))
					.color(NamedTextColor.RED));
				return true;
			}

			String configKey = args[0];
			SheepwarsGameConfigs config = EnumUtils.getEnum(SheepwarsGameConfigs.class, configKey);

			if (config == null) {
				sender.sendMessage(Component.text("Unknown config option: " + configKey)
					.color(NamedTextColor.RED));
				return true;
			}

			FileConfiguration fileConfig = getPlugin().getConfig();
			String configPath = id + '.' + config.toString();

			switch (config) {
				case players -> {
					List<String> playerNames = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
					fileConfig.set(configPath, playerNames);
					sender.sendMessage(Component.text("Set players to: " + String.join(", ", playerNames))
						.color(NamedTextColor.GREEN));
				}
				case teams -> {
					String teamStr = args[1];
					SheepwarsTeamOptions teamOption = EnumUtils.getEnum(SheepwarsTeamOptions.class, teamStr);
					if (teamOption != null) {
						fileConfig.set(configPath, teamStr);
						sender.sendMessage(Component.text("Set teams to: " + teamOption.getTeamCount() + " teams")
							.color(NamedTextColor.GREEN));
					} else {
						sender.sendMessage(Component.text("Invalid team option. Use: " + SheepwarsTeamOptions.getUsage())
							.color(NamedTextColor.RED));
					}
				}
				case area -> {
					String areaStr = args[1];
					SheepwarsAreaOptions areaOption = EnumUtils.getEnum(SheepwarsAreaOptions.class, areaStr);
					if (areaOption != null) {
						fileConfig.set(configPath, areaStr);
						sender.sendMessage(Component.text("Set area to: " + areaOption.toString() + " (" + areaOption.getSize() + " blocks)")
							.color(NamedTextColor.GREEN));
					} else {
						sender.sendMessage(Component.text("Invalid area option. Use: " + SheepwarsAreaOptions.getUsage())
							.color(NamedTextColor.RED));
					}
				}
				case location -> {
					String locationStr = args[1];
					SheepwarsLocationOptions locationOption = EnumUtils.getEnum(SheepwarsLocationOptions.class, locationStr);
					if (locationOption != null) {
						fileConfig.set(configPath, locationStr);
						sender.sendMessage(Component.text("Set location to: " + locationOption.toString())
							.color(NamedTextColor.GREEN));
					} else {
						sender.sendMessage(Component.text("Invalid location option. Use: " + SheepwarsLocationOptions.getUsage())
							.color(NamedTextColor.RED));
					}
				}
			}

			getPlugin().saveConfig();
			return true;
		}

		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 1) {
				return Arrays.stream(SheepwarsGameConfigs.values())
					.map(SheepwarsGameConfigs::toString)
					.collect(Collectors.toList());
			}

			if (args.length == 2) {
				String configKey = args[0];
				SheepwarsGameConfigs config = EnumUtils.getEnum(SheepwarsGameConfigs.class, configKey);

				if (config != null) {
					switch (config) {
						case players -> {
							return CommandUtility.getOnlinePlayerNames();
						}
						case teams -> {
							return Arrays.stream(SheepwarsTeamOptions.values())
								.map(SheepwarsTeamOptions::toString)
								.collect(Collectors.toList());
						}
						case area -> {
							return Arrays.stream(SheepwarsAreaOptions.values())
								.map(SheepwarsAreaOptions::toString)
								.collect(Collectors.toList());
						}
						case location -> {
							return Arrays.stream(SheepwarsLocationOptions.values())
								.map(SheepwarsLocationOptions::toString)
								.collect(Collectors.toList());
						}
					}
				}
			}

			if (args.length > 2) {
				String configKey = args[0];
				if ("players".equals(configKey)) {
					return CommandUtility.getOnlinePlayerNames();
				}
			}

			return null;
		}

		@Override
		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			return "<" +
				Arrays.stream(SheepwarsGameConfigs.values())
					.map(config -> config.toString() + " " + config.getUsage())
					.collect(Collectors.joining(" | ")) +
				">";
		}
	}
}