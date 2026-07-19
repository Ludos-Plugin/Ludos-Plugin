package fr.ludos.core.game;

import org.bukkit.event.Listener;

import fr.ludos.core.Process;

/**
 * A {@link Process} that can be started and stopped and also report on its current state.
 */
public interface GameProcess extends Process, Listener {
	void start();
	void stop();

	boolean isStarted();
}