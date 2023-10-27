package fr.ludos.games;

import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.controller.TeamController;


public abstract class Game<TTeamController extends TeamController> {
    protected Scoreboard scoreboard;
    TTeamController teamController;


    public Game(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }
    
}