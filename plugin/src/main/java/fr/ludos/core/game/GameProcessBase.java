package fr.ludos.core.game;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An {@link GameProcess} implementation that can be initialized, started, stopped and de-initialized.
 */
public abstract class GameProcessBase implements GameProcess {
	private boolean started = false;
	public final boolean isStarted() {
		return started;
	}

	protected abstract JavaPlugin getPlugin();

	public final void start() {
		if (started) return;

		started = true;

		onInit();

		getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());

		onStart();
	}
	protected void onInit() { }
	protected void onStart() { }

	public final void stop() {
		if (! started) return;
		started = false;

		onStop();

		HandlerList.unregisterAll(this);

		onDeinit();
	}
	protected void onDeinit() { }
	protected void onStop() { }

	public void restart() {
		stop();
		start();
	}

	public boolean isClear() {
		return ! isStarted();
	}
}
