package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;

public final class GameSubcommand implements TabExecutor {
	public static final String arg = "game";


	protected final Ludos plugin;


	public GameSubcommand(Ludos plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		GameSubcommandOptions option = Arrays.stream(GameSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return false;

		return onCommandOption(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}
	private boolean onCommandOption(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull GameSubcommandOptions option) {
		switch (option) {
		case start:
			if (args.length < 1) break;

			String startGameId = args[0].toLowerCase();
			if ( !Game.getRegistered().containsKey(startGameId) ) {
				sender.sendMessage("Game not found: " + startGameId);
				break;
			}

			Game.startGame(startGameId);
			return true;
		case stop:
			Game.stopCurrentGame();
			return true;
		case config:
			if (args.length < 1) break;

			String configGameId = args[0].toLowerCase();
			Game.Builder configGame = Game.getRegistered().get(configGameId);
			if (configGame == null) {
				sender.sendMessage("Game not found: " + configGameId);
				break;
			}

			return configGame.executeGameConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case guidebook:
			if (args.length < 1) break;

			String guidebookGameId = args[0].toLowerCase();
			Game.Builder guidebookGame = Game.getRegistered().get(guidebookGameId);
			if (guidebookGame == null) {
				sender.sendMessage("Game not found: " + guidebookGameId);
				break;
			}

			Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (player == null) break;

			ItemStack book = guidebookGame.createGuidebook();
			player.getInventory().addItem(book);
			return true;
		case help:
			sender.sendMessage(getUsage(sender, command, label));
			return true;
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(GameSubcommandOptions.values())
				.map(GameSubcommandOptions::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0].toLowerCase();
		GameSubcommandOptions option = Arrays.stream(GameSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		return onTabCompleteOption(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}
	private List<String> onTabCompleteOption(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull GameSubcommandOptions option) {
		switch (option) {
		case start:
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());
		case stop:
			break;
		case config:
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			String configGameId = args[0].toLowerCase();
			Game.Builder configGame = Game.getRegistered().get(configGameId);
			if (configGame == null) break;

			return configGame.gameConfigTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case guidebook:
			if (args.length == 1)
				return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			if (args.length == 2)
				return CommandUtility.getOnlinePlayerNames();
		case help:
			break;
		}

		return null;
	}


	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " game ");

		usage.append('<');
		usage.append(
			Arrays.stream(GameSubcommandOptions.values()).sorted().map(GameSubcommandOptions::toString)
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
}
