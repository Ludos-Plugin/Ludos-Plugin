package fr.ludos.game;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

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

		Bukkit.getPluginManager().registerEvents(this, getPlugin());

		try {
			onStart();
		} catch (Exception e) {
			getPlugin().getLogger().severe("Error while starting game process: " + e.getMessage());
			e.printStackTrace();
			stop();
		}
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
}
