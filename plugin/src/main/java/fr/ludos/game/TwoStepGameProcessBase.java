package fr.ludos.game;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class TwoStepGameProcessBase implements TwoStepGameProcess {
	private boolean started = false;
	public final boolean isStarted() {
		return started;
	}
	private boolean setup = false;
	public final boolean isSetup() {
		return setup;
	}

	public final boolean isRunning() {
		return isStarted() || isSetup();
	}

	public boolean isClear() {
		return ! isStarted();
	}

	protected abstract JavaPlugin getPlugin();


	public void setup() {
		if (setup) return;
		setup = true;

		onSetup();
	}
	protected void onSetup() { }

	public final void start() {
		if (! setup) throw new IllegalStateException("Cannot start game process: not setup");
		if (started) return;

		started = true;

		onInit();

		Bukkit.getPluginManager().registerEvents(this, getPlugin());

		onStart();
	}
	protected void onInit() { }
	protected void onStart() { }

	public final void stop() {
		if (started) {
			started = false;

			onStop();

			HandlerList.unregisterAll(this);

			onDeinit();
		}

		setdown();
	}
	protected void onDeinit() { }
	protected void onStop() { }

	public void setdown() {
		if (! setup) return;
		setup = false;

		onSetdown();
	}
	protected void onSetdown() { }

	public void restart() {
		stop();
		start();
	}
}
