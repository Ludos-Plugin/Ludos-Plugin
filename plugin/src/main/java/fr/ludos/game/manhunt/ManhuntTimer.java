package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
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


public class ManhuntTimer extends GameProcessBase {
	private final ManhuntGame game;

	private ManhuntRevealOptions revealOption = ManhuntRevealOptions.three_minutes;
	private BossBar bossbar;

	private boolean isRunning = false;

	private BukkitTask task;

	private long totalSeconds;
	private String formattedTime;

	@Override
	protected JavaPlugin getPlugin() {
		return game.getPlugin();
	}

	public ManhuntTimer(ManhuntGame game, ManhuntRevealOptions revealOption) {
		this.game = game;
		this.revealOption = revealOption;

		bossbar = Bukkit.createBossBar("Timer", BarColor.RED, BarStyle.SEGMENTED_12);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		bossbar.addPlayer(event.getPlayer());
		bossbar.setVisible(true);
	}

	@Override
	protected final void onStart() {
		resume();

		for (Player player : Bukkit.getOnlinePlayers()) {
			bossbar.addPlayer(player);
		}
		bossbar.setVisible(true);

	}

	@Override
	protected final void onStop() {
		pause();

		bossbar.removeAll();
		bossbar.setVisible(false);

		Bukkit.getServer().broadcast(
			Component.text("Timer ended. Final Time : " + formattedTime)
				.color(NamedTextColor.GREEN)
		);
	}

	public void resume() {
		isRunning = true;

		task = new BukkitRunnable() {
			@Override
			public void run() {
				addSecond();
			}
		}.runTaskTimer(game.getPlugin(), 20, 20);
	}

	public void pause() {
		if (! isStarted() || ! isRunning) return;

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
		double timerDuration = (double) revealOption.getDuration();

		double progress = ((double)totalSeconds % timerDuration) / timerDuration;
		bossbar.setProgress(progress);
		bossbar.setTitle(formattedTime);

		if (totalSeconds % timerDuration == 0 && totalSeconds != 0) {
			game.revealPrey();
		}
	}
}
