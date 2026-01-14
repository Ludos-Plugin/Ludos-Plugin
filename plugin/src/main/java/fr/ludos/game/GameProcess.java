package fr.ludos.game;

import org.bukkit.event.Listener;

public interface GameProcess extends Listener {
	void start();
	void stop();

	boolean isStarted();
}