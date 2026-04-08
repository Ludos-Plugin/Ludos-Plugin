package fr.ludos.game.manhunt;

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
import org.bukkit.entity.Player;

import fr.ludos.command.CommandUtility;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaOption;
import fr.ludos.game.areaController.worldborder.WorldBorderLocationOption;
import fr.ludos.game.lobbyController.LobbyStartDelayOption;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import fr.ludos.game.teamController.GameJoinOption;

public enum ManhuntGameConfigs {
	prey (() -> "[player]") {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
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
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : any single player, or a random player
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();
			allPlayers.add(randomOption);
			return allPlayers;
		}
	},
	area (WorldBorderAreaOption::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
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
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : large, medium, small
			if (args.length == 1)
				return areaOptions;
			return null;
		}
	},
	location (WorldBorderLocationOption::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getLocation(config).name() );
				return true;
			}

			String givenLocation = args[0];
			WorldBorderLocationOption locationOption = Arrays.stream(WorldBorderLocationOption.values()).filter(o -> o.name().equals(givenLocation)).findFirst().orElse(null);
			if (locationOption == null) return false;

			setLocation(config, locationOption);

			sender.sendMessage("Game location set to " + locationOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : random, here
			if (args.length == 1)
				return locationOptions;
			return null;
		}
	},
	reveal (ManhuntRevealOptions::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
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
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : short, medium, long
			if (args.length == 1)
				return revealOptions;
			return null;
		}
	},
	join (GameJoinOption::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getJoinOption(config).name() );
				return true;
			}

			String givenJoin = args[0];
			GameJoinOption joinOption = Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(givenJoin)).findFirst().orElse(null);
			if (joinOption == null) return false;

			setJoinOption(config, joinOption);

			sender.sendMessage("Game Join Option set to " + joinOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : auto, manual, none
			if (args.length == 1)
				return joinOptions;
			return null;
		}
	},
	wait_players (LobbyWaitPlayersOption::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getWaitPlayersOption(config).name() );
				return true;
			}

			String givenWaitPlayers = args[0];
			LobbyWaitPlayersOption waitPlayersOption = Arrays.stream(LobbyWaitPlayersOption.values()).filter(o -> o.name().equals(givenWaitPlayers)).findFirst().orElse(null);
			if (waitPlayersOption == null) return false;

			setWaitPlayersOption(config, waitPlayersOption);

			sender.sendMessage("Lobby Wait Option set to " + waitPlayersOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : online, all
			if (args.length == 1)
				return waitPlayersOptions;
			return null;
		}
	},
	start_delay (LobbyStartDelayOption::getUsage) {
		@Override
		public boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getWaitDurationOption(config).name() );
				return true;
			}

			String givenWaitDuration = args[0];
			LobbyStartDelayOption startDelayOption = Arrays.stream(LobbyStartDelayOption.values()).filter(o -> o.name().equals(givenWaitDuration)).findFirst().orElse(null);
			if (startDelayOption == null) return false;

			setWaitDurationOption(config, startDelayOption);

			sender.sendMessage("Lobby Start Delay Option set to " + startDelayOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : short, medium, long
			if (args.length == 1)
				return waitDurationOptions;
			return null;
		}
	};

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private ManhuntGameConfigs(Supplier<String> usageGetter) {
		this.usageGetter = usageGetter;
	}

	public abstract boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args);
	public abstract List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args);


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
	public static final String joinKey = "join";
	public static final String joinPath = ManhuntGame.ID + '.' + joinKey;

	public static final String waitPlayersKey = "waitPlayers";
	public static final String waitPlayersPath = ManhuntGame.ID + '.' + waitPlayersKey;
	public static final String startDelayKey = "startDelay";
	public static final String startDelayPath = ManhuntGame.ID + '.' + startDelayKey;

	private static final String allOption = "all";
	private static final String randomOption = "random";

	public static final List<String> areaOptions = Arrays.stream(WorldBorderAreaOption.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> locationOptions = Arrays.stream(WorldBorderLocationOption.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> revealOptions = Arrays.stream(ManhuntRevealOptions.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> joinOptions = Arrays.stream(GameJoinOption.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> waitPlayersOptions = Arrays.stream(LobbyWaitPlayersOption.values())
		.map(v -> v.name())
		.collect(Collectors.toList());
	public static final List<String> waitDurationOptions = Arrays.stream(LobbyStartDelayOption.values())
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

	public static WorldBorderLocationOption getLocation(ConfigurationSection config) {
		String locationString = config.getString(locationPath);
		return Arrays.stream(WorldBorderLocationOption.values()).filter(o -> o.name().equals(locationString)).findFirst()
			.orElse(WorldBorderLocationOption.here);
	}
	public static void setLocation(ConfigurationSection config, WorldBorderLocationOption location) {
		String value = location == null ? null : location.name();
		config.set(locationPath, value);
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

	public static GameJoinOption getJoinOption(ConfigurationSection config) {
		String joinString = config.getString(joinPath);
		return Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(joinString)).findFirst()
			.orElse(GameJoinOption.auto);
	}
	public static void setJoinOption(ConfigurationSection config, GameJoinOption join) {
		String value = join == null ? null : join.name();
		config.set(joinPath, value);
	}

	public static LobbyWaitPlayersOption getWaitPlayersOption(ConfigurationSection config) {
		String waitPlayersString = config.getString(waitPlayersPath);
		return Arrays.stream(LobbyWaitPlayersOption.values()).filter(o -> o.name().equals(waitPlayersString)).findFirst()
			.orElse(LobbyWaitPlayersOption.online);
	}
	public static void setWaitPlayersOption(ConfigurationSection config, LobbyWaitPlayersOption waitPlayers) {
		String value = waitPlayers == null ? null : waitPlayers.name();
		config.set(waitPlayersPath, value);
	}

	public static LobbyStartDelayOption getWaitDurationOption(ConfigurationSection config) {
		String startDelayString = config.getString(startDelayPath);
		return Arrays.stream(LobbyStartDelayOption.values()).filter(o -> o.name().equals(startDelayString)).findFirst()
			.orElse(LobbyStartDelayOption.ten_seconds);
	}
	public static void setWaitDurationOption(ConfigurationSection config, LobbyStartDelayOption startDelay) {
		String value = startDelay == null ? null : startDelay.name();
		config.set(startDelayPath, value);
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