package fr.ludos.game;

import org.bukkit.plugin.java.JavaPlugin;

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
			throw new IllegalCallerException("Cannot start game process: not setup");
		}
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		setdown();
	}
}
