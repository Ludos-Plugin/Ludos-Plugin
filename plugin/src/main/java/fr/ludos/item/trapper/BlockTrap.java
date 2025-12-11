package fr.ludos.item.trapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class BlockTrap extends TrapperTrap {
	private final Material type;
	protected Material getType() {
		return type;
	}

	public BlockTrap(Player owner, Location location, World world, Material type) {
		super(owner, location, world);
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
