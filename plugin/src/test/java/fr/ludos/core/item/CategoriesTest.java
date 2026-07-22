package fr.ludos.core.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CategoriesTest {
	@ParameterizedTest(name = "Material {0} must be in the HELMETS group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_HELMET", "CHAINMAIL_HELMET", "GOLDEN_HELMET", "IRON_HELMET",
		"DIAMOND_HELMET", "NETHERITE_HELMET", "TURTLE_HELMET"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid helmets return true for isHelmet")
	void testIsHelmetTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isHelmet(item));
		assertTrue(Categories.is(Categories.Group.HELMETS, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the CHESTPLATES group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "GOLDEN_CHESTPLATE", "IRON_CHESTPLATE",
		"DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE", "ELYTRA"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid chestplates return true for isChestplate")
	void testIsChestplateTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isChestplate(item));
		assertTrue(Categories.is(Categories.Group.CHESTPLATES, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the LEGGINGS group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS", "GOLDEN_LEGGINGS", "IRON_LEGGINGS",
		"DIAMOND_LEGGINGS", "NETHERITE_LEGGINGS"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid leggings return true for isLeggings")
	void testIsLeggingsTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isLeggings(item));
		assertTrue(Categories.is(Categories.Group.LEGGINGS, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the BOOTS group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_BOOTS", "CHAINMAIL_BOOTS", "GOLDEN_BOOTS", "IRON_BOOTS",
		"DIAMOND_BOOTS", "NETHERITE_BOOTS"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid boots return true for isBoots")
	void testIsBootsTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isBoots(item));
		assertTrue(Categories.is(Categories.Group.BOOTS, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the SWORDS group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_SWORD", "STONE_SWORD", "GOLDEN_SWORD", "IRON_SWORD",
		"DIAMOND_SWORD", "NETHERITE_SWORD"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid swords return true for isSword")
	void testIsSwordTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isSword(item));
		assertTrue(Categories.is(Categories.Group.SWORDS, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the AXES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_AXE", "STONE_AXE", "GOLDEN_AXE", "IRON_AXE",
		"DIAMOND_AXE", "NETHERITE_AXE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid axes return true for isAxe")
	void testIsAxeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isAxe(item));
		assertTrue(Categories.is(Categories.Group.AXES, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the PICKAXES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "IRON_PICKAXE",
		"DIAMOND_PICKAXE", "NETHERITE_PICKAXE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid pickaxes return true for isPickaxe")
	void testIsPickaxeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isPickaxe(item));
		assertTrue(Categories.is(Categories.Group.PICKAXES, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the SHOVELS group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_SHOVEL", "STONE_SHOVEL", "GOLDEN_SHOVEL", "IRON_SHOVEL",
		"DIAMOND_SHOVEL", "NETHERITE_SHOVEL"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid shovels return true for isShovel")
	void testIsShovelTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isShovel(item));
		assertTrue(Categories.is(Categories.Group.SHOVELS, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the HOES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_HOE", "STONE_HOE", "GOLDEN_HOE", "IRON_HOE",
		"DIAMOND_HOE", "NETHERITE_HOE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid hoes return true for isHoe")
	void testIsHoeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isHoe(item));
		assertTrue(Categories.is(Categories.Group.HOES, material));
	}

	@ParameterizedTest(name = "Material {0} must be in the RANGED group")

	@EnumSource(value = Material.class, names = {
		"BOW", "CROSSBOW", "TRIDENT"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid ranged weapons return true for isRanged")
	void testIsRangedTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.isRanged(item));
		assertTrue(Categories.is(Categories.Group.RANGED, material));
	}


	@DisplayName("Verifies that ARMOR group contains all helmets, chestplates, leggings, and boots")
	@Test
	void testArmorGroup() {
		assertTrue(Categories.is(Categories.Group.ARMOR, Material.DIAMOND_HELMET));
		assertTrue(Categories.is(Categories.Group.ARMOR, Material.IRON_CHESTPLATE));
		assertTrue(Categories.is(Categories.Group.ARMOR, Material.GOLDEN_LEGGINGS));
		assertTrue(Categories.is(Categories.Group.ARMOR, Material.NETHERITE_BOOTS));
		assertTrue(Categories.is(Categories.Group.ARMOR, Material.ELYTRA));
	}

	@DisplayName("Verifies that WEAPONS group contains swords, axes, and ranged weapons")
	@Test
	void testWeaponsGroup() {
		assertTrue(Categories.is(Categories.Group.WEAPONS, Material.DIAMOND_SWORD));
		assertTrue(Categories.is(Categories.Group.WEAPONS, Material.IRON_AXE));
		assertTrue(Categories.is(Categories.Group.WEAPONS, Material.BOW));
		assertTrue(Categories.is(Categories.Group.WEAPONS, Material.TRIDENT));
	}

	@DisplayName("Verifies that TOOLS group contains pickaxes, shovels, and hoes")
	@Test
	void testToolsGroup() {
		assertTrue(Categories.is(Categories.Group.TOOLS, Material.DIAMOND_PICKAXE));
		assertTrue(Categories.is(Categories.Group.TOOLS, Material.STONE_SHOVEL));
		assertTrue(Categories.is(Categories.Group.TOOLS, Material.GOLDEN_HOE));
	}

	@DisplayName("Verifies that IMPORTANT_DURABILITY group contains ARMOR, WEAPONS, and TOOLS")
	@Test
	void testImportantDurabilityGroup() {
		assertTrue(Categories.is(Categories.Group.IMPORTANT_DURABILITY, Material.DIAMOND_HELMET));
		assertTrue(Categories.is(Categories.Group.IMPORTANT_DURABILITY, Material.IRON_SWORD));
		assertTrue(Categories.is(Categories.Group.IMPORTANT_DURABILITY, Material.NETHERITE_PICKAXE));
		assertFalse(Categories.is(Categories.Group.IMPORTANT_DURABILITY, Material.APPLE));
	}



	@DisplayName("Verifies that isHelmet returns false for null item")
	@Test
	void testIsHelmetNull() {
		assertFalse(Categories.isHelmet((ItemStack) null));
		assertFalse(Categories.is(Categories.Group.HELMETS, (Material) null));
	}

	@DisplayName("Verifies that isChestplate returns false for null item")
	@Test
	void testIsChestplateNull() {
		assertFalse(Categories.isChestplate((ItemStack) null));
	}

	@DisplayName("Verifies that isLeggings returns false for null item")
	@Test
	void testIsLeggingsNull() {
		assertFalse(Categories.isLeggings((ItemStack) null));
	}

	@DisplayName("Verifies that isBoots returns false for null item")
	@Test
	void testIsBootsNull() {
		assertFalse(Categories.isBoots((ItemStack) null));
	}

	@DisplayName("Verifies that isSword returns false for null item")
	@Test
	void testIsSwordNull() {
		assertFalse(Categories.isSword((ItemStack) null));
	}

	@DisplayName("Verifies that isAxe returns false for null item")
	@Test
	void testIsAxeNull() {
		assertFalse(Categories.isAxe((ItemStack) null));
	}

	@DisplayName("Verifies that isPickaxe returns false for null item")
	@Test
	void testIsPickaxeNull() {
		assertFalse(Categories.isPickaxe((ItemStack) null));
	}

	@DisplayName("Verifies that isShovel returns false for null item")
	@Test
	void testIsShovelNull() {
		assertFalse(Categories.isShovel((ItemStack) null));
	}

	@DisplayName("Verifies that isHoe returns false for null item")
	@Test
	void testIsHoeNull() {
		assertFalse(Categories.isHoe((ItemStack) null));
	}

	@DisplayName("Verifies that isRanged returns false for null item")
	@Test
	void testIsRangedNull() {
		assertFalse(Categories.isRanged((ItemStack) null));
	}



	@DisplayName("Verifies that uncategorized materials return false for specific categories")
	@Test
	void testNonCategorizedMaterials() {
		assertFalse(Categories.isHelmet(Material.DIAMOND_SWORD));
		assertFalse(Categories.isChestplate(Material.BOW));
		assertFalse(Categories.isSword(Material.DIAMOND_HELMET));
		assertFalse(Categories.isAxe(Material.IRON_PICKAXE));
		assertFalse(Categories.isHoe(Material.STONE_AXE));
		assertFalse(Categories.isRanged(Material.DIAMOND_AXE));
		assertFalse(Categories.is(Categories.Group.HELMETS, Material.APPLE));
	}


	@DisplayName("Verifies that get() returns an unmodifiable set")
	@Test
	void testGetReturnsUnmodifiableSet() {
		assertThrows(UnsupportedOperationException.class, () -> {
			Categories.get(Categories.Group.SWORDS).add(Material.GOLDEN_AXE);
		});
	}

	@DisplayName("Verifies that get() returns a non-empty set for known groups")
	@Test
	void testGetKnownGroup() {
		assertNotNull(Categories.get(Categories.Group.HELMETS));
		assertFalse(Categories.get(Categories.Group.HELMETS).isEmpty());
	}


	@DisplayName("Verifies that ARMOR union does not contain tools")
	@Test
	void testArmorDoesNotContainTools() {
		assertFalse(Categories.is(Categories.Group.ARMOR, Material.DIAMOND_PICKAXE));
		assertFalse(Categories.is(Categories.Group.ARMOR, Material.STONE_SHOVEL));
		assertFalse(Categories.is(Categories.Group.ARMOR, Material.IRON_HOE));
	}

	@DisplayName("Verifies that WEAPONS union does not contain armor")
	@Test
	void testWeaponsDoesNotContainArmor() {
		assertFalse(Categories.is(Categories.Group.WEAPONS, Material.DIAMOND_HELMET));
		assertFalse(Categories.is(Categories.Group.WEAPONS, Material.IRON_CHESTPLATE));
	}

	@DisplayName("Verifies that TOOLS union does not contain weapons")
	@Test
	void testToolsDoesNotContainWeapons() {
		assertFalse(Categories.is(Categories.Group.TOOLS, Material.DIAMOND_SWORD));
		assertFalse(Categories.is(Categories.Group.TOOLS, Material.BOW));
	}
}