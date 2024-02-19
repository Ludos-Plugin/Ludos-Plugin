package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import fr.ludos.game.Game;
import fr.ludos.game.TeamController;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;

import javax.annotation.Nullable;

public final class ManhuntTeamController extends TeamController implements Listener {
    public final Team hunterTeam;
    public final Team preyTeam;


    public ManhuntTeamController(Scoreboard scoreboard, @Nullable Set<Player> players, @Nullable Player prey) {
        super(scoreboard);

        hunterTeam = super.createTeam("Hunters", ChatColor.RED);
        hunterTeam.setAllowFriendlyFire(false);

        preyTeam = super.createTeam("Prey", ChatColor.BLUE);
        preyTeam.setAllowFriendlyFire(false);

        if (players == null) {
            players = new HashSet<Player>();
            players.addAll(Bukkit.getOnlinePlayers());
        }
        if (prey == null) {
            Player[] arrayNumbers = players.toArray( new Player[players.size()] );
            prey = arrayNumbers[ new Random().nextInt(players.size()) ];
        }

        preyTeam.addEntry(prey.getName());

        for (Player player : players) {
            hunterTeam.addEntry(player.getName());
        }
    }

    @Override
    public void stop() {
        super.stop();

        preyTeam.unregister();
        hunterTeam.unregister();
    }


    @Override
    protected Team[] getTeams() {
        return new Team[] {
            hunterTeam, preyTeam
        };
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if ( 
            preyTeam.hasEntry(event.getEntity().getName())
        ) {
            Bukkit.broadcastMessage(player.getName());
            Bukkit.broadcastMessage("Prey Slain!");
            preyTeam.removeEntry(player.getName());
        }

        if ( preyTeam.getSize() == 0 ) {
            Bukkit.broadcastMessage("All Prey Dead! End of Game!");
            Game.stopGame();
        }
    }
    
}