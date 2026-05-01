package fr.ludos.item.assassin.trap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class AssassinTrap {
	private final Player owner;
	public Player getOwner() {
		return owner;
	}

	private final Location location;
	public Location getLocation() {
		return location;
	}

	private final World world;
	public World getWorld() {
		return world;
	}


	public AssassinTrap(Player owner, Location location, World world) {
		this.owner = owner;
		this.location = location;
		this.world = world;
	}

	public abstract Boolean canTriggerEffect(LivingEntity target);
	public abstract void triggerEffect(LivingEntity target);
}