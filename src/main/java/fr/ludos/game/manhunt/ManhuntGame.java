package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
// import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import org.apache.commons.lang3.EnumUtils;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.ludos.Main;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.PlayCommandOptions;
import fr.ludos.game.Game;

public class ManhuntGame extends Game {

    private ManhuntCompassEvents compassEvents;
    private ManhuntTimer timer;

    protected ManhuntGame(Builder builder) {
        super(builder);

        ManhuntTeamController controller = new ManhuntTeamController(scoreboard, builder.getPlayers(), builder.getPrey());
        this.teamController = controller;
        
        this.timer = new ManhuntTimer(controller);

        compassEvents = new ManhuntCompassEvents();
        Bukkit.getPluginManager().registerEvents(compassEvents, Main.getInstance());

        Bukkit.broadcastMessage("The Game of Manhunt started");
    }

    @Override
    public void stop() {
        super.stop();

        timer.stop();

        HandlerList.unregisterAll((Listener)compassEvents);

        Bukkit.broadcastMessage("The Game of Manhunt ended");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // if(
        //     event.getDamager() instanceof Player damager && 
        //     event.getEntity() instanceof Player entity &&
        //     teamController.areAllies(damager, entity) 
        // ) {
        //     event.setCancelled(false);
        // }   
    }


    

    public static class Builder extends Game.Builder {
        private static final String allOption = "all";
        private static final String randomOption = "random";

        private Player prey = null;
        private List<Player> players = null;


        @Nullable
        public Set<Player> getPlayers() {
            if (players == null) {
                return null;
            }
            return new HashSet<Player>(players);
        }

        @Nullable
        public Player getPrey() {
            return prey;
        }

        public String getPlayersString() {
            return players == null ? "All" : players.stream() // TODO: Translate
                .map(Player::getName)
                .collect(Collectors.joining(" "));
        }

        public String getPreyString() {
            return prey == null ? "Random" : prey.getName(); // TODO: Translate
        }

        @Override
        public String getId() {
            return "manhunt";
        }

        @Override
        public boolean gameCommand(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {

            switch ( option ) {
                case config:
                    if (args.length == 0) {
                        return false;
                    }

                    String arg = args[0];
                    if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, arg) ) {
                        return false;
                    }
                    ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( arg );

                    return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
                case start:
                    Game.startGame(this);
                    break;
                case stop:
                    Game.stopGame();
                    break;
            }

            return true;
        }

        @Override
        public List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {
            if (args.length == 0) {
                return null;
            }

            switch ( option ) {
                case config:
                    if (args.length == 1) {
                        // Show all configs
                        return Arrays.stream(ManhuntGameConfigs.values())
                            .map(ManhuntGameConfigs::toString)
                            .sorted()
                            .collect(Collectors.toList());
                    }
                    
                    String arg = args[0];
                    if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, arg) ) {
                        return null;
                    }
                    ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( arg );

                    return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
                case start:
                    break;
                case stop:
                    break;
            }

            return null;
        }

        private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args, ManhuntGameConfigs config) {
            switch ( config ) {
                case players:
                    if ( args.length == 0 ) {
                        // Field is left empty, send the current config
                        sender.sendMessage( getPlayersString() );
                        return true;
                    }

                    if ( args[0].equalsIgnoreCase(allOption) ) {
                        // Reset to default option
                        players = null;
                        sender.sendMessage("All players included in the game"); // TODO: Translate
                        return true;
                    }
                    
                    players = new ArrayList<Player>();
                    for ( int i = 0; i < args.length; i++) {
                        Player argPlayer = Bukkit.getPlayerExact(args[i]);
                        if (argPlayer == null) {
                            continue;
                        }

                        players.add(argPlayer);
                    }
                    if (players.isEmpty()) {
                        players = null;
                    }

                    sender.sendMessage( getPlayersString() );
                    return true;
                case prey:
                    if ( args.length == 0 ) {
                        // Field is left empty, send the current config
                        sender.sendMessage( getPreyString() );
                        return true;
                    }

                    if ( args[0].equalsIgnoreCase(randomOption) ) {
                        // Reset to default option
                        prey = null;
                        sender.sendMessage("Prey player set to Random"); // TODO: Translate
                        return true;
                    }

                    prey = Bukkit.getPlayerExact(args[0]);

                    sender.sendMessage( getPreyString() );
                    return true;
            }

            return false;
        }

        private List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args, ManhuntGameConfigs config) {
            List<String> allPlayers = CommandUtility.getOnlinePlayerNames();

            switch ( config ) {
                case players:
                    // Options are : any enumeration of players, or all players

                    if ( args.length == 1 ) {
                        allPlayers.add(allOption);
                    }
                    return allPlayers;
                case prey:
                    // Options are : any single player, or a random player

                    if ( args.length == 1 ) {
                        allPlayers.add(randomOption);
                        return allPlayers;
                    }
                    return null;
            }

            return null;
        }

        @Override
        public ManhuntGame build() {
            return new ManhuntGame(this);
        }
    }
    
}