package fr.ludos.game.sheepwars;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.ludos.Ludos;
import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.game.GameAreaController;
import fr.ludos.game.GameTeamController;
import fr.ludos.game.worldborder.WorldBorderAreaController;
import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;


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
	private WorldBorderAreaController areaController;
	private Scoreboard scoreboard;

	public SheepwarsGame(Builder builder) {
		super(builder);
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	}

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	@Override
	public GameTeamController getGameTeamController() {
		return teamController;
	}

	@Override
	public GameAreaController getGameAreaController() {
		return areaController;
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		if (teamController == null) return false;
		return teamController.getSelectedPlayers().contains(player);
	}

	@Override
	protected void onGameInit() {
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
			areaController = new WorldBorderAreaController(this, mapLocationOption(config), mapAreaOption(config));
			Optional<Player> centerPlayer = teamController.getSelectedPlayers().stream().findFirst();
			if (centerPlayer.isPresent()) {
				areaController.setup(centerPlayer.get().getLocation());
			} else {
				areaController.setup(Bukkit.getWorlds().get(0).getSpawnLocation());
			}
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Failed to initialize team controller: " + e.getMessage());
		}
	}

	@Override
	protected void onGameStart() {
		setupWorldBorder();

		for (Player player : teamController.getSelectedPlayers()) {
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.setScoreboard(scoreboard);
		}

		Bukkit.broadcast(
			Component.text("Sheepwars has started! Use your Cow Eggs wisely!")
				.color(NamedTextColor.GREEN)
		);
	}

	@Override
	protected void onGameStop() {
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
		if (areaController == null) {
			return;
		}

		World world = areaController.getCenter().getWorld();
		if (world == null) {
			return;
		}

		WorldBorder border = world.getWorldBorder();
		border.setWarningDistance(50);
		border.setDamageAmount(1.0);
		border.setDamageBuffer(10.0);
	}

	private void resetWorldBorder() {
		if (areaController != null) {
			areaController.resetBorder();
		}
	}

	private WorldBorderAreaOption mapAreaOption(FileConfiguration config) {
		String areaStr = config.getString(areaPath);
		if (areaStr == null) {
			return WorldBorderAreaOption.medium;
		}

		SheepwarsAreaOptions option = EnumUtils.getEnum(SheepwarsAreaOptions.class, areaStr);
		if (option == null) {
			return WorldBorderAreaOption.medium;
		}

		return switch (option) {
			case small -> WorldBorderAreaOption.small;
			case medium -> WorldBorderAreaOption.medium;
			case large -> WorldBorderAreaOption.large;
		};
	}

	private WorldBorderLocationOption mapLocationOption(FileConfiguration config) {
		String locationStr = config.getString(locationPath);
		if (locationStr == null) {
			return WorldBorderLocationOption.here;
		}

		SheepwarsLocationOptions option = EnumUtils.getEnum(SheepwarsLocationOptions.class, locationStr);
		if (option == null) {
			return WorldBorderLocationOption.here;
		}

		return option == SheepwarsLocationOptions.random
			? WorldBorderLocationOption.random
			: WorldBorderLocationOption.here;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (teamController != null && teamController.getSelectedPlayers().contains(player)) {
			player.setScoreboard(scoreboard);
		}
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
		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			return "<config> <value> - Available configs: players, duration, respawnTime, teamKills";
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
	}
}