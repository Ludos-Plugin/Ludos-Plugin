package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.command.CommandUtility;
import fr.ludos.command.Subcommand;
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

			if (!(sender instanceof Player leader)) {
				sender.sendMessage("Only players can start games.");
				return true;
			}

			String startGameId = args[0].toLowerCase();
			if ( !Game.getRegistered().containsKey(startGameId) ) {
				sender.sendMessage("Game not found: " + startGameId);
				return false;
			}

			Group group = Group.getGroupOfPlayer(leader);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			if (!group.isLeader(leader)) {
				sender.sendMessage("Only the group leader can start a game.");
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
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " game start <" +
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ "> [solo]";
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
			if (!(sender instanceof Player leader)) {
				sender.sendMessage("Only players can stop games.");
				return true;
			}

			Group group = Group.getGroupOfPlayer(leader);
			if (group == null) {
				sender.sendMessage("You are not in a group.");
				return true;
			}

			if (!group.isLeader(leader)) {
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
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " game stop";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	// config() {
	// 	@Override
	// 	public String getDescription() {
	// 		return "Configure a game.";
	// 	}
	// 	@Override
	// 	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
	// 		if (args.length < 1) return false;

	// 		String configGameId = args[0].toLowerCase();
	// 		Game.Builder configGame = Game.getRegistered().get(configGameId);
	// 		if (configGame == null) {
	// 			sender.sendMessage("Game not found: " + configGameId);
	// 			return false;
	// 		}

	// 		return configGame.executeGameConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	// 	}
	// 	@Override
	// 	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
	// 		if (args.length == 1)
	// 			return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

	// 		String configGameId = args[0].toLowerCase();
	// 		Game.Builder configGame = Game.getRegistered().get(configGameId);
	// 		if (configGame == null) return null;

	// 		return configGame.gameConfigTabComplete(sender, command, label, java.util.Arrays.copyOfRange(args, 1, args.length));
	// 	}
	// 	@Override
	// 	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
	// 		return "/" + label + " game config <" +
	// 			Game.getRegistered().keySet().stream().sorted()
	// 				.collect(Collectors.joining(" | "))
	// 			+ "> [option]";
	// 	}
	// },
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
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return "/" + label + " game guidebook <" +
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
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			}

			String arg = args[0].toLowerCase();
			GameSubcommand option = Arrays.stream(GameSubcommand.values())
				.filter(o -> o != help)
				.filter(o -> o.name().equals(arg))
				.findFirst().orElse(null);
			if (option == null) return false;

			sender.sendMessage(option.getUsage(sender, command, label));
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
		public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game ");

			usage.append('<');
			usage.append(
				Arrays.stream(GameSubcommand.values())
					.filter(o -> o != help)
					.map(GameSubcommand::name)
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');

			usage.append(' ');

			usage.append('<');
			usage.append(
				Game.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');

			usage.append(' ');

			usage.append("[option]");

			return usage.toString();
		}
	};
}