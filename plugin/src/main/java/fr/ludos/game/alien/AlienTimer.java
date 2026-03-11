package fr.ludos.game.alien;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AlienTimer extends GameProcessBase {
	private final AlienGame game;
	private final AlienRevealOptions revealOption;
	private final BossBar bossbar;

	private boolean isRunning = false;
	private BukkitTask task;
	private long totalSeconds;
	private String formattedTime = "00:00:00";

	@Override
	protected JavaPlugin getPlugin() {
		return game.getPlugin();
	}

	public AlienTimer(AlienGame game, AlienRevealOptions revealOption) {
		this.game = game;
		this.revealOption = revealOption;
		this.bossbar = Bukkit.createBossBar("Timer", BarColor.RED, revealOption.getBarStyle());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		bossbar.addPlayer(event.getPlayer());
		bossbar.setVisible(true);
	}

	@Override
	protected void onStart() {
		resume();

		for (Player player : Bukkit.getOnlinePlayers()) {
			bossbar.addPlayer(player);
		}
		bossbar.setVisible(true);
	}

	@Override
	protected void onStop() {
		pause();
		bossbar.removeAll();
		bossbar.setVisible(false);

		Bukkit.broadcast(Component.text("Timer ended. Final Time : " + formattedTime).color(NamedTextColor.GREEN));
	}

	public void resume() {
		if (!isStarted() || isRunning) {
			return;
		}

		isRunning = true;
		task = new BukkitRunnable() {
			@Override
			public void run() {
				addSecond();
			}
		}.runTaskTimer(game.getPlugin(), 20L, 20L);
	}

	public void pause() {
		if (!isStarted() || !isRunning) {
			return;
		}

		isRunning = false;
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	private void addSecond() {
		totalSeconds++;

		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

		double timerDuration = revealOption.getDuration();
		double progress = (double) (totalSeconds % revealOption.getDuration()) / timerDuration;

		bossbar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
		bossbar.setTitle(formattedTime);
	}
}