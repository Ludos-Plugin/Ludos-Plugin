package fr.ludos.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.ludos.Main;


public class ServerListener implements Listener {
    Main plugin;

    public ServerListener(Main plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void playerLeftTheGame(PlayerQuitEvent event){
        // Player player = event.getPlayer();
    }

}
