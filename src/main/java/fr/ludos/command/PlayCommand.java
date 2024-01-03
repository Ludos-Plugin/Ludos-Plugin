package fr.ludos.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;


import fr.ludos.games.Game;

public class PlayCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
        if ( ! Game.registered.containsKey(args[0]) ) {
            return false;
        }

        return Game.registered.get(args[0])
            .onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1) {
            return Game.registered.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        }
        if ( ! Game.registered.containsKey(args[0]) ) {
            return null;
        }
        return Game.registered.get(args[0])
            .onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }

    public String getUsage() {

        StringBuilder usage = new StringBuilder("/<command> ");

        usage.append('<');
        usage.append( Game.registered.keySet().stream()
                        .sorted()
                        .collect(Collectors.joining(" | ") ) );
        usage.append('>');

        usage.append(' ');

        usage.append('<');
        usage.append( Arrays.stream(PlayCommandOptions.values()).map(PlayCommandOptions::toString)
                        .sorted()
                        .collect(Collectors.joining(" | ")) );
        usage.append('>');

        usage.append(' ');

        usage.append("[option]");

        return usage.toString();
    }
}