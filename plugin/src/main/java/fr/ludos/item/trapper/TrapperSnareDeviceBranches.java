package fr.ludos.item.trapper;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItemBranches;
import io.papermc.paper.entity.LookAnchor;


public enum TrapperSnareDeviceBranches implements SpecialItemBranches<TrapperSnareDeviceBranches> {
	REVEALING (
		Component.text("REVEALING").color(TextColor.color(0xFFAA00)).decorate(TextDecoration.ITALIC),
		Component.text("REVEALING Description")
	) {
		private final Material type = Material.ENDER_EYE;

		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			if (block.getType() == Material.SAND) {
				trapBlock.getLocation().getBlock().setType(Material.SAND);
			} else {
				trapBlock.getLocation().getBlock().setType(Material.GRASS_BLOCK);
			}

			owner.sendMessage(block.getType().toString());
			return new TrapperTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), this);
		}

		@Override
		public Boolean executeEffect(Player target, TrapperTrap info) {
			if (target.getLocation().distance(info.getLocation()) >= 3) return false;
			Material blockType = info.getLocation().getBlock().getType();
			if (blockType != Material.GRASS_BLOCK && blockType != Material.DIRT && blockType != Material.SAND) return true;

			info.getOwner().sendMessage("Trap triggered !");
			target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 1));

			info.getLocation().getBlock().setType(Material.AIR);

			// TODO Envoyer un event au plugin pour notifier que le joueur a été révélé et declencher le reveal de la position du joueur
			//Bukkit.getServer().getPluginManager().callEvent()

			return true;
		}

		@Override
		public void onSwitchBranch(BranchItem<TrapperSnareDeviceBranches> item) {
			item.getStack().setType(type);
		}
	},


	SLOWING	(
		Component.text("SLOWING").color(TextColor.color(0x0000FF)).decorate(TextDecoration.ITALIC),
		Component.text("SLOWING Description")
	) {
		private final Material type = Material.COBWEB;


		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			trapBlock.getLocation().getBlock().setType(Material.COARSE_DIRT);

			return new TrapperTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), this);
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

		@Override
		public void onSwitchBranch(BranchItem<TrapperSnareDeviceBranches> item) {
			item.getStack().setType(type);
		}
	},

	REBOUND (
		Component.text("REBOUND").color(TextColor.color(0x00FF00)).decorate(TextDecoration.ITALIC),
		Component.text("REBOUND Description")
	) {
		private final Material type = Material.END_ROD;


		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);
			trapBlock.getLocation().getBlock().setType(Material.END_ROD);

			return new TrapperTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), this);
		}

		@Override
		public Boolean executeEffect(Player target, TrapperTrap info) {
			if (target.getLocation().distance(info.getLocation()) >= 7) return false;
			if (info.getLocation().getBlock().getType() != Material.END_ROD) return true;

			Player owner = info.getOwner();
			info.getLocation().getBlock().setType(Material.AIR);

			owner.teleport(info.getLocation());
			owner.lookAt(target, LookAnchor.EYES, LookAnchor.EYES);
			target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 10, 1));

			owner.sendMessage("Trap triggered !");

			return true;
		}

		@Override
		public void onSwitchBranch(BranchItem<TrapperSnareDeviceBranches> item) {
			item.getStack().setType(type);
		}
	};


	public final static TrapperSnareDeviceBranches[] values = TrapperSnareDeviceBranches.values();

	private final Component name;
	@Override
	public Component getName() {
		return name;
	}

	private final Component description;
	@Override
	public Component getDescription() {
		return description;
	}


	private TrapperSnareDeviceBranches(Component name, Component description) {
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

	@Override
	public int index() {
		return ArrayUtils.indexOf(values(), this);
	}

	public abstract TrapperTrap createTrap(Player owner, Block block, BlockFace face);
	public abstract Boolean executeEffect(Player target, TrapperTrap info);
}