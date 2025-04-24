package fr.ludos.game.manhunt;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;


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
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		bossbar.addPlayer(event.getPlayer());
		bossbar.setVisible(true);
	}

	public void start() {
		if (isStarted) return;

		isStarted = true;
		resume();

		Bukkit.getPluginManager().registerEvents(this, game.getPlugin());

		for (Player player : Bukkit.getOnlinePlayers()) {
			bossbar.addPlayer(player);
		}
		bossbar.setVisible(true);

	}

	public void stop() {
		if (! isStarted) return;

		pause();
		isStarted = false;

		bossbar.removeAll();
		bossbar.setVisible(false);

		HandlerList.unregisterAll(this);

		Bukkit.getServer().broadcast(
			Component.text("Timer ended. Final Time : " + formattedTime)
				.color(NamedTextColor.GREEN)
		);
	}

	public void resume() {
		if (! isStarted || isRunning) return;

		isRunning = true;

		task = new BukkitRunnable() {
			@Override
			public void run() {
				addSecond();
			}
		}.runTaskTimer(game.getPlugin(), 20, 20);
	}

	public void pause() {
		if (! isStarted || ! isRunning) return;

		isRunning = false;

		if (task != null) task.cancel();
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
