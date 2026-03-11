package fr.ludos.game.alien;

import org.bukkit.event.Listener;

public interface AlienQuest extends Listener {
	String getId();

	String getDisplayName();

	boolean isCompleted();

	void start();

	void stop();
}