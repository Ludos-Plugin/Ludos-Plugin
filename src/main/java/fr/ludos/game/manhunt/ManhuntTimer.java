package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ManhuntTimer implements Listener {

    private boolean chronoStarted = false;
    private long startTime;
    private BukkitTask task;
    private long totalseconds;

    private ManhuntTeamController teamController;

    public ManhuntTimer(ManhuntTeamController teamController) {
        this.teamController = teamController;

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        Main.getInstance().getLogger().info("ManhuntTimer has been enabled!");
    }

    public void stop() {
        HandlerList.unregisterAll((Listener)this);
        Main.getInstance().getLogger().info("ManhuntTimer has been disabled!");
    }
    

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!chronoStarted) {
            startChrono();
            chronoStarted = true;
        }

        Player player = event.getPlayer();
        if ( this.teamController.preyTeam.hasEntry( player.getName() ) ) {
            stopChrono();
            chronoStarted = false;
        }
    }

    private void startChrono() {
        startTime = System.currentTimeMillis();
        task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), this::updateChrono, 0, 20);
    }

    private void stopChrono() {
        task.cancel();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        long seconds = totalTime / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Chrono terminé. Temps total : " + formattedTime);
    }

    private void updateChrono() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        totalseconds = elapsedTime / 1000;
        long minutes = totalseconds / 60;
        long seconds = totalseconds % 60;

        String formattedTime = String.format("%02d:%02d", minutes, seconds);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Chrono: " + formattedTime);

        if (totalseconds % 180 == 0){
            Bukkit.broadcastMessage("Position du chassé ");
        }
    }
}
