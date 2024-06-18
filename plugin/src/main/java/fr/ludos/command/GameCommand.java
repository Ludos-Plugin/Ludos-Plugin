package fr.ludos.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;


import fr.ludos.game.Game;

public class GameCommand implements TabExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		if ( ! Game.getRegistered().containsKey(args[0]) ) {
			return false;
		}

		return Game.getRegistered().get(args[0])
			.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length <= 1) {
			return Game.getRegistered().keySet().stream()
				.sorted()
				.collect(Collectors.toList());
		}

		String arg = args[0];
		if ( ! Game.getRegistered().containsKey(arg) ) {
			return null;
		}
		return Game.getRegistered().get(arg)
			.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	public String getUsage() {

		StringBuilder usage = new StringBuilder("/<command> ");

		usage.append('<');
		usage.append( Game.getRegistered().keySet().stream()
						.sorted()
						.collect(Collectors.joining(" | ") ) );
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