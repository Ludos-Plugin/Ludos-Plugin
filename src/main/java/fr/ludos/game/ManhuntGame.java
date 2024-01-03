package fr.ludos.game;

import org.bukkit.Bukkit;
// import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.apache.commons.lang3.EnumUtils;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.ludos.command.CommandUtility;
import fr.ludos.command.PlayCommandOptions;
import fr.ludos.controller.ManhuntTeamController;


public class ManhuntGame extends Game {

    protected ManhuntGame(Player[] players, Player huntedPlayer) {
        super();
        scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        this.teamController = new ManhuntTeamController(scoreboard);
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

    public void start() {
        Bukkit.broadcastMessage("Started a Game of Manhunt");
    }

    

    public static class Builder extends Game.Builder {
        private static final String allOption = "all";
        private static final String randomOption = "random";

        private Player setHunted = null;

        private List<Player> players = null;



        public String getPlayersString() {
            return players == null ? "All" : players.stream().map(Player::getName) // TODO: Translate
                .collect(Collectors.joining(" "));
        }

        public String getHuntedString() {
            return setHunted == null ? "Random" : setHunted.getName(); // TODO: Translate
        }

        @Override
        public String getId() {
            return "manhunt";
        }

        @Override
        public boolean gameCommand(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {
            if (args.length == 0) {
                return false;
            }

            switch ( option ) {
                case config:
                    if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, args[0]) ) {
                        return false;
                    }
                    ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( args[0] );

                    return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
                case start:
                    break;
            }

            return true;
        }

        @Override
        public List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {
            if (args.length == 0) {
                return null;
            }

            if (args.length == 1) {
                // Show all configs
                return Arrays.stream(ManhuntGameConfigs.values())
                    .map(ManhuntGameConfigs::toString)
                    .sorted()
                    .collect(Collectors.toList());
            }

            switch ( option ) {
                case config:
                    if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, args[0]) ) {
                        return null;
                    }
                    ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( args[0] );

                    return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
                case start:
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
                case hunted:
                    if ( args.length == 0 ) {
                        // Field is left empty, send the current config
                        sender.sendMessage( getHuntedString() );
                        return true;
                    }

                    if ( args[0].equalsIgnoreCase(randomOption) ) {
                        // Reset to default option
                        setHunted = null;
                        sender.sendMessage("Hunted player set to Random"); // TODO: Translate
                        return true;
                    }

                    setHunted = Bukkit.getPlayerExact(args[0]);

                    sender.sendMessage( getHuntedString() );
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
                case hunted:
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
            Player[] playersArray = new Player[players.size()];
            players.toArray(playersArray);
            return new ManhuntGame(playersArray, setHunted);
        }
    }
    
}