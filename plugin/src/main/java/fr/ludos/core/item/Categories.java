package fr.ludos.core.item;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Categories {
	public enum Group {
		HELMETS,
		CHESTPLATES,
		LEGGINGS,
		BOOTS,
		SWORDS,
		AXES,
		PICKAXES,
		SHOVELS,
		HOES,
		RANGED,
		ARMOR,
		WEAPONS,
		TOOLS,
		IMPORTANT_DURABILITY
	}

	private static final Map<Group, Set<Material>> GROUPS = new EnumMap<>(Group.class);

	public static final Set<Material> HELMETS = register(Group.HELMETS,
		Material.LEATHER_HELMET,
		Material.CHAINMAIL_HELMET,
		Material.GOLDEN_HELMET,
		Material.IRON_HELMET,
		Material.DIAMOND_HELMET,
		Material.NETHERITE_HELMET,
		Material.TURTLE_HELMET
	);

	public static final Set<Material> CHESTPLATES = register(Group.CHESTPLATES,
		Material.LEATHER_CHESTPLATE,
		Material.CHAINMAIL_CHESTPLATE,
		Material.GOLDEN_CHESTPLATE,
		Material.IRON_CHESTPLATE,
		Material.DIAMOND_CHESTPLATE,
		Material.NETHERITE_CHESTPLATE,
		Material.ELYTRA
	);

	public static final Set<Material> LEGGINGS = register(Group.LEGGINGS,
		Material.LEATHER_LEGGINGS,
		Material.CHAINMAIL_LEGGINGS,
		Material.GOLDEN_LEGGINGS,
		Material.IRON_LEGGINGS,
		Material.DIAMOND_LEGGINGS,
		Material.NETHERITE_LEGGINGS
	);

	public static final Set<Material> BOOTS = register(Group.BOOTS,
		Material.LEATHER_BOOTS,
		Material.CHAINMAIL_BOOTS,
		Material.GOLDEN_BOOTS,
		Material.IRON_BOOTS,
		Material.DIAMOND_BOOTS,
		Material.NETHERITE_BOOTS
	);

	public static final Set<Material> SWORDS = register(Group.SWORDS,
		Material.WOODEN_SWORD,
		Material.STONE_SWORD,
		Material.GOLDEN_SWORD,
		Material.IRON_SWORD,
		Material.DIAMOND_SWORD,
		Material.NETHERITE_SWORD
	);

	public static final Set<Material> AXES = register(Group.AXES,
		Material.WOODEN_AXE,
		Material.STONE_AXE,
		Material.GOLDEN_AXE,
		Material.IRON_AXE,
		Material.DIAMOND_AXE,
		Material.NETHERITE_AXE
	);

	public static final Set<Material> PICKAXES = register(Group.PICKAXES,
		Material.WOODEN_PICKAXE,
		Material.STONE_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.IRON_PICKAXE,
		Material.DIAMOND_PICKAXE,
		Material.NETHERITE_PICKAXE
	);

	public static final Set<Material> SHOVELS = register(Group.SHOVELS,
		Material.WOODEN_SHOVEL,
		Material.STONE_SHOVEL,
		Material.GOLDEN_SHOVEL,
		Material.IRON_SHOVEL,
		Material.DIAMOND_SHOVEL,
		Material.NETHERITE_SHOVEL
	);

	public static final Set<Material> HOES = register(Group.HOES,
		Material.WOODEN_HOE,
		Material.STONE_HOE,
		Material.GOLDEN_HOE,
		Material.IRON_HOE,
		Material.DIAMOND_HOE,
		Material.NETHERITE_HOE
	);

	public static final Set<Material> RANGED = register(Group.RANGED,
		Material.BOW,
		Material.CROSSBOW,
		Material.TRIDENT
	);

	public static final Set<Material> ARMOR = register(Group.ARMOR, union(HELMETS, CHESTPLATES, LEGGINGS, BOOTS));
	public static final Set<Material> WEAPONS = register(Group.WEAPONS, union(SWORDS, AXES, RANGED));
	public static final Set<Material> TOOLS = register(Group.TOOLS, union(PICKAXES, SHOVELS, HOES));
	public static final Set<Material> IMPORTANT_DURABILITY = register(Group.IMPORTANT_DURABILITY, union(ARMOR, WEAPONS, TOOLS));

	public static Set<Material> get(Group group) {
		return GROUPS.getOrDefault(group, Collections.emptySet());
	}

	public static boolean is(Group group, Material material) {
		if (material == null) return false;
		return get(group).contains(material);
	}

	private static Set<Material> register(Group group, Material... materials) {
		return register(group, Arrays.asList(materials));
	}

	private static Set<Material> register(Group group, Iterable<Material> materials) {
		EnumSet<Material> values = EnumSet.noneOf(Material.class);
		for (Material material : materials) {
			if (material != null) {
				values.add(material);
			}
		}

		Set<Material> view = Collections.unmodifiableSet(values);
		GROUPS.put(group, view);
		return view;
	}

	@SafeVarargs
	private static Set<Material> union(Set<Material>... groups) {
		EnumSet<Material> values = EnumSet.noneOf(Material.class);
		for (Set<Material> group : groups) {
			values.addAll(group);
		}
		return Collections.unmodifiableSet(values);
	}

    public static boolean isChestplate(ItemStack item) {
		if (item == null) return false;
		return Categories.CHESTPLATES.contains(item.getType());
	}

    public static boolean isHelmet(ItemStack item) {
        if (item == null) return false;
        return Categories.HELMETS.contains(item.getType());
    }

    public static boolean isLeggings(ItemStack item) {
        if (item == null) return false;
        return Categories.LEGGINGS.contains(item.getType());
    }

    public static boolean isBoots(ItemStack item) {
        if (item == null) return false;
        return Categories.BOOTS.contains(item.getType());
    }

    public static boolean isSword(ItemStack item) {
        if (item == null) return false;
        return Categories.SWORDS.contains(item.getType());
    }

    public static boolean isAxe(ItemStack item) {
        if (item == null) return false;
        return Categories.AXES.contains(item.getType());
    }

    public static boolean isPickaxe(ItemStack item) {
        if (item == null) return false;
        return Categories.PICKAXES.contains(item.getType());
    }

    public static boolean isShovel(ItemStack item) {
        if (item == null) return false;
        return Categories.SHOVELS.contains(item.getType());
    }

    public static boolean isHoe(ItemStack item) {
        if (item == null) return false;
        return Categories.HOES.contains(item.getType());
    }

    public static boolean isRanged(ItemStack item) {
        if (item == null) return false;
        return Categories.RANGED.contains(item.getType());
    }
}