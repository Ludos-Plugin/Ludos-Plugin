package fr.ludos.game;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.role.Role;

public abstract class TwoStepGameProcessBase extends GameProcessBase implements TwoStepGameProcess {
	private boolean setup = false;
	public final boolean isSetup() {
		return setup;
	}

	protected abstract JavaPlugin getPlugin();

	public void setup() {
		if (setup) return;
		setup = true;

		onSetup();
	}
	protected void onSetup() { }

	public void setdown() {
		if (! setup) return;
		setup = false;

		onSetdown();
	}
	protected void onSetdown() { }


	@Override
	public void start() {
		if (! setup) {
			getPlugin().getLogger().severe("Cannot start game process: not setup");
			return;
		}
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		setdown();
	}
}
