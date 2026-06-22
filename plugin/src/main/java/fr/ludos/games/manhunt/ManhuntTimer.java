package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.core.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class ManhuntTimer extends GameProcessBase {
	private final ManhuntGame game;

	private ManhuntRevealOptions revealOption = ManhuntRevealOptions.three_minutes;
	private BossBar bossbar;

	private boolean isRunning = false;
	public boolean isRunning() {
		return isRunning;
	}

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
		Player player = event.getPlayer();
		if (! game.getGroup().isPlayer(player)) return;

		bossbar.addPlayer(event.getPlayer());
		bossbar.setVisible(true);

		if (isRunning()) return;

		new BukkitRunnable() {
			@Override
			public void run() {
				ManhuntTeamController teamController = game.getTeamController();
				Set<Player> hunters = teamController.getTeamOnlinePlayers(teamController.hunterTeam);
				Set<Player> prey = teamController.getTeamOnlinePlayers(teamController.preyTeam);

				if (! prey.isEmpty() && ! hunters.isEmpty()) {
					resume();

					Bukkit.broadcast(
						Component.text("The game has resumed!")
					);
				}
			}
		}.runTaskLater(getPlugin(), 1);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (! game.getGroup().isPlayer(player)) return;
		if (! isRunning()) return;

		new BukkitRunnable() {
			@Override
			public void run() {
				ManhuntTeamController teamController = game.getTeamController();
				Set<Player> hunters = teamController.getTeamOnlinePlayers(teamController.hunterTeam);
				Set<Player> prey = teamController.getTeamOnlinePlayers(teamController.preyTeam);

				if (prey.isEmpty()) {
					pause();

					Bukkit.broadcast(
						Component.text("The game has been paused because the Prey has left the game.")
							.append(Component.text('\n'))
							.append(Component.text("Waiting for them to join back..."))
					);
				}
				if (hunters.isEmpty()) {
					pause();

					Bukkit.broadcast(
						Component.text("The game has been paused because all Hunters have left the game.")
							.append(Component.text('\n'))
							.append(Component.text("Waiting for them to join back..."))
					);
				}

			}
		}.runTask(getPlugin());
	}

	@Override
	protected final void onStart() {
		super.onStart();
		resume();

		for (Player player : game.getGroup().getOnlinePlayers()) {
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
		super.onStop();
	}

	public void resume() {
		if (isRunning) return;
		isRunning = true;

		task = new BukkitRunnable() {
			@Override
			public void run() {
				addSecond();
			}
		}.runTaskTimer(game.getPlugin(), 20, 20);
	}

	public void pause() {
		if (! isRunning) return;
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
		double timerDuration = (double) revealOption.getDuration();

		double progress = ((double)totalSeconds % timerDuration) / timerDuration;
		bossbar.setProgress(progress);
		bossbar.setTitle(formattedTime);

		if (totalSeconds % timerDuration == 0 && totalSeconds != 0) {
			game.revealPrey();
		}
	}
}
