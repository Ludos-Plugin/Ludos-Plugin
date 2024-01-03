package fr.ludos.game;

import fr.ludos.Main;
import fr.ludos.command.PlayCommandOptions;

import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.command.TabExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import javax.annotation.Nullable;

import fr.ludos.controller.TeamController;


public abstract class Game implements Listener {
    protected Scoreboard scoreboard;
    protected TeamController teamController;

    public static final Map<String, Builder> registered = new HashMap<String, Builder>();


    public static void registerGame(Builder constructor) {
        Game.registered.put(constructor.getId(), constructor);
    }

    @Nullable
    public static Game buildGame(String name) {
        if ( registered.containsKey(name) ) {
            return registered.get(name).build();
        }

        return null;
    }

    public abstract void start();


    public Game() {
        Bukkit.getPluginManager().registerEvents((Listener)this, Main.getInstance());
    }
    


    public static abstract class Builder implements TabExecutor {
        public abstract String getId();

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 1) {
                return false;
            }

            if ( ! EnumUtils.isValidEnum(PlayCommandOptions.class, args[0]) ) {
                return false;
            }
            PlayCommandOptions option = PlayCommandOptions.valueOf( args[0] );

            return gameCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
        }

        @Override
        public final List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
            if (args.length < 2) {
                return Arrays.stream(PlayCommandOptions.values())
                    .map(PlayCommandOptions::toString)
                    .sorted()
                    .collect(Collectors.toList());
            }

            if ( ! EnumUtils.isValidEnum(PlayCommandOptions.class, args[0]) ) {
                return null;
            }
            PlayCommandOptions option = PlayCommandOptions.valueOf( args[0] );

            return gameTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
        }

        public abstract boolean gameCommand(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option);
        public abstract List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option);


        public abstract Game build();
    }
}