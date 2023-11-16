package fr.ludos.command;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for ( int i = 0; i < args.length; i++ ) {
            Bukkit.broadcastMessage( args[i] );
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if ( !(sender instanceof Player) ) return null;
        if ( !command.getName().equalsIgnoreCase("ludosplay") ) return null;

        switch (args.length) {
            case 1:
            default:
                List<String> actions = new ArrayList<String>();
                actions.add("config");
                actions.add("start");

                Collections.sort(actions);
                return actions;
            case 2:
                List<String> games = new ArrayList<String>();
                games.add("manhunt");

                Collections.sort(games);
                return games;
        }
    }
}