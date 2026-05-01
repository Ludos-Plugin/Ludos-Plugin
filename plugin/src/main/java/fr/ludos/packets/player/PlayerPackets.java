package fr.ludos.packets.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface PlayerPackets {
	void setGlowForPlayer(Entity target, Player viewer, boolean value);
}