package fr.ludos.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import fr.ludos.game.Game;


public class GameCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) return false;

		String gameString = args[0].toLowerCase();
		if (gameString.equals(GameCommandOptions.stop.toString())) {
			Game.stopCurrentGame();
			return true;
		}

		if (! Game.getRegistered().containsKey(gameString)) return false;

		return Game.getRegistered().get(gameString)
			.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length <= 1) {
			return Stream.concat(
					Game.getRegistered().keySet().stream().sorted(),
					Stream.of(GameCommandOptions.stop.name()) // option to stop the current game without specifying the game id
				)
				.collect(Collectors.toList() );
		}

		String gameString = args[0].toLowerCase();
		if (! Game.getRegistered().containsKey(gameString)) return null;

		return Game.getRegistered().get(gameString)
			.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	public String getUsage() {

		StringBuilder usage = new StringBuilder("/<command> ");

		usage.append('<');
		usage.append( Stream.concat(
				Game.getRegistered().keySet().stream().sorted(),
				Stream.of(GameCommandOptions.stop.name()) // option to stop the current game without specifying the game id
			)
			.collect(Collectors.joining(" | ")) );
		usage.append('>');

		usage.append(' ');

		usage.append('<');
		usage.append( Arrays.stream(GameCommandOptions.values()).map(GameCommandOptions::toString)
			.sorted()
			.collect(Collectors.joining(" | ")) );
		usage.append('>');

		usage.append(' ');

		usage.append("[option]");

		return usage.toString();
	}
}