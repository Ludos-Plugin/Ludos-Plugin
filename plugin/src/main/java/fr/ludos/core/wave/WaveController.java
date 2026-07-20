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

	private final WaveGame game;
	public final WaveGame getGame() {
		return this.game;
	}

	@Override
	protected JavaPlugin getPlugin() {
		return game.getPlugin();
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
		if (game.getWorldManager().isLobbyStarted()) return;
		if (! game.getTeamController().contains(event.getPlayer())) return;
		Bukkit.getScheduler().runTask(getPlugin(), this::evaluateWaveState);
	}

	@EventHandler
	public void _onEntityDeath(EntityDeathEvent event) {
		if (game.getWorldManager().isLobbyStarted()) return;
		if (event.getEntity().getWorld() != game.getWorldManager().getWorld()) return;
		Bukkit.getScheduler().runTask(getPlugin(), this::evaluateWaveState);
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

	public void scheduleReturn() {
		if (scheduled) return;

		scheduled = true;
		long delay = 20 * 5;

		Bukkit.getScheduler().runTaskLater(getPlugin(), this::stop, delay);
	}

	public void scheduleNextWave() {
		if (scheduled) return;

		scheduled = true;
		long delay = 20 * 2;
		if (maxWaves > 0 && wave >= maxWaves) {
			Bukkit.broadcast(getCompletionText());
			Bukkit.getScheduler().runTaskLater(getPlugin(), this::stop, delay);
		}
		else {
			wave ++;
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
				scheduled = false;
				nextWave();
			}, delay);
		}
	}
}
