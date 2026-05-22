package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.Subcommand;
import fr.ludos.command.SubcommandManager;
import fr.ludos.game.Game;
import fr.ludos.group.Group;

public enum GameSubcommand implements Subcommand {
	start() {
		@Override
		public String getDescription() {
			return "As a group leader, start a game.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;

			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can start games.");
				return true;
			}

			String startGameId = args[0].toLowerCase();
			if ( !Game.getRegistered().containsKey(startGameId) ) {
				sender.sendMessage("Game not found: " + startGameId);
				return false;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			boolean membersCanRunGames = GroupConfigs.getGroupRightsOption(group.getConfig()).canRunGames();
			if (! group.isLeader(player) && ! membersCanRunGames) {
				sender.sendMessage("Only the group leader can start games.");
				return true;
			}

			Game.startGame(startGameId, group);
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());
			return null;
		}
		@Override
		public String getUsage() {
			return "<" +
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ ">";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	stop() {
		@Override
		public String getDescription() {
			return "Stop the current game.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (!(sender instanceof Player player)) {
				sender.sendMessage("Only players can stop games.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			boolean membersCanRunGames = GroupConfigs.getGroupRightsOption(group.getConfig()).canRunGames();
			if (! group.isLeader(player) && ! membersCanRunGames) {
				sender.sendMessage("Only the group leader can stop the game.");
				return true;
			}

			Game game = group.getGame();
			if (game == null) {
				sender.sendMessage("There is no game running.");
				return true;
			}

			game.stop();
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return null;
		}
		@Override
		public String getUsage() {
			return "";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	config() {
		@Override
		public String getDescription() {
			return "Configure a game.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;
			if (! (sender instanceof Player player)) {
				sender.sendMessage("Only players can configure games.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(player);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			boolean membersCanConfig = GroupConfigs.getGroupRightsOption(group.getConfig()).canConfig();
			if (! group.isLeader(player) && ! membersCanConfig) {
				sender.sendMessage("Only the group leader can configure the group.");
				return true;
			}

			String configGameId = args[0].toLowerCase();
			Game.Builder configGame = Game.getRegistered().get(configGameId);
			if (configGame == null) {
				sender.sendMessage("Game not found: " + configGameId);
				return false;
			}

			ConfigurationSection configSection = group.getConfig();
			if (! configSection.isConfigurationSection(Game.namespace)) {
				configSection.createSection(Game.namespace);
			}
			ConfigurationSection gamesSection = configSection.getConfigurationSection(Game.namespace);

			boolean res = configGame.executeGameConfig(sender, command, label, gamesSection, Arrays.copyOfRange(args, 1, args.length));

			if (res) {
				Ludos plugin = JavaPlugin.getPlugin(Ludos.class);
				Group.saveConfigGroup(plugin, group);
				plugin.saveConfig();
			}

			return res;
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
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	guidebook() {
		@Override
		public String getDescription() {
			return "Give the guidebook for a game to a player.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;

			String guidebookGameId = args[0].toLowerCase();
			Game.Builder guidebookGame = Game.getRegistered().get(guidebookGameId);
			if (guidebookGame == null) {
				sender.sendMessage("Game not found: " + guidebookGameId);
				return false;
			}

			Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (player == null) return false;

			ItemStack book = guidebookGame.createGuidebook();
			player.getInventory().addItem(book);
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			if (args.length == 2)
				return CommandUtility.getOnlinePlayerNames();

			return null;
		}
		@Override
		public String getUsage() {
			return "<" +
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ "> [player]";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	help() {
		@Override
		public String getDescription() {
			return "Show help for game commands.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getUsage());
				return true;
			}

			String arg = args[0].toLowerCase();
			GameSubcommand option = Arrays.stream(GameSubcommand.values())
				.filter(o -> o != help)
				.filter(o -> o.name().equals(arg))
				.findFirst().orElse(null);
			if (option == null) return false;

			sender.sendMessage(option.getUsage());
			return true;
		}
		@Override
		public boolean requireOp() {
			return false;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Arrays.stream(GameSubcommand.values())
					.filter(o -> o != help)
					.map(GameSubcommand::name)
					.collect(Collectors.toList());
			}
			return null;
		}
		@Override
		public String getUsage() {
			return SubcommandManager.getUsage(
				Arrays.stream(GameSubcommand.values())
					.filter(o -> o != help)
			);
		}
	};
}