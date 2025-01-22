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
		public TrapperTrap createTrap(Player owner, Block block) {
			block.getLocation().getBlock().setType(Material.OAK_LEAVES);
			owner.sendMessage("Trap placed !");
			return new TrapperTrap(owner, block.getLocation(), block.getWorld(), this);
		}

		@Override
		public Boolean executeEffect(Player target, TrapperTrap info) {
			if (target.getLocation().distance(info.getLocation()) >= 5) return false;
			if (info.getLocation().getBlock().getType() != Material.OAK_LEAVES) return true;

			info.getOwner().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 1));
			info.getOwner().sendMessage("Trap triggered !");

			info.getWorld().getBlockAt(info.getLocation()).setType(Material.AIR);

			return true;
		}
	},


	REVEALING (ChatColor.GOLD.toString() + ChatColor.ITALIC + "REVEALING", "REVEALING Description") {
		@Override
		public TrapperTrap createTrap(Player owner, Block block) {
			block.getLocation().getBlock().setType(Material.SUNFLOWER);
			owner.sendMessage("Trap placed !");
			return new TrapperTrap(owner, block.getLocation(), block.getWorld(), this);
		}

		@Override
		public Boolean executeEffect(Player target, TrapperTrap info) {
			if (target.getLocation().distance(info.getLocation()) >= 3) return false;
            if (info.getLocation().getBlock().getType() != Material.SUNFLOWER) return true;

            info.getOwner().sendMessage("Trap triggered !");
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 1));

			info.getLocation().getBlock().setType(Material.AIR);

            // TODO Envoyer un event au plugin pour notifier que le joueur a été révélé et declencher le reveal de la position du joueur
            //Bukkit.getServer().getPluginManager().callEvent()

			return true;
		}
	},


	SLOWING	(ChatColor.BLUE.toString() + ChatColor.ITALIC + "SLOWING", "SLOWING Description") {
		@Override
		public TrapperTrap createTrap(Player owner, Block block) {
			block.getLocation().getBlock().setType(Material.COARSE_DIRT);
			owner.sendMessage("Trap placed !");
			return new TrapperTrap(owner, block.getLocation(), block.getWorld(), this);
		}

		@Override
		public Boolean executeEffect(Player target, TrapperTrap info) {
			if (target.getLocation().distance(info.getLocation()) >= 7) return false;
			if (info.getLocation().getBlock().getType() != Material.COARSE_DIRT) return true;

            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 1));
            info.getOwner().sendMessage("Trap triggered !");

			info.getLocation().getBlock().setType(Material.COBWEB);

			return true;
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

	public abstract TrapperTrap createTrap(Player owner, Block block);
	public abstract Boolean executeEffect(Player target, TrapperTrap info);
}