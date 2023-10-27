package fr.ludos;

import java.io.IOException;
import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.command.MainCommand;
import fr.ludos.controller.DataController;
import fr.ludos.games.ManhuntGame;

public class Main extends JavaPlugin implements Listener {
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;


    public void StartManhuntGame() {
        new ManhuntGame(scoreboard);
    }


    @Override
    public void onDisable() {
        try {
            DataController.saveProperties();
        } catch (IOException e) {

        }
        Bukkit.broadcastMessage( DataController.getProperties().toString() );

        System.out.println("Plugin Disabled");
    }

    @Override
    public void onEnable() {
        try {
            DataController.loadProperties();
        } catch (IOException e) {
            
        }
        System.out.println("Plugin Enabled");

        scoreboardManager = getServer().getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();

        getCommand("ludos").setExecutor(new MainCommand());
    }

}