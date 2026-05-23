package fr.ludos.game.manhunt;

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

import fr.ludos.area.WorldBorderAreaOption;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.ConfigSubcommand;

public enum ManhuntGameConfigs implements ConfigSubcommand {
	prey {
		@Override
		public String getDescription() {
			return "Select which player will be prey; random if unset";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getPreyString(config) );
				return true;
			}

			String givenPreyName = args[0];

			if ( givenPreyName.equalsIgnoreCase(randomOption) ) {
				// Reset to default option
				setPreyName(config, null);

				sender.sendMessage("Prey player set to Random"); // TODO: Translate
				return true;
			}

			setPreyName(config, givenPreyName);

			sender.sendMessage( "Prey player set to " + getPreyString(config) );
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : any single player, or a random player
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();
			allPlayers.add(randomOption);
			return allPlayers;
		}
		@Override
		public String getUsage() {
			return "[player | random]";
		}

		@Override
		public boolean requireOp() {
			return false;
		}
	},
	area {
		@Override
		public String getDescription() {
			return "Defines the size of the game area";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getArea(config).name() );
				return true;
			}

			String givenArea = args[0];
			WorldBorderAreaOption areaOption = Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equals(givenArea)).findFirst().orElse(null);
			if (areaOption == null) return false;

			setArea(config, areaOption);

			sender.sendMessage("Game area set to " + areaOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : large, medium, small
			if (args.length == 1)
				return areaOptions;
			return null;
		}

		@Override
		public String getUsage() {
			return WorldBorderAreaOption.getUsage();
		}

		@Override
		public boolean requireOp() {
			return false;
		}
	},
	reveal {
		@Override
		public String getDescription() {
			return "Defines the time it takes for the prey to be revealed";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getReveal(config).name() );
				return true;
			}

			String givenReveal = args[0];
			ManhuntRevealOptions revealOption = Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.name().equals(givenReveal)).findFirst().orElse(null);
			if (revealOption == null) return false;

			setReveal(config, revealOption);

			sender.sendMessage("Prey Reveal Frequency set to " + revealOption.displayName()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : short, medium, long
			if (args.length == 1)
				return revealOptions;
			return null;
		}

		@Override
		public String getUsage() {
			return ManhuntRevealOptions.getUsage();
		}

		@Override
		public boolean requireOp() {
			return false;
		}
	};


	public static final String playersKey = "players";
	public static final String playersPath = ManhuntGame.ID + '.' + playersKey;
	public static final String preyKey = "prey";
	public static final String preyPath = ManhuntGame.ID + '.' + preyKey;

	public static final String areaKey = "area";
	public static final String areaPath = ManhuntGame.ID + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = ManhuntGame.ID + '.' + locationKey;
	public static final String revealKey = "reveal";
	public static final String revealPath = ManhuntGame.ID + '.' + revealKey;

	private static final String randomOption = "random";

	public static final List<String> areaOptions = Arrays.stream(WorldBorderAreaOption.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> revealOptions = Arrays.stream(ManhuntRevealOptions.values())
		.map(v -> v.name())
		.collect(Collectors.toList());


	public static String getPreyName(ConfigurationSection config) {
		return config.getString(preyPath);
	}
	public static void setPreyName(ConfigurationSection config, String prey) {
		String value = prey == null ? null : prey;
		config.set(preyPath, value);
	}

	public static Set<String> getPlayerNames(ConfigurationSection config) {
		return config.getStringList(playersPath).stream()
			.collect(Collectors.toSet());
	}
	public static void setPlayerNames(ConfigurationSection config, Set<String> players) {
		List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
		config.set(playersPath, value);
	}

	public static WorldBorderAreaOption getArea(ConfigurationSection config) {
		String areaString = config.getString(areaPath);
		return Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equals(areaString)).findFirst()
			.orElse(WorldBorderAreaOption.medium);
	}
	public static void setArea(ConfigurationSection config, WorldBorderAreaOption area) {
		String value = area == null ? null : area.name();
		config.set(areaPath, value);
	}

	public static ManhuntRevealOptions getReveal(ConfigurationSection config) {
		String revealString = config.getString(revealPath);
		return Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.name().equals(revealString)).findFirst()
			.orElse(ManhuntRevealOptions.three_minutes);
	}
	public static void setReveal(ConfigurationSection config, ManhuntRevealOptions reveal) {
		String value = reveal == null ? null : reveal.name();
		config.set(revealPath, value);
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

	@Nullable
	public static OfflinePlayer getChosenPrey(ConfigurationSection config) {
		String preyName = getPreyName(config);
		if (preyName == null) {
			return null;
		}
		return Bukkit.getOfflinePlayer(preyName);
	}

	public static String getPlayersString(ConfigurationSection config) {
		Set<String> playerNames = getPlayerNames(config);
		return playerNames.isEmpty() ? "All" : playerNames.stream() // TODO: Translate
			.collect(Collectors.joining(" "));
	}

	public static String getPreyString(ConfigurationSection config) {
		String preyName = getPreyName(config);
		return preyName == null ? "Random" : preyName; // TODO: Translate
	}
}