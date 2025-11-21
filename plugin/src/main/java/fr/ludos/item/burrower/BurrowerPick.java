package fr.ludos.item.burrower;

import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.function.TriFunction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.game.Game;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import fr.ludos.item.LevelItem;
import fr.ludos.item.ItemUtilities;


public class BurrowerPick extends LevelItem<BurrowerPickLevels> {
	public static final String HAMMER_MODE = "mode";
	private NamespacedKey modeKey = new NamespacedKey(getGame().getPlugin(), HAMMER_MODE);

	private Boolean hammerMode = false;
	public Boolean getHammerMode() {
		return hammerMode;
	}


	public BurrowerPick(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (
			! container.has(modeKey, PersistentDataType.INTEGER)
		) {
			throw new IllegalArgumentException();
		}

		hammerMode = getPersistentData(stack, modeKey, PersistentDataType.INTEGER) == 1;
	}

	public BurrowerPick(Player owner, Game game) {
		this(owner, BurrowerPickLevels.WOODEN, game);
	}
	public BurrowerPick(Player owner, BurrowerPickLevels level, Game game) {
		this(new ItemStack(level.getMaterial()), owner, level, game);
	}

	protected BurrowerPick(ItemStack item, Player owner, Game game) {
		this(item, owner, BurrowerPickLevels.WOODEN, game);
	}
	protected BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, Game game) {
		this(item, owner, level, 0, game);
	}
	protected BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, double xp, Game game) {
		super(item, owner, level, xp, game);
		setHammerMode(false);
	}


	public void setHammerMode(Boolean value) {
		ItemMeta meta = getStack().getItemMeta();

		hammerMode = value;
		meta.getPersistentDataContainer().set(modeKey, PersistentDataType.INTEGER, value ? 1 : 0);

		getStack().setItemMeta(meta);

		updateWielding();

		updateLore();
		updateName();
	}

	public void updateWielding() {
		updateWielding(getOwner().getInventory().getItemInMainHand());
	}

	public void updateWielding(ItemStack item) {
		Boolean isInHand = item.equals(getStack());
		if (! hammerMode || ! isInHand) {
			getOwner().removePotionEffect(PotionEffectType.SLOW_DIGGING);
			return;
		}

		getOwner().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 0, false, false));
	}

	public void breakRadius(Block block, BlockFace face) {
		TriFunction<Integer, Integer, Integer, Vector> vectorGetter =
			face == BlockFace.EAST || face == BlockFace.WEST ? (x, y, z) -> new Vector(z, x, y) :
			face == BlockFace.UP || face == BlockFace.DOWN ? (x, y, z) -> new Vector(x, z, y) :
			face == BlockFace.SOUTH || face == BlockFace.NORTH ? (x, y, z) -> new Vector(x, y, z) :
			(x, y, z) -> new Vector();

		Boolean isDepthAxisPositive = face == BlockFace.EAST || face == BlockFace.UP || face == BlockFace.SOUTH;

		int radius = getLevel().getRadius();
		int depth = getLevel().getDepth();

		for (int depthOffset = 0; depthOffset <= depth; depthOffset++) {
			for (int xOffset = -radius; xOffset <= radius; xOffset++) {
				for (int yOffset = -radius; yOffset <= radius; yOffset++) {
					Vector vector = vectorGetter.apply(xOffset, yOffset, isDepthAxisPositive ? -depthOffset : depthOffset);
					Block relativeBlock = block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

					if (
						ItemUtilities.isBreakable(relativeBlock) &&
						relativeBlock.isPreferredTool(getStack())
					) {
						awardBreak(getOwner(), relativeBlock);
						relativeBlock.breakNaturally(getStack());
					}
				}
			}
		}
	}


	public void awardBreak(Player player, Block block) {
		if (getOwner() != player) {
			return;
		}

		double oreXp = BurrowerPick.getOreReward(block);
		if (oreXp != 0) {
			addXp(oreXp);
		}
	}

	public void toggleHammerMode() {
		setHammerMode(! hammerMode);

		Player owner = getOwner();
		// int radius = 1 + getLevel().getRadius() * 2;

		// String pickModeTitle = hammerMode // TODO: Translate!
		// 	? ChatColor.GREEN + "Hammer"
		// 	: ChatColor.RED + "Pickaxe";
		// String pickModeSubtitle = hammerMode // TODO: Translate!
		// 	? radius + "x" + radius
		// 	: "";

		// owner.sendTitle(pickModeTitle, pickModeSubtitle, 10, 20, 10);
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}


	public static double getOreReward(Block ore) {
		Material material = ore.getType();
		switch (material) {
			case ANCIENT_DEBRIS:
				return 60;
			case EMERALD_ORE:
				return 50;
			case DIAMOND_ORE:
				return 45;
			case GOLD_ORE:
				return 40;
			case REDSTONE_ORE:
				return 35;
			case LAPIS_ORE:
				return 30;
			case NETHER_QUARTZ_ORE:
				return 25;
			case IRON_ORE:
				return 20;
			case OBSIDIAN:
				return 15;
			case COAL_ORE:
				return 10;
			case COPPER_ORE:
				return 5;
			default:
				return material.getHardness();
		}
	}



	@Override
	public String getId() {
		return "manhuntBurrowerPick";
	}

	@Override
	protected Component getName() {
		if (hammerMode == null) {
			hammerMode = false;
		}
		return Component.text("Burrower's Pick (")
			.append(Component.text(hammerMode ? "Hammer" : "Pickaxe")
				.color(hammerMode ? NamedTextColor.RED : NamedTextColor.AQUA))
			.append(Component.text(")"))
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		int size = 1 + getLevel().getRadius() * 2;
		int depth = getLevel().getDepth() + 1;


		lore.add(
			Component.text("Mode: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(hammerMode ? "Hammer Mode" : "Pickaxe Mode")
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		lore.add(
			Component.text("Press ")
				.color(NamedTextColor.GRAY)
			.append(Component.text("Right Click (MB2) ")
				.color(NamedTextColor.YELLOW))
			.append(Component.text("to Switch Mode")
				.color(NamedTextColor.GRAY))
			.decoration(TextDecoration.ITALIC, false)
		);

		lore.add(
			Component.text("Size: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(size + "x" + size)
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		lore.add(
			Component.text("Depth: ")
				.color(NamedTextColor.GRAY)
			.append(Component.text(depth)
				.color(NamedTextColor.YELLOW))
			.decoration(TextDecoration.ITALIC, false)
		);

		return lore;
	}

	@Override
	public void setLvl(BurrowerPickLevels level) {
		super.setLvl(level);
		getStack().setType(level.getMaterial());
		getStack().removeEnchantment(Enchantment.DIG_SPEED);
		getStack().removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
		getStack().addEnchantments(level.getEnchantments());
	}

	@Override
	public BurrowerPickLevels convertToLevel(int level) {
		return BurrowerPickLevels.findByKey(level);
	}



	public static class Events extends LevelItem.Events<BurrowerPick, BurrowerPickLevels> {

		public Events(Game game) {
			super(game, BurrowerPickLevels.WOODEN, 0);
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
				return;
			}


			Player player = event.getPlayer();
			BurrowerPick pickaxe = getItem(player.getInventory().getItemInMainHand(), game);
			if (pickaxe == null) {
				return;
			}

			if (player.hasCooldown(pickaxe.getStack().getType())) {
				return;
			}

			pickaxe.toggleHammerMode();

			player.setCooldown(pickaxe.getStack().getType(), 5);
		}

		@EventHandler
		public void onSwitchItem(PlayerItemHeldEvent event) {
			Player player = event.getPlayer();

			BurrowerPick pick = BurrowerPick.findIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			if (pick == null) {
				return;
			}

			ItemStack item = player.getInventory().getItem(event.getNewSlot());
			if (item == null) {
				return;
			}

			pick.updateWielding(item);
		}

		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) {
			Player player = event.getPlayer();
			ItemStack mainHandItem = player.getInventory().getItemInMainHand();

			BurrowerPick pick = getItem(mainHandItem, game);
			if (pick == null) {
				return;
			}

			List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 100);
			if (lastTwoTargetBlocks.size() != 2) return;

			Block targetBlock = lastTwoTargetBlocks.get(1);
			Block adjacentBlock = lastTwoTargetBlocks.get(0);


			if (! pick.getHammerMode()) {
				pick.awardBreak(player, targetBlock);
				return;
			}

			BlockFace face = targetBlock.getFace(adjacentBlock);

			pick.breakRadius(targetBlock, face);
		}


		@Override
		@Nullable
		protected BurrowerPick getItem(ItemStack stack, Game game) {
			try {
				BurrowerPick pick = new BurrowerPick(stack, game);
				return pick;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected BurrowerPick createItem(Player owner, BurrowerPickLevels level, Game game) {
			return new BurrowerPick(owner, level, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}
