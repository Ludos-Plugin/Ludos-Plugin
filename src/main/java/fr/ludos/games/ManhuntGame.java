package fr.ludos.games;

import org.bukkit.Bukkit;
// import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.Listener;

import fr.ludos.command.MainCommandOptions;
import fr.ludos.controller.ManhuntTeamController;


public class ManhuntGame extends Game implements Listener {

    protected ManhuntGame(Player[] players, Player huntedPlayer) {
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

    public void Start() {
        Bukkit.broadcastMessage("Started a Game of Manhunt");
    }

    

    public static class Builder extends Game.Builder {

        private Player setHunted = null;

        private List<Player> players = null;



        public String getPlayersString() {
            return players == null ? "All" : players.stream().map(x -> x.getName()).collect(Collectors.joining(" "));
        }

        public String getHuntedString() {
            return setHunted == null ? "Random" : setHunted.getName();
        }

        @Override
        public String getName() {
            return "manhunt";
        }

        @Override
        public boolean gameCommand(CommandSender sender, Command command, String label, String[] args, MainCommandOptions option) {
            if (args.length == 0) {
                return false;
            }

            try {
                ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( args[0] );

                
                switch (args.length) {
                    case 1:
                        switch (args[0]) {
                            case "players":
                                sender.sendMessage( getPlayersString() );
                                break;
                            case "hunted":
                                sender.sendMessage( getHuntedString() );
                                break;
                        }
                        return true;
                    default:
                        switch ( option ) {
                            case config:
                                switch ( config ) {
                                    case players:
                                        if ( args[1].equals("all") ) {
                                            players = null;
                                            sender.sendMessage("All players included in the game"); // TODO: Translate
                                            return true;
                                        }
                                        
                                        players = new ArrayList<Player>();
                                        for ( int i = 1; i < args.length; i++) {
                                            players.add(Bukkit.getPlayerExact(args[i]));
                                        }
                                        sender.sendMessage( getPlayersString() );
                                        return true;
                                    case hunted:
                                        if ( args[1].equals("random") ) {
                                            setHunted = null;
                                            sender.sendMessage("Hunted player set to Random"); // TODO: Translate
                                            return true;
                                        }

                                        setHunted = Bukkit.getPlayerExact(args[1]);
                                        sender.sendMessage( getHuntedString() );
                                        return true;
                                }
                            case start:
                                break;
                        }
                        return true;
                }
            } catch (Exception e) {
                return false;
            }

        }

        @Override
        public List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, MainCommandOptions option) {

            if (args.length == 0) {
                return null;
            }

            if (args.length == 1) {
                switch ( option ) {
                    case config:
                        return Arrays.stream(ManhuntGameConfigs.values()).map(x -> x.toString())
                            .sorted()
                            .collect(Collectors.toList());
                    case start:
                        break;
                }
                return null;
            }


            switch ( option ) {
                case config:
                    List<String> allPlayers = Bukkit.getServer().getOnlinePlayers().stream().map(x -> x.getName())
                        .sorted()
                        .collect(Collectors.toList());

                    try {
                        ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( args[0] );

                        switch ( config ) {
                            case players:
                                if ( args.length == 2 ) {
                                    allPlayers.add("all");
                                }
                                return allPlayers;
                            case hunted:
                                if ( args.length == 2 ) {
                                    allPlayers.add("random");
                                    return allPlayers;
                                }
                                return null;
                        }
                    } catch (Exception e) {
                        return null;
                    }
                case start:
                    break;
            }


            return null;
        }

        @Override
        public ManhuntGame Build() {
            Player[] playersArray = new Player[players.size()];
            players.toArray(playersArray);
            return new ManhuntGame(playersArray, setHunted);
        }
    }
    
}