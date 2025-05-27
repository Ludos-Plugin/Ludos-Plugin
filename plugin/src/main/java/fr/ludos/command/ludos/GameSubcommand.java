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
		if (option != null) {
			switch (option) {
			case stop:
				Game.stopCurrentGame();
				return true;
			case help:
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			}
		}

		Game.Builder game = Game.getRegistered().get(arg);
		if (game == null) {
			sender.sendMessage("Game not found: " + arg);
			return false;
		}

		return onGameCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), game);
	}

	private boolean onGameCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull Game.Builder game) {
		GameOptions option;
		if (args.length == 0) {
			option = GameOptions.start;
		}
		else {
			String arg = args[0];
			option = Arrays.stream(GameOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
			if (option == null) return false;
		}

		switch (option) {
		case start:
			game.build().start();
			return true;
		case config:
			return game.executeGameConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case help:
			sender.sendMessage(game.getGameConfigUsage(sender, command, label));
			return true;
		case guidebook:
			if (!(sender instanceof Player player)) return false;

			ItemStack book = game.createGuidebook();
			player.getInventory().addItem(book);
			return true;
		default:
			return false;
		}
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Stream.concat(
				Game.getRegistered().keySet().stream(),
				Arrays.stream(GameSubcommandOptions.values()).map(GameSubcommandOptions::toString)
			)
				.collect(Collectors.toList());
		}

		String gameId = args[0].toLowerCase();

		Game.Builder game = Game.getRegistered().get(gameId);
		if (game == null) return null;

		return onGameTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), game);
	}

	private List<String> onGameTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull Game.Builder game) {
		if (args.length <= 1) {
			return Arrays.stream(GameOptions.values()).map(GameOptions::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0];
		GameOptions option = Arrays.stream(GameOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		switch (option) {
		case config:
			return game.gameConfigTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		default:
			return null;
		}
	}


	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " game ");

		usage.append('<');
		usage.append(
			Stream.concat(
				Game.getRegistered().keySet().stream().sorted(),
				Arrays.stream(GameSubcommandOptions.values()).sorted().map(GameSubcommandOptions::toString)
			)
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append('<');
		usage.append(
			Arrays.stream(GameOptions.values()).sorted().map(GameOptions::toString)
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append("[option]");

		return usage.toString();
	}
}
