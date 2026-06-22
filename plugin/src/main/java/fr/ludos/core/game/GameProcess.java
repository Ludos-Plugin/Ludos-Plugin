package fr.ludos.core.game;

import org.bukkit.event.Listener;

import fr.ludos.core.Process;

public interface GameProcess extends Process, Listener {
	void stop();

	boolean isStarted();
}