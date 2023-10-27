package fr.ludos.controller;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

public final class ManhuntTeamController extends TeamController implements Listener {
    public final Team huntersTeam;
    public final Team huntedTeam;


    public ManhuntTeamController(Scoreboard scoreboard) {
        super(scoreboard);

        huntersTeam = super.createTeam("Hunters", ChatColor.RED);
        huntersTeam.setAllowFriendlyFire(false);

        huntedTeam = super.createTeam("Hunted", ChatColor.BLUE);
        huntedTeam.setAllowFriendlyFire(false);
    }


    @Override
    protected Team[] getTeams() {
        return new Team[] {
            huntersTeam, huntedTeam
        };
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if ( 
            event.getEntity() instanceof Player player &&
            huntedTeam.hasEntry(player.getName())
        ) {
            Bukkit.broadcastMessage(player.getName());
            Bukkit.broadcastMessage("Hunted killed!");
            huntedTeam.removeEntry(player.getName());
        }

        if ( huntedTeam.getSize() == 0 ) {
            Bukkit.broadcastMessage("All Hunted Dead! End of Game!");
            // Main.StopGame();
        }
    }
    
}