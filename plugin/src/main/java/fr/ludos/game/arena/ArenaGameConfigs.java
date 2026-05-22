package fr.ludos.game.arena;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
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

public enum ArenaGameConfigs implements ConfigSubcommand {
	team1 {
		@Override
		public String getDescription() {
			return "Which players will be part of team 1.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			return handleTeamCommand(sender, config, args, 0);
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			return teamTabComplete();
		}
		@Override
		public String getUsage() {
			return "[" + autoOption + " | <player1> <player2> ...]";
		}
	},
	team2 {
		@Override
		public String getDescription() {
			return "Which players will be part of team 2.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			return handleTeamCommand(sender, config, args, 1);
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			return teamTabComplete();
		}
		@Override
		public String getUsage() {
			return "[" + autoOption + " | <player1> <player2> ...]";
		}
	},
	mode {
		@Override
		public String getDescription() {
			return "The kind of Arena game to play.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(getMode(config).name());
				return true;
			}
			ArenaModeOption value = ArenaModeOption.resolve(args[0]).orElse(null);
			if (value == null) return false;
			setMode(config, value);
			sender.sendMessage("Arena mode set to " + value.name());
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 1) return modeOptions;
			return null;
		}
		@Override
		public String getUsage() {
			return ArenaModeOption.getUsage();
		}
	},
	rounds {
		@Override
		public String getDescription() {
			return "How many rounds an Arena game will last.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				sender.sendMessage(Integer.toString(getRounds(config)));
				return true;
			}
			try {
				setRounds(config, Integer.parseInt(args[0]));
			} catch (NumberFormatException e) {
				return false;
			}
			sender.sendMessage("Arena rounds set to " + getRounds(config));
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
			return "The size of the Arena.";
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
			sender.sendMessage("Arena area set to " + value.name());
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 1) return areaOptions;
			return null;
		}
		@Override
		public String getUsage() {
			return WorldBorderAreaOption.getUsage();
		}
	};

	@Override
	public boolean requireOp() {
		return false;
	}

	public abstract boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args);
	public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);


	public static final String team1Key = "team1";
	public static final String team1Path = ArenaGame.ID + '.' + team1Key;
	public static final String team2Key = "team2";
	public static final String team2Path = ArenaGame.ID + '.' + team2Key;
	public static final String modeKey = "mode";
	public static final String modePath = ArenaGame.ID + '.' + modeKey;
	public static final String roundsKey = "rounds";
	public static final String roundsPath = ArenaGame.ID + '.' + roundsKey;
	public static final String areaKey = "area";
	public static final String areaPath = ArenaGame.ID + '.' + areaKey;

	private static final String autoOption = "auto";

	public static final List<String> modeOptions = ArenaModeOption.options;
	public static final List<String> areaOptions = WorldBorderAreaOption.options;


	private static String teamPath(int index) {
		return index == 0 ? team1Path : team2Path;
	}

	public static Set<String> getTeamNames(ConfigurationSection config, int index) {
		return new HashSet<>(config.getStringList(teamPath(index)));
	}

	public static void setTeamNames(ConfigurationSection config, int index, Set<String> names) {
		config.set(teamPath(index), names == null ? null : List.copyOf(names));
	}

	@Nullable
	public static Set<OfflinePlayer> getChosenTeam(ConfigurationSection config, int index) {
		Set<String> names = getTeamNames(config, index);
		if (names.isEmpty()) return null;
		return names.stream()
			.map(Bukkit::getOfflinePlayer)
			.collect(Collectors.toSet());
	}

	public static ArenaModeOption getMode(ConfigurationSection config) {
		return ArenaModeOption.fromConfig(config.getString(modePath), ArenaModeOption.duel);
	}
	public static void setMode(ConfigurationSection config, ArenaModeOption mode) {
		config.set(modePath, mode == null ? null : mode.name());
	}

	public static int getRounds(ConfigurationSection config) {
		return Math.max(1, config.getInt(roundsPath, 3));
	}
	public static void setRounds(ConfigurationSection config, int rounds) {
		config.set(roundsPath, Math.max(1, rounds));
	}

	public static WorldBorderAreaOption getArea(ConfigurationSection config) {
		String value = config.getString(areaPath);
		return Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equalsIgnoreCase(value)).findFirst().orElse(WorldBorderAreaOption.small);
	}
	public static void setArea(ConfigurationSection config, WorldBorderAreaOption area) {
		config.set(areaPath, area == null ? null : area.name());
	}

	private static boolean handleTeamCommand(CommandSender sender, ConfigurationSection config, String[] args, int index) {
		if (args.length == 0) {
			Set<String> names = getTeamNames(config, index);
			sender.sendMessage(names.isEmpty() ? "Auto" : String.join(" ", names));
			return true;
		}
		if (autoOption.equalsIgnoreCase(args[0])) {
			setTeamNames(config, index, null);
			sender.sendMessage("Arena team" + (index + 1) + " reset to " + autoOption);
			return true;
		}
		setTeamNames(config, index, new HashSet<>(Arrays.asList(args)));
		sender.sendMessage("Arena team" + (index + 1) + " set to " + String.join(" ", getTeamNames(config, index)));
		return true;
	}

	private static List<String> teamTabComplete() {
		List<String> values = CommandUtility.getOnlinePlayerNames();
		values.add(autoOption);
		return values;
	}
}
