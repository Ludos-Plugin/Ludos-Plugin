package fr.ludos;

import java.io.IOException;
import java.net.http.WebSocket.Listener;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.command.MainCommand;
import org.bukkit.entity.Player;
// import fr.ludos.controller.DataController;
// import fr.ludos.games.ManhuntGame;

public class Main extends JavaPlugin implements Listener {
    private ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;


    public void StartManhuntGame() {
        // new ManhuntGame(scoreboard);
    }

    @Override
    public void onEnable() {
        // try {
        //     DataController.loadProperties();
        // } catch (IOException e) {
            
        // }
        System.out.println("Plugin Enabled");

        scoreboardManager = getServer().getScoreboardManager();
        scoreboard = scoreboardManager.getNewScoreboard();
        

        PluginCommand playCmd = getCommand("playludos");
        MainCommand command = new MainCommand();
        playCmd.setExecutor(command);
        playCmd.setTabCompleter(command);
        playCmd.setTabCompleter(command);
    }

    @Override
    public void onDisable() {
        // try {
        //     DataController.saveProperties();
        // } catch (IOException e) {

        // }
        // Bukkit.broadcastMessage( DataController.getProperties().toString() );

        System.out.println("Plugin Disabled");
    }

}