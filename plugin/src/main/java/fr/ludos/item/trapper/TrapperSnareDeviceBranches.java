package fr.ludos.item.trapper;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public enum TrapperSnareDeviceBranches implements BranchItem.Branch<TrapperSnareDeviceBranches> {
	REVEALING (
		Component.text("Revealing")
			.color(NamedTextColor.YELLOW)
			.decorate(TextDecoration.ITALIC),
		Component.text("Revealing Description")
	) {
		private final Material type = Material.FERMENTED_SPIDER_EYE;

		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			trapBlock.getLocation().getBlock().setType(Material.SUNFLOWER);

			owner.sendMessage(block.getType().toString());
			return new TrapperTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), this);
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

		@Override
		public void onEquip(SpecialItem item) {
			item.getStack().setType(type);
		}
		@Override
		public void onUnequip(SpecialItem item) { }
	},


	SLOWING	(
		Component.text("Impeding")
			.color(NamedTextColor.DARK_BLUE)
			.decorate(TextDecoration.ITALIC),
		Component.text("Impeding Description")
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
		public void onEquip(SpecialItem item) {
			item.getStack().setType(type);
		}
		@Override
		public void onUnequip(SpecialItem item) { }
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

	public abstract TrapperTrap createTrap(Player owner, Block block, BlockFace face);
	public abstract Boolean executeEffect(Player target, TrapperTrap info);
}