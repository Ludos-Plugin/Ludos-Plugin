package fr.ludos.item.assassin.trap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class BlockTrap extends AssassinTrap {
	private final Material type;
	protected Material getType() {
		return type;
	}

	public BlockTrap(Player owner, Location location, Vector range, World world, Material type) {
		super(owner, location, range, world);
		this.type = type;
		location.getBlock().setType(type);
	}

	@Override
	public final void triggerEffect(LivingEntity target) {
		if (this.getLocation().getBlock().getType() == this.getType()) {
			this.getLocation().getBlock().setType(Material.AIR);
		}
		triggerBlockTrapEffect(target);
	}

	public abstract void triggerBlockTrapEffect(LivingEntity target);
}
