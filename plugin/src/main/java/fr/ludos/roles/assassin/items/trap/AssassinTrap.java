package fr.ludos.roles.assassin.items.trap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class AssassinTrap {
	private final Player owner;
	public Player getOwner() {
		return owner;
	}

	private final Location location;
	public Location getLocation() {
		return location;
	}

	private final Vector range;
	public Vector getRange() {
		return range;
	}

	private final World world;
	public World getWorld() {
		return world;
	}


	public AssassinTrap(Player owner, Location location, Vector range, World world) {
		this.owner = owner;
		this.location = location;
		this.range = range;
		this.world = world;
	}

	public Boolean canTriggerEffect(LivingEntity target) {
		return true;
	}
	public abstract void triggerEffect(LivingEntity target);
}