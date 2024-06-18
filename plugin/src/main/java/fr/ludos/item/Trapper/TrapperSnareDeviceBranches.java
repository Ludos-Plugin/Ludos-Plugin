package fr.ludos.item.trapper;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItemBranches;
import net.md_5.bungee.api.ChatColor;

public enum TrapperSnareDeviceBranches implements SpecialItemBranches<TrapperSnareDeviceBranches> {
	BOOSTING (ChatColor.GREEN.toString() + ChatColor.ITALIC + "BOOSTING", "BOOSTING Description") {
		@Override
		public void createTrap(Player owner, Block block) {
			TrapperTrap boostingTrap = new TrapperTrap(owner, block.getLocation(), block.getWorld(), 5);
			TrapperTrap.traps.add(boostingTrap);

			block.getLocation().getBlock().setType(Material.COARSE_DIRT);
		}

		@Override
		public void executeEffect(Player target, TrapperTrap info) {
			info.getOwner().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1));
			info.getOwner().sendMessage("Trap triggered !");

			// this.getWorld().getBlockAt(getLocation()).setType(Material.COBWEB);
			if (info.getLocation().getBlock().getType() == Material.COARSE_DIRT) {
				info.getLocation().getBlock().setType(Material.AIR);
			}
		}
	},


	REVEALING (ChatColor.GOLD.toString() + ChatColor.ITALIC + "REVEALING", "REVEALING Description") {
		@Override
		public void createTrap(Player owner, Block block) {
			TrapperTrap revealingTrap = new TrapperTrap(owner, block.getLocation(), block.getWorld(), 3);
			TrapperTrap.traps.add(revealingTrap);
		}

		@Override
		public void executeEffect(Player target, TrapperTrap info) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 1));
            info.getOwner().sendMessage("Trap triggered !");

            if (info.getLocation().getBlock().getType() == Material.SUNFLOWER) {
                info.getLocation().getBlock().setType(Material.AIR);
            }

            // TODO Evoyer un event au plugin pour notifier que le joueur a été révélé et declencher le reveal de la position du joueur
            //Bukkit.getServer().getPluginManager().callEvent()
		}
	},


	SLOWING	(ChatColor.BLUE.toString() + ChatColor.ITALIC + "SLOWING", "SLOWING Description") {
		@Override
		public void createTrap(Player owner, Block block) {
			TrapperTrap slowingTrap = new TrapperTrap(owner, block.getLocation(), block.getWorld(), 7);
			TrapperTrap.traps.add(slowingTrap);
		}

		@Override
		public void executeEffect(Player target, TrapperTrap info) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 1));
            info.getOwner().sendMessage("Trap triggered !");

            if (info.getLocation().getBlock().getType() == Material.COBWEB) {
                info.getLocation().getBlock().setType(Material.AIR);
            }
		}
	};


	public final static TrapperSnareDeviceBranches[] values = TrapperSnareDeviceBranches.values();

	private String name;
	public String getName() {
		return name;
	}

	private String description;
	public String getDescription() {
		return description;
	}


	private TrapperSnareDeviceBranches(String name, String description) {
		this.name = name;
		this.description = description;
	}


	@Nullable
	public static TrapperSnareDeviceBranches findByKey(int i) {
		if ( i >= values.length ) {
			return null;
		}
		return values[i];
	}


	public int index() {
		return ArrayUtils.indexOf(values(), this);
	}

	public abstract void createTrap(Player owner, Block block);
	public abstract void executeEffect(Player target, TrapperTrap info);
}