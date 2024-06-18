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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Main;

public class ManhuntTimer implements Listener {

	private int revealSeconds = 180;
    private ManhuntGame game;
	private BossBar bossbar;

    private boolean isStarted = false;
    private boolean isRunning = false;

    private BukkitTask task;

    private long totalSeconds;
    private String formattedTime;

    public ManhuntTimer(ManhuntGame game, int revealSeconds) {
        this.game = game;
        this.revealSeconds = revealSeconds;

		bossbar = Bukkit.createBossBar("Timer", BarColor.RED, BarStyle.SEGMENTED_12);

        start();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bossbar.addPlayer(event.getPlayer());
        bossbar.setVisible(true);
    }

    private void start() {
        if (isStarted) {
            return;
        }
        isStarted = true;
        resume();

        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

		for (Player player : Bukkit.getOnlinePlayers()) {
            bossbar.addPlayer(player);
		}
		bossbar.setVisible(true);

    }

    public void stop() {
        if (! isStarted) {
            return;
        }
        pause();
        isStarted = false;

		bossbar.removeAll();
		bossbar.setVisible(false);

		HandlerList.unregisterAll(this);

        Bukkit.broadcastMessage(ChatColor.GREEN + "Timer ended. Final Time : " + formattedTime);
    }

    public void resume() {
        if (! isStarted || isRunning) {
            return;
        }
        isRunning = true;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                addSecond();
            }
        }.runTaskTimer(Main.getInstance(), 20, 20);
    }

    public void pause() {
        if (! isStarted || ! isRunning) {
            return;
        }
        isRunning = false;

        if (task != null) {
            task.cancel();
        }
        task = null;
    }



    private void addSecond() {
        totalSeconds += 1;

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 60;

        formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

		double progress = ((double)totalSeconds % (double)revealSeconds) / (double)revealSeconds;
		bossbar.setProgress(progress);
		bossbar.setTitle(formattedTime);

        if (totalSeconds % revealSeconds == 0 && totalSeconds != 0) {
            game.revealPrey();
        }
    }
}
