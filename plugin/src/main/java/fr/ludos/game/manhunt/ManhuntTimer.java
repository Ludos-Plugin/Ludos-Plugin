package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Main;

public class ManhuntTimer implements Listener {

	private static final Integer revealSeconds = 180;
    private ManhuntGame game;
	private BossBar bossbar;

    private boolean state = false;
    private long startTime;
    private BukkitTask task;

    private long totalSeconds;
    private String formattedTime;

    public ManhuntTimer(ManhuntGame game) {
        this.game = game;
		bossbar = Bukkit.createBossBar("Timer", BarColor.RED, BarStyle.SEGMENTED_12);

        start();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bossbar.addPlayer(event.getPlayer());
        bossbar.setVisible(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() == 0) {
            stop();
        }
    }

    private void start() {
        if (state) {
            return;
        }
        state = true;

		for (Player player : Bukkit.getOnlinePlayers()) {
			bossbar.addPlayer(player);
		}
		bossbar.setVisible(true);

        startTime = System.currentTimeMillis();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
    }

    public void stop() {
        if (! state) {
            return;
        }
        state = false;

        task.cancel();

		bossbar.removeAll();
		bossbar.setVisible(false);

        Bukkit.broadcastMessage(ChatColor.GREEN + "Chrono terminé. Temps total : " + formattedTime);
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        totalSeconds = elapsedTime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;

        formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

		double progress = ((double)totalSeconds % (double)revealSeconds) / (double)revealSeconds;
		bossbar.setProgress(progress);
		bossbar.setTitle(formattedTime);

        if (totalSeconds % 180 == 0 && totalSeconds != 0) {
            Bukkit.broadcastMessage("Position du chassé " + "");
        }
    }
}
