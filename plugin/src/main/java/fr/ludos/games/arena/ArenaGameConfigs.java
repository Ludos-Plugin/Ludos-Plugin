package fr.ludos.games.arena;

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

import fr.ludos.core.area.WorldBorderAreaOption;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.ConfigSubcommand;

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
			return "[" + AUTO_OPTION + " | <player1> <player2> ...]";
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
			return "[" + AUTO_OPTION + " | <player1> <player2> ...]";
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
			if (args.length == 1) return MODE_OPTIONS;
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
				sender.sendMessage("Invalid number");
				return true;
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
			if (value == null)  {
				sender.sendMessage("Invalid option");
				return true;
			}
			setArea(config, value);
			sender.sendMessage("Arena area set to " + value.name());
			return true;
		}
		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 1) return AREA_OPTIONS;
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


	public static final String TEAM_1_KEY = "team1";
	public static final String TEAM_1_PATH = ArenaGame.ID + '.' + TEAM_1_KEY;
	public static final String TEAM_2_KEY = "team2";
	public static final String TEAM_2_PATH = ArenaGame.ID + '.' + TEAM_2_KEY;
	public static final String MODE_KEY = "mode";
	public static final String MODE_PATH = ArenaGame.ID + '.' + MODE_KEY;
	public static final String ROUNDS_KEY = "rounds";
	public static final String ROUNDS_PATH = ArenaGame.ID + '.' + ROUNDS_KEY;
	public static final String AREA_KEY = "area";
	public static final String AREA_PATH = ArenaGame.ID + '.' + AREA_KEY;

	private static final String AUTO_OPTION = "auto";

	public static final List<String> MODE_OPTIONS = ArenaModeOption.OPTIONS;
	public static final List<String> AREA_OPTIONS = WorldBorderAreaOption.OPTIONS;


	private static String teamPath(int index) {
		return index == 0 ? TEAM_1_PATH : TEAM_2_PATH;
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
		return ArenaModeOption.fromConfig(config.getString(MODE_PATH), ArenaModeOption.duel);
	}
	public static void setMode(ConfigurationSection config, ArenaModeOption mode) {
		config.set(MODE_PATH, mode == null ? null : mode.name());
	}

	public static int getRounds(ConfigurationSection config) {
		return Math.max(1, config.getInt(ROUNDS_PATH, 3));
	}
	public static void setRounds(ConfigurationSection config, int rounds) {
		config.set(ROUNDS_PATH, Math.max(1, rounds));
	}

	public static WorldBorderAreaOption getArea(ConfigurationSection config) {
		String value = config.getString(AREA_PATH);
		return Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equalsIgnoreCase(value)).findFirst().orElse(WorldBorderAreaOption.small);
	}
	public static void setArea(ConfigurationSection config, WorldBorderAreaOption area) {
		config.set(AREA_PATH, area == null ? null : area.name());
	}

	private static boolean handleTeamCommand(CommandSender sender, ConfigurationSection config, String[] args, int index) {
		if (args.length == 0) {
			Set<String> names = getTeamNames(config, index);
			sender.sendMessage(names.isEmpty() ? "Auto" : String.join(" ", names));
			return true;
		}
		if (AUTO_OPTION.equalsIgnoreCase(args[0])) {
			setTeamNames(config, index, null);
			sender.sendMessage("Arena team" + (index + 1) + " reset to " + AUTO_OPTION);
			return true;
		}
		setTeamNames(config, index, new HashSet<>(Arrays.asList(args)));
		sender.sendMessage("Arena team" + (index + 1) + " set to " + String.join(" ", getTeamNames(config, index)));
		return true;
	}

	private static List<String> teamTabComplete() {
		List<String> values = CommandUtility.getOnlinePlayerNames();
		values.add(AUTO_OPTION);
		return values;
	}
}
