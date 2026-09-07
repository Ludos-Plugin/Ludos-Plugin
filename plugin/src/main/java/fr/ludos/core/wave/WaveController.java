package fr.ludos.core.wave;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.item.SpecialItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Controller for managing waves in a {@link WaveGame}.
 * Handles wave progression, scheduling, loadouts and related events.
 */
public abstract class WaveController extends GameProcessBase {
	private boolean scheduled = false;
	private boolean evaluating = false;

	private final WaveGame game;
	public final WaveGame game() {
		return this.game;
	}

	@Override
	protected JavaPlugin plugin() {
		return game.plugin();
	}

	private int wave = 0;
	public final int getCurrentWave() {
		return this.wave;
	}
	public final int getCurrentWaveNumber() {
		return this.wave + 1;
	}
	private final int maxWaves;
	public final int getMaxWaves() {
		return this.maxWaves;
	}

	@Nullable
	private final WaveLoadoutService loadout;
	public final WaveLoadoutService getLoadout() {
		return this.loadout;
	}

	protected WaveController(WaveGame game, int maxWaves, @Nullable WaveLoadoutService loadout) {
		this.game = game;
		this.maxWaves = maxWaves;
		this.loadout = loadout;
	}



	@EventHandler
	public void _onPlayerQuit(PlayerQuitEvent event) {
		if (evaluating) return;
		if (game.worldManager().isLobbyStarted()) return;
		if (! game.teamController().contains(event.getPlayer())) return;

		evaluating = true;
		Bukkit.getScheduler().runTask(plugin(), () -> {
			evaluateWaveState();
			evaluating = false;
		});
	}

	@EventHandler
	public void _onEntityDeath(EntityDeathEvent event) {
		if (evaluating) return;
		if (game.worldManager().isLobbyStarted()) return;
		if (event.getEntity().getWorld() != game.worldManager().getWorld()) return;

		evaluating = true;
		Bukkit.getScheduler().runTask(plugin(), () -> {
			evaluateWaveState();
			evaluating = false;
		});
	}

	public void applyLoadout(Player player) {
		if (loadout != null) {
			loadout.applyBaseKit(player);
		}
		SpecialItem.Events.refreshPlayerInventory(game, player);
	}

	protected abstract void nextWave();
	public abstract void startWave();
	protected abstract void evaluateWaveState();

	protected Component getCompletionText() {
		return Component.text("Waves completed!").color(NamedTextColor.GOLD);
	}

	public final void completeCurrentWave() {
		wave ++;

		if (maxWaves > 0 && wave >= maxWaves) {
			scheduleReturn();
		} else {
			scheduleNextWave();
		}
	}

	public final void scheduleReturn() {
		if (scheduled) return;

		scheduled = true;
		long delay = 20 * 5;

		onScheduleEnd();
		Bukkit.getScheduler().runTaskLater(plugin(), this::scheduledEnd, delay);
	}
	private void scheduledEnd() {
		scheduled = false;
		stop();
	}

	public void onScheduleEnd() { }


	public final void scheduleNextWave() {
		if (scheduled) return;

		scheduled = true;
		long delay = 20 * 2;

		onScheduleNextWave();
		Bukkit.getScheduler().runTaskLater(plugin(), this::scheduledNextWave, delay);
	}
	private void scheduledNextWave() {
		scheduled = false;
		nextWave();
	}

	public void onScheduleNextWave() {
		game.group().sendMessage(
			Component.text("Wave " + getCurrentWaveNumber() + " starting...")
				.color(NamedTextColor.YELLOW)
		);
	}
}
