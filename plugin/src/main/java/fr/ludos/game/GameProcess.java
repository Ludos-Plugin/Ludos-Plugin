package fr.ludos.game;

import org.bukkit.event.Listener;

import fr.ludos.Process;

public interface GameProcess extends Process, Listener {
	void stop();

	boolean isStarted();
}