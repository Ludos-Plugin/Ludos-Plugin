package fr.ludos.item.trapper;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public enum TrapperSnareDeviceBranches implements BranchItem.Branch<TrapperSnareDeviceBranches> {
	REVEALING (
		Component.text("Revealing")
			.color(NamedTextColor.YELLOW)
			.decorate(TextDecoration.ITALIC),
		Component.text("Revealing Description"),
		Material.ENDER_EYE
	) {
		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			return new BlockTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), block.getType()) {
				@Override
				public Boolean canTriggerEffect(LivingEntity target) {
					return target.getLocation().distance(this.getLocation()) < 3;
				}

				@Override
				public void triggerBlockTrapEffect(LivingEntity target) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 20, 1));
					// TODO: Envoyer un event au plugin pour notifier que le joueur a été révélé et declencher le reveal de la position du joueur
					//Bukkit.getServer().getPluginManager().callEvent()
				}
			};
		}
	},


	SLOWING	(
		Component.text("Impeding")
			.color(NamedTextColor.DARK_BLUE)
			.decorate(TextDecoration.ITALIC),
		Component.text("Impeding Description"),
		Material.COBWEB
	) {
		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			return new BlockTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), Material.COARSE_DIRT) {
				@Override
				public Boolean canTriggerEffect(LivingEntity target) {
					return target.getLocation().distance(this.getLocation()) < 7;
				}

				@Override
				public void triggerBlockTrapEffect(LivingEntity target) {
					target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 1));

					// TODO: Find an algorithm to make a web-like structure around the trap location
					// For now, just set the block to cobweb
					this.getLocation().getBlock().setType(Material.COBWEB);
				}
			};
		}
	},


	REBOUND	(
		Component.text("Rebound")
			.color(NamedTextColor.LIGHT_PURPLE)
			.decorate(TextDecoration.ITALIC),
		Component.text("Rebound Description"),
		Material.ENDER_PEARL
	) {
		@Override
		public TrapperTrap createTrap(Player owner, Block block, BlockFace face) {
			Block trapBlock = block.getRelative(face);

			return new BlockTrap(owner, trapBlock.getLocation(), trapBlock.getWorld(), Material.END_ROD) {
				@Override
				public Boolean canTriggerEffect(LivingEntity target) {
					return target.getLocation().distance(this.getLocation()) < 7;
				}

				@Override
				public void triggerBlockTrapEffect(LivingEntity target) {
					this.getOwner().teleport(this.getLocation(), TeleportCause.ENDER_PEARL);
					this.getOwner().lookAt(target, LookAnchor.EYES, LookAnchor.EYES);
				}
			};
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

	private final Material type;
	public Material getType() {
		return type;
	}

	private final int limit;
	public int getLimit() {
		return limit;
	}


	private TrapperSnareDeviceBranches(Component name, Component description, Material type) {
		this(name, description, type, 0);
	}
	private TrapperSnareDeviceBranches(Component name, Component description, Material type, int limit) {
		this.name = name;
		this.description = description;
		this.type = type;
		this.limit = limit;
	}

	@Override
	public void onEquip(SpecialItem item) {
		item.getStack().setType(type);
	}
	@Override
	public void onUnequip(SpecialItem item) {}

	public abstract TrapperTrap createTrap(Player owner, Block block, BlockFace face);
}