package fr.ludos.game;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;

public abstract class GameProcessBase implements GameProcess {
	private boolean started = false;
	public final boolean isStarted() {
		return started;
	}

	protected abstract JavaPlugin getPlugin();

	public void start() {
		if (started) return;

		started = true;

		onInit();

		Bukkit.getPluginManager().registerEvents(this, getPlugin());

		onStart();
	}
	protected void onInit() { }
	protected void onStart() { }

	public void stop() {
		if (! started) return;
		started = false;

		onStop();

		HandlerList.unregisterAll(this);

		onDeinit();
	}
	protected void onDeinit() { }
	protected void onStop() { }
}
