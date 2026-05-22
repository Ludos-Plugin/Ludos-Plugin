package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.Ludos;
import fr.ludos.command.ConfigSubcommand;
import fr.ludos.game.Game;
import fr.ludos.game.lobbyController.LobbyStartDelayOption;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import fr.ludos.game.teamController.GameJoinOption;
import fr.ludos.group.GroupJoinOption;
import fr.ludos.group.GroupRightsOption;

public enum GroupConfigs implements ConfigSubcommand {
	member_authorization() {
		@Override
		public String getDescription() {
			return "Defines what rights this group's members have.";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getGroupRightsOption(config).name() );
				return true;
			}

			String givenRights = args[0];
			GroupRightsOption joinOption = Arrays.stream(GroupRightsOption.values()).filter(o -> o.name().equals(givenRights)).findFirst().orElse(null);
			if (joinOption == null) return false;

			setGroupRightsOption(config, joinOption);

			sender.sendMessage("Group Join Option set to " + joinOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			// Options are : none, game, config
			if (args.length == 1)
				return GroupRightsOption.getOptions();
			return null;
		}

		@Override
		public String getUsage() {
			return GroupRightsOption.getUsage();
		}
	},
	group_join() {
		@Override
		public String getDescription() {
			return "Defines what happens when a player attempts to join the group";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getGroupJoinOption(config).name() );
				return true;
			}

			String givenJoin = args[0];
			GroupJoinOption joinOption = Arrays.stream(GroupJoinOption.values()).filter(o -> o.name().equals(givenJoin)).findFirst().orElse(null);
			if (joinOption == null) return false;

			setGroupJoinOption(config, joinOption);

			sender.sendMessage("Group Join Option set to " + joinOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			// Options are : auto, manual, none
			if (args.length == 1)
				return GroupJoinOption.getOptions();
			return null;
		}

		@Override
		public String getUsage() {
			return GroupJoinOption.getUsage();
		}
	},
	game_join {
		@Override
		public String getDescription() {
			return "Defines what happens when a player attempts to join the group during a game";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if ( args.length == 0 ) {
				// Field is left empty, send the current config
				sender.sendMessage( getGameJoinOption(config).name() );
				return true;
			}

			String givenJoin = args[0];
			GameJoinOption joinOption = Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(givenJoin)).findFirst().orElse(null);
			if (joinOption == null) return false;

			setGameJoinOption(config, joinOption);

			sender.sendMessage("Game Join Option set to " + joinOption.name()); // TODO: Translate
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : auto, manual, none
			if (args.length == 1)
				return GameJoinOption.getOptions();
			return null;
		}

		@Override
		public String getUsage() {
			return GameJoinOption.getUsage();
		}
	},
	wait_players {
		@Override
		public String getDescription() {
			return "Define which players to wait for in a game Lobby";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
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
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : online, all
			if (args.length == 1)
				return LobbyWaitPlayersOption.getOptions();
			return null;
		}

		@Override
		public String getUsage() {
			return LobbyWaitPlayersOption.getUsage();
		}

		@Override
		public boolean requireOp() {
			return false;
		}
	},
	start_delay {
		@Override
		public String getDescription() {
			return "How long it takes to start a game when all the players have joined the lobby";
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
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
		public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			// Options are : short, medium, long
			if (args.length == 1)
				return LobbyStartDelayOption.getOptions();
			return null;
		}

		@Override
		public String getUsage() {
			return LobbyStartDelayOption.getUsage();
		}

		@Override
		public boolean requireOp() {
			return false;
		}
	},
	game() {
		@Override
		public String getDescription() {
			return "Configure a game for this group.";
		}
		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length < 1) return false;

			String configGameId = args[0].toLowerCase();
			Game.Builder configGame = Game.getRegistered().get(configGameId);
			if (configGame == null) {
				sender.sendMessage("Game not found: " + configGameId);
				return false;
			}

			if (! config.isConfigurationSection(Game.namespace)) {
				config.createSection(Game.namespace);
			}
			ConfigurationSection gamesSection = config.getConfigurationSection(Game.namespace);

			boolean success = configGame.executeGameConfig(sender, command, label, gamesSection, Arrays.copyOfRange(args, 1, args.length));
			if (success) {
				JavaPlugin plugin = Ludos.getPlugin(Ludos.class);
				plugin.saveConfig();
			}
			return success;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			String configGameId = args[0].toLowerCase();
			Game.Builder configGame = Game.getRegistered().get(configGameId);
			if (configGame == null) return null;

			return configGame.gameConfigTabComplete(sender, command, label, java.util.Arrays.copyOfRange(args, 1, args.length));
		}
		@Override
		public String getUsage() {
			return "<" +
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ "> [option]";
		}
	};

	@Override
	public boolean requireOp() {
		return false;
	}

	public static final String groupRightsKey = "group_rights";
	public static final String groupRightsPath = Ludos.namespace + '.' + groupRightsKey;

	public static final String groupJoinKey = "group_join";
	public static final String groupJoinPath = Ludos.namespace + '.' + groupJoinKey;

	public static final String gameJoinKey = "game_join";
	public static final String gameJoinPath = Ludos.namespace + '.' + gameJoinKey;

	public static final String waitPlayersKey = "waitPlayers";
	public static final String waitPlayersPath = Ludos.namespace + '.' + waitPlayersKey;

	public static final String startDelayKey = "startDelay";
	public static final String startDelayPath = Ludos.namespace + '.' + startDelayKey;



	public static GroupRightsOption getGroupRightsOption(ConfigurationSection config) {
		String rightsString = config.getString(groupRightsPath);
		return Arrays.stream(GroupRightsOption.values()).filter(o -> o.name().equals(rightsString)).findFirst()
			.orElse(GroupRightsOption.invite);
	}
	public static void setGroupRightsOption(ConfigurationSection config, GroupRightsOption rights) {
		String value = rights == null ? null : rights.name();
		config.set(groupRightsPath, value);
	}


	public static GameJoinOption getGameJoinOption(ConfigurationSection config) {
		String joinString = config.getString(gameJoinPath);
		return Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(joinString)).findFirst()
			.orElse(GameJoinOption.auto);
	}
	public static void setGameJoinOption(ConfigurationSection config, GameJoinOption join) {
		String value = join == null ? null : join.name();
		config.set(gameJoinPath, value);
	}

	public static GroupJoinOption getGroupJoinOption(ConfigurationSection config) {
		String joinString = config.getString(groupJoinPath);
		return Arrays.stream(GroupJoinOption.values()).filter(o -> o.name().equals(joinString)).findFirst()
			.orElse(GroupJoinOption.need_accept);
	}
	public static void setGroupJoinOption(ConfigurationSection config, GroupJoinOption join) {
		String value = join == null ? null : join.name();
		config.set(groupJoinPath, value);
	}

	public static LobbyWaitPlayersOption getWaitPlayersOption(ConfigurationSection config) {
		String waitPlayersString = config.getString(waitPlayersPath);
		return Arrays.stream(LobbyWaitPlayersOption.values()).filter(o -> o.name().equals(waitPlayersString)).findFirst()
			.orElse(LobbyWaitPlayersOption.all);
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
}
