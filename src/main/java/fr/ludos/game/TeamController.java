package fr.ludos.game;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public abstract class TeamController {

    protected Scoreboard scoreboard;


    public TeamController(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void stop() {
        
    }
    
    protected abstract Team[] getTeams();

    protected Team createTeam(String name, ChatColor color) {
        Team team = scoreboard.registerNewTeam(name);
        team.setPrefix(color.toString());
        
        return team;
    }

    public boolean areAllies(Player player1, Player player2) {
        Team[] teams = getTeams();
        for (Team team : teams) {
            if (team.hasEntry(player1.getName()) && team.hasEntry(player2.getName())) {
                return true;
            }
        }
        return false;
    }
}
