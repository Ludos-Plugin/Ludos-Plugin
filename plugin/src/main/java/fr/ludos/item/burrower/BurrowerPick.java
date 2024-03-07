package fr.ludos.item.burrower;

import fr.ludos.Main;
import fr.ludos.role.BurrowerRole;
import fr.ludos.item.LevelItem;
import fr.ludos.item.ItemUtilities;

import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;



import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;


public class BurrowerPick extends LevelItem<BurrowerPickLevels> {
	public static final String HAMMER_MODE = "mode";
	private NamespacedKey modeKey = new NamespacedKey(Main.getInstance(), HAMMER_MODE);

	private Boolean hammerMode = false;


	public BurrowerPick(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (
			! container.has(modeKey, PersistentDataType.INTEGER)
		) {
			throw new IllegalArgumentException();
		}

		hammerMode = getHammerModeFromItem(stack, modeKey);
	}

	public BurrowerPick(Player owner) {
		this(owner, BurrowerPickLevels.WOODEN);
	}
	public BurrowerPick(Player owner, BurrowerPickLevels level) {
		this(new ItemStack(level.getMaterial()), owner, level);
	}

	protected BurrowerPick(ItemStack item, Player owner) {
		this(item, owner, BurrowerPickLevels.WOODEN);
	}
	protected BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level) {
		this(item, owner, level, 0);
	}
	protected BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, double xp) {
		super(item, owner, level, xp);
		setHammerMode(false);
	}


	protected static Boolean getHammerModeFromItem(ItemStack item, NamespacedKey key) {
		return getPersistentData(item, key, PersistentDataType.INTEGER) == 1;
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

	public void breakRadius(Location location, BlockFace face) {
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
					Block block = location.getBlock().getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

					if (ItemUtilities.isBreakable(block) && block.isPreferredTool(getStack())) {
						awardBreak(getOwner(), block);
						block.breakNaturally(getStack());
					}
				}
			}
		}
	}

	@Override
	public BurrowerPickLevels convertToLevel(int level) {
		return BurrowerPickLevels.findByKey(level);
	}


	public Boolean getHammerMode() {
		return hammerMode;
	}

	@Override
	protected String getName() {
		if (hammerMode == null) {
			hammerMode = false;
		}
		return "Burrower's Pick (" + (hammerMode ? ChatColor.RED + "Hammer" : ChatColor.AQUA + "Pickaxe") + ChatColor.RESET.toString() + ChatColor.WHITE + ")"; // TODO: Translate
	}
	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		int size = 1 + getLevel().getRadius() * 2;
		int depth = getLevel().getDepth() + 1;
		String modeFormatted = ChatColor.GRAY + "Mode: " + ChatColor.YELLOW + (hammerMode ? "Hammer Mode" : "Pickaxe Mode");
		String sizeFormatted = ChatColor.GRAY + "Size: " + ChatColor.YELLOW + (size + "x" + size);
		String depthFormatted = ChatColor.GRAY + "Depth: " + ChatColor.YELLOW + (depth);
		lore.add(modeFormatted);
		lore.add(sizeFormatted);
		lore.add(depthFormatted);

		return lore;
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

	public void addLvl() {
		if (getLevel().isMax()) {
			return;
		}

		setLvl(getLevel().getNext());
		if (getOwner() != null) {
			getOwner().sendMessage(ChatColor.GREEN + "Your pickaxe has leveled up!"); // TODO: Translate
		}
	}

	@Override
	public void setLvl(BurrowerPickLevels level) {
		super.setLvl(level);
		getStack().setType(level.getMaterial());
		getStack().removeEnchantment(Enchantment.DIG_SPEED);
		getStack().removeEnchantment(Enchantment.LOOT_BONUS_BLOCKS);
		getStack().addEnchantments(level.getEnchantments());
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


	public static class Events extends LevelItem.Events<BurrowerPick, BurrowerPickLevels> {

		public Events() {
			super(BurrowerRole.id, BurrowerPickLevels.WOODEN);
		}


		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
				return;
			}


			Player player = event.getPlayer();
			BurrowerPick pickaxe = getItem(player.getInventory().getItemInMainHand());
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

			BurrowerPick pick = BurrowerPick.findIn(player.getInventory(), this::getItem);
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

			BurrowerPick pick = getItem(mainHandItem);
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

			pick.breakRadius(targetBlock.getLocation(), face);
		}

		@Override
		@Nullable
		protected BurrowerPick getItem(ItemStack stack) {
			try {
				BurrowerPick pick = new BurrowerPick(stack);
				return pick;
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
		@Override
		protected BurrowerPick createItem(Player owner, BurrowerPickLevels level) {
			return new BurrowerPick(owner, level);
		}
	}
}
