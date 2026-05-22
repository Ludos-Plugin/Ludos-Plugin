package fr.ludos.game.raid;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.command.CommandUtility;
import fr.ludos.command.ConfigSubcommand;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaOption;
import fr.ludos.game.arena.ArenaGame;

public enum RaidGameConfigs implements ConfigSubcommand {
	players {
		@Override
		public String getDescription() {
			return "Which players will participate in the Raid.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				Set<String> names = getPlayerNames(config);
				sender.sendMessage(names.isEmpty() ? "Auto" : String.join(" ", names));
				return true;
			}

			if (allOption.equalsIgnoreCase(args[0])) {
				setPlayerNames(config, null);
				sender.sendMessage("Players reset to " + allOption);
				return true;
			}

			setPlayerNames(config, new HashSet<>(Arrays.asList(args)));
			sender.sendMessage("Players set to " + String.join(" ", args));
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			List<String> values = CommandUtility.getOnlinePlayerNames();
			values.add(allOption);
			return values;
		}
		@Override
		public String getUsage() {
			return "[" + allOption + " | <player1> <player2> ...]";
		}
	},
	rounds {
		@Override
		public String getDescription() {
			return "How many rounds a Raid will last.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(Integer.toString(getWaves(config)));
				return true;
			}
			try {
				setWaves(config, Integer.parseInt(args[0]));
			} catch (NumberFormatException e) {
				return false;
			}
			sender.sendMessage("Raid waves set to " + getWaves(config));
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			return null;
		}
		@Override
		public String getUsage() {
			return "<number>";
		}
	},
	area {
		@Override
		public String getDescription() {
			return "The size of the Raid game area.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(getArea(config).name());
				return true;
			}
			WorldBorderAreaOption value = Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equalsIgnoreCase(args[0])).findFirst().orElse(null);
			if (value == null) return false;
			setArea(config, value);
			sender.sendMessage("Raid area set to " + value.name());
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 1) return WorldBorderAreaOption.options;
			return null;
		}
		@Override
		public String getUsage() {
			return WorldBorderAreaOption.getUsage();
		}
	};

	public static final String playersKey = "players";
	public static final String playersPath = ArenaGame.ID + '.' + playersKey;

	public static final String wavesKey = "waves";
	public static final String wavesPath = ArenaGame.ID + '.' + wavesKey;

	public static final String areaKey = "area";
	public static final String areaPath = ArenaGame.ID + '.' + areaKey;

	private static final String allOption = "all";

	@Override
	public boolean requireOp() {
		return false;
	}

	@Nullable
	public static Set<OfflinePlayer> getChosenPlayers(ConfigurationSection config) {
		Set<String> playerNames = getPlayerNames(config);
		if (playerNames.isEmpty()) return null;

		return new HashSet<>(
			playerNames.stream()
				.map(Bukkit::getOfflinePlayer)
				.filter(p -> p != null)
				.collect(Collectors.toSet())
		);
	}

	public static Set<String> getPlayerNames(ConfigurationSection config) {
		return new HashSet<>(config.getStringList(playersPath));
	}
	public static void setPlayerNames(ConfigurationSection config, Set<String> names) {
		config.set(playersPath, names == null ? null : List.copyOf(names));
	}

	public static int getWaves(ConfigurationSection config) {
		return config.getInt(wavesPath, 15);
	}
	public static void setWaves(ConfigurationSection config, int value) {
		config.set(wavesPath, value);
	}

	public static WorldBorderAreaOption getArea(ConfigurationSection config) {
		String area = config.getString(areaPath, null);
		return Arrays.stream(WorldBorderAreaOption.values())
			.filter(o -> o.name().equalsIgnoreCase(area))
			.findFirst()
			.orElse(WorldBorderAreaOption.medium);
	}
	public static void setArea(ConfigurationSection config, WorldBorderAreaOption value) {
		config.set("raid.area", value.name());
	}
}
