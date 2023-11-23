package fr.ludos.games;

// import fr.ludos.Main;
import fr.ludos.command.MainCommandOptions;

import org.bukkit.command.TabExecutor;
import org.bukkit.scoreboard.Scoreboard;

// import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import javax.annotation.Nullable;

import fr.ludos.controller.TeamController;


public abstract class Game {
    protected Scoreboard scoreboard;
    protected TeamController teamController;

    public static final Map<String, Builder> registered = new HashMap<String, Builder>();


    public static void RegisterGame(Builder constructor) {
        Game.registered.put(constructor.getName(), constructor);
    }

    @Nullable
    public static Game BuildGame(String name) {
        if ( registered.containsKey(name) ) {
            return registered.get(name).Build();
        }

        return null;
    }


    public abstract void Start();
    

    public static abstract class Builder implements TabExecutor {
        public abstract String getName();

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            switch (args.length) {
                case 0:
                    return false;
                default:
                    try {
                        MainCommandOptions option = MainCommandOptions.valueOf(args[0]);
                        return gameCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
                    } catch (Exception e) {
                        return false;
                    }
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            switch (args.length) {
                case 0:
                case 1:
                    return Arrays.stream(MainCommandOptions.values()).map(x -> x.toString())
                        .sorted()
                        .collect(Collectors.toList());
                default:
                    try {
                        MainCommandOptions option = MainCommandOptions.valueOf(args[0]);
                        return gameTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
                    } catch (Exception e) {
                        return null;
                    }
            }
        }

        public abstract boolean gameCommand(CommandSender sender, Command command, String label, String[] args, MainCommandOptions option);
        public abstract List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, MainCommandOptions option);


        public abstract Game Build();
    }
}