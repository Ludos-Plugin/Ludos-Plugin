package fr.ludos.game;

import org.bukkit.event.Listener;

public interface TwoStepGameProcess extends GameProcess {
	void setup();
	void setdown();

	boolean isSetup();
}