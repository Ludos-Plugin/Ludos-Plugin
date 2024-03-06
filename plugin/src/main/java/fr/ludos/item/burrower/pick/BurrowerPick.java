package fr.ludos.item.burrower.pick;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import fr.ludos.item.LevelItem;

import fr.ludos.item.ItemUtilities;
import fr.ludos.item.LevelItem;


/**
 * Pickaxe is a class that represents a special item, "Miner Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to give a miner pickaxe to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, and finally diamond pickaxe.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call addPickaxeInventory(player) to give a miner pickaxe to the specified player.
 * <br><br>
 * - Call levelPickaxe(player, xp) with the XP gained from mining to level up the pickaxe.
 * <br><br>
 * Example:
 * <pre>{@code
 * Pickaxe pickaxe = new Pickaxe();
 * pickaxe.addPickaxeInventory(player);
 * pickaxe.levelPickaxe(player, xp);
 * }</pre>
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public class BurrowerPick extends LevelItem<BurrowerPickLevels> {

	private Boolean hammerMode = false;


	public BurrowerPick(ItemStack stack) throws IllegalArgumentException {
		super(stack);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (
			! container.has(BurrowerPickEvents.getModeKey(), PersistentDataType.INTEGER)
		) {
			throw new IllegalArgumentException();
		}

		hammerMode = getHammerModeFromItem(stack, BurrowerPickEvents.getModeKey());
	}

	public BurrowerPick(Player owner) {
		this(owner, BurrowerPickLevels.WOODEN);
	}
	public BurrowerPick(Player owner, BurrowerPickLevels level) {
		this(new ItemStack(level.getMaterial()), owner, level);
	}

	public BurrowerPick(ItemStack item, Player owner) {
		this(item, owner, BurrowerPickLevels.WOODEN);
	}
	public BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level) {
		this(item, owner, level, 0);
	}
	public BurrowerPick(ItemStack item, Player owner, BurrowerPickLevels level, double xp) {
		super(item, owner, level, xp);
		setHammerMode(false);
	}


	protected static Boolean getHammerModeFromItem(ItemStack item, NamespacedKey key) {
		return getPersistentData(item, key, PersistentDataType.INTEGER) == 1;
	}


	public void setHammerMode(Boolean value) {
		ItemMeta meta = getStack().getItemMeta();

		this.hammerMode = value;
		meta.getPersistentDataContainer().set(BurrowerPickEvents.getModeKey(), PersistentDataType.INTEGER, value ? 1 : 0);

		getStack().setItemMeta(meta);

		updateLore();
		updateName();
	}

	public void breakRadius(Location location, BlockFace face, Integer radius) {
		BiFunction<Integer, Integer, Vector> vectorGetter =
			face == BlockFace.EAST || face == BlockFace.WEST ? (x, y) -> new Vector(0, x, y) :
			face == BlockFace.UP || face == BlockFace.DOWN ? (x, y) -> new Vector(x, 0, y) :
			face == BlockFace.NORTH || face == BlockFace.SOUTH ? (x, y) -> new Vector(x, y, 0) :
			(x, y) -> new Vector();


		for (int xOffset = -radius; xOffset <= radius; xOffset++) {
			for (int zOffset = -radius; zOffset <= radius; zOffset++) {
				Vector vector = vectorGetter.apply(xOffset, zOffset);
				Block block = location.getBlock().getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());

				if (ItemUtilities.isBreakable(block) && block.isPreferredTool(getStack())) {
					awardBreak(getOwner(), block);
					block.breakNaturally(getStack());
				}
			}
		}
	}

	@Override
	public BurrowerPickLevels convertToLevel(int level) {
		return BurrowerPickLevels.findByKey(level);
	}

	@Override
	public NamespacedKey getOwnerKey() {
		return BurrowerPickEvents.getOwnerKey();
	}

	@Override
	public NamespacedKey getLvlKey() {
		return BurrowerPickEvents.getLvlKey();
	}

	@Override
	public NamespacedKey getXpKey() {
		return BurrowerPickEvents.getXpKey();
	}


	public Boolean getHammerMode() {
		return hammerMode;
	}

	@Override
	protected String getName() {
		if (hammerMode == null) {
			hammerMode = false;
		}
		return "Burrower's Pick (" + (hammerMode ? "Hammer" : "Pickaxe") + ")"; // TODO: Translate
	}
	@Override
	public List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		Integer size = 1 + getLevel().getRadius() * 2;
		String modeFormatted = ChatColor.GRAY + "Mode: " + ChatColor.YELLOW + (hammerMode ? "Hammer Mode" : "Pickaxe Mode");
		String sizeFormatted = ChatColor.GRAY + "Size: " + ChatColor.YELLOW + (size + "x" + size);
		lore.add(modeFormatted);
		lore.add(sizeFormatted);

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
		Integer radius = 1 + getLevel().getRadius() * 2;

		String pickModeTitle = hammerMode // TODO: Translate!
			? ChatColor.GREEN + "Hammer"
			: ChatColor.RED + "Pickaxe";
		String pickModeSubtitle = hammerMode // TODO: Translate!
			? radius + "x" + radius
			: "";

		owner.sendTitle(pickModeTitle, pickModeSubtitle, 10, 20, 10);
		owner.playSound(owner.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 0.25f, 1);
	}


	public static double getOreReward(Block ore){
		switch (ore.getType()) {
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
			case STONE:
				return 1;
			default:
				return 0;
		}
	}
}
