package fr.ludos.game;

import fr.ludos.Main;
import fr.ludos.command.PlayCommandOptions;
import fr.ludos.role.Role;

import org.bukkit.event.HandlerList;
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


public abstract class Game implements Listener {
    protected final Scoreboard scoreboard;
    protected TeamController teamController;

    @Nullable
    public static Game current = null;
    public static final Map<String, Builder> registered = new HashMap<String, Builder>();

    public final Map<String, Role> activeRoles = new HashMap<String, Role>();


    public static void registerGame(Builder constructor) {
        Game.registered.put(constructor.getId(), constructor);
    }

    public static void startGame(Builder builder) {
        stopGame();

        current = builder.build();
    }
    public static void startGame(String id) {
        if ( ! registered.containsKey(id) ) {
            return;
        }
        
        startGame(registered.get(id));
    }
    public static void stopGame() {
        if ( current != null) {
            current.stop();
            current = null;
        }
    }

    public Game(Builder builder) {
        scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

        for (Role.Builder role : Role.registered.values()) {
            activeRoles.put(role.getId(), role.build(builder.getId()));
        }
    }

    public void stop() {
        HandlerList.unregisterAll(this);

        for (Role role : activeRoles.values()) {
            role.stop();
        }
        activeRoles.clear();

        if (teamController != null) {
            teamController.stop();
        }
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