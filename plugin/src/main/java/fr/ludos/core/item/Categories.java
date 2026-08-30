package fr.ludos.core.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Categories for {@link ItemStack}s.
 */
public final class Categories {
	public static final Category HELMETS = Category.of(
		Material.LEATHER_HELMET,
		Material.CHAINMAIL_HELMET,
		Material.GOLDEN_HELMET,
		Material.IRON_HELMET,
		Material.DIAMOND_HELMET,
		Material.NETHERITE_HELMET,
		Material.TURTLE_HELMET
	);

	public static final Category CHESTPLATES = Category.of(
		Material.LEATHER_CHESTPLATE,
		Material.CHAINMAIL_CHESTPLATE,
		Material.GOLDEN_CHESTPLATE,
		Material.IRON_CHESTPLATE,
		Material.DIAMOND_CHESTPLATE,
		Material.NETHERITE_CHESTPLATE,
		Material.ELYTRA
	);

	public static final Category LEGGINGS = Category.of(
		Material.LEATHER_LEGGINGS,
		Material.CHAINMAIL_LEGGINGS,
		Material.GOLDEN_LEGGINGS,
		Material.IRON_LEGGINGS,
		Material.DIAMOND_LEGGINGS,
		Material.NETHERITE_LEGGINGS
	);

	public static final Category BOOTS = Category.of(
		Material.LEATHER_BOOTS,
		Material.CHAINMAIL_BOOTS,
		Material.GOLDEN_BOOTS,
		Material.IRON_BOOTS,
		Material.DIAMOND_BOOTS,
		Material.NETHERITE_BOOTS
	);

	public static final Category ARMOR = Category.union(HELMETS, CHESTPLATES, LEGGINGS, BOOTS);
	/**
	 * Armor type enum for categorizing armor pieces.
	 */
	public static enum ARMOR_TYPE {
		HELMETS,
		CHESTPLATES,
		LEGGINGS,
		BOOTS
	};

	public static final Category SWORDS = Category.of(
		Material.WOODEN_SWORD,
		Material.STONE_SWORD,
		Material.GOLDEN_SWORD,
		Material.IRON_SWORD,
		Material.DIAMOND_SWORD,
		Material.NETHERITE_SWORD
	);

	public static final Category AXES = Category.of(
		Material.WOODEN_AXE,
		Material.STONE_AXE,
		Material.GOLDEN_AXE,
		Material.IRON_AXE,
		Material.DIAMOND_AXE,
		Material.NETHERITE_AXE
	);

	public static final Category MELEE_WEAPONS = Category.union(SWORDS, AXES);
	/**
	 * Melee weapon type enum for categorizing melee weapons.
	 */
	public static enum MELEE_WEAPON_TYPE {
		SWORDS,
		AXES
	};

	public static final Category RANGED_WEAPONS = Category.of(
		Material.BOW,
		Material.CROSSBOW,
		Material.TRIDENT
	);
	/**
	 * Ranged weapon type enum for categorizing ranged weapons.
	 */
	public static enum RANGED_WEAPONS_TYPE {
		BOW,
		CROSSBOW,
		TRIDENT
	};

	public static final Category WEAPONS = Category.union(MELEE_WEAPONS, RANGED_WEAPONS);
	/**
	 * Weapon type enum for categorizing Weapons.
	 */
	public static enum WEAPONS_TYPE {
		MELEE,
		RANGED
	};

	public static final Category PICKAXES = Category.of(
		Material.WOODEN_PICKAXE,
		Material.STONE_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.IRON_PICKAXE,
		Material.DIAMOND_PICKAXE,
		Material.NETHERITE_PICKAXE
	);

	public static final Category SHOVELS = Category.of(
		Material.WOODEN_SHOVEL,
		Material.STONE_SHOVEL,
		Material.GOLDEN_SHOVEL,
		Material.IRON_SHOVEL,
		Material.DIAMOND_SHOVEL,
		Material.NETHERITE_SHOVEL
	);

	public static final Category HOES = Category.of(
		Material.WOODEN_HOE,
		Material.STONE_HOE,
		Material.GOLDEN_HOE,
		Material.IRON_HOE,
		Material.DIAMOND_HOE,
		Material.NETHERITE_HOE
	);

	public static final Category TOOLS = Category.union(PICKAXES, SHOVELS, HOES);
	/**
	 * Tool type enum for categorizing Tools.
	 */
	public static enum TOOLS_TYPE {
		PICKAXES,
		SHOVELS,
		HOES
	};

	public static final Category IMPORTANT_DURABILITY = Category.union(ARMOR, WEAPONS, TOOLS);
}