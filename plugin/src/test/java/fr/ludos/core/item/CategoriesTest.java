package fr.ludos.core.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
		assertTrue(Categories.HELMETS.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the CHESTPLATES group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "GOLDEN_CHESTPLATE", "IRON_CHESTPLATE",
		"DIAMOND_CHESTPLATE", "NETHERITE_CHESTPLATE", "ELYTRA"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid chestplates return true for isChestplate")
	void testIsChestplateTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.CHESTPLATES.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the LEGGINGS group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS", "GOLDEN_LEGGINGS", "IRON_LEGGINGS",
		"DIAMOND_LEGGINGS", "NETHERITE_LEGGINGS"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid leggings return true for isLeggings")
	void testIsLeggingsTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.LEGGINGS.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the BOOTS group")

	@EnumSource(value = Material.class, names = {
		"LEATHER_BOOTS", "CHAINMAIL_BOOTS", "GOLDEN_BOOTS", "IRON_BOOTS",
		"DIAMOND_BOOTS", "NETHERITE_BOOTS"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid boots return true for isBoots")
	void testIsBootsTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.BOOTS.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the SWORDS group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_SWORD", "STONE_SWORD", "GOLDEN_SWORD", "IRON_SWORD",
		"DIAMOND_SWORD", "NETHERITE_SWORD"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid swords return true for isSword")
	void testIsSwordTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.SWORDS.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the AXES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_AXE", "STONE_AXE", "GOLDEN_AXE", "IRON_AXE",
		"DIAMOND_AXE", "NETHERITE_AXE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid axes return true for isAxe")
	void testIsAxeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.AXES.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the PICKAXES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_PICKAXE", "STONE_PICKAXE", "GOLDEN_PICKAXE", "IRON_PICKAXE",
		"DIAMOND_PICKAXE", "NETHERITE_PICKAXE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid pickaxes return true for isPickaxe")
	void testIsPickaxeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.PICKAXES.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the SHOVELS group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_SHOVEL", "STONE_SHOVEL", "GOLDEN_SHOVEL", "IRON_SHOVEL",
		"DIAMOND_SHOVEL", "NETHERITE_SHOVEL"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid shovels return true for isShovel")
	void testIsShovelTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.SHOVELS.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the HOES group")

	@EnumSource(value = Material.class, names = {
		"WOODEN_HOE", "STONE_HOE", "GOLDEN_HOE", "IRON_HOE",
		"DIAMOND_HOE", "NETHERITE_HOE"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid hoes return true for isHoe")
	void testIsHoeTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.HOES.contains(item));
	}

	@ParameterizedTest(name = "Material {0} must be in the RANGED group")

	@EnumSource(value = Material.class, names = {
		"BOW", "CROSSBOW", "TRIDENT"
	}, mode = EnumSource.Mode.INCLUDE)

	@DisplayName("Verifies that valid ranged weapons return true for isRanged")
	void testIsRangedTrue(Material material) {
		ItemStack item = new ItemStack(material);
		assertTrue(Categories.RANGED_WEAPONS.contains(item));
	}


	@DisplayName("Verifies that ARMOR group contains all helmets, chestplates, leggings, and boots")
	@Test
	void testArmorGroup() {
		assertTrue(Categories.ARMOR.contains(Material.DIAMOND_HELMET));
		assertTrue(Categories.ARMOR.contains(Material.IRON_CHESTPLATE));
		assertTrue(Categories.ARMOR.contains(Material.GOLDEN_LEGGINGS));
		assertTrue(Categories.ARMOR.contains(Material.NETHERITE_BOOTS));
		assertTrue(Categories.ARMOR.contains(Material.ELYTRA));
	}

	@DisplayName("Verifies that WEAPONS group contains swords, axes, and ranged weapons")
	@Test
	void testWeaponsGroup() {
		assertTrue(Categories.WEAPONS.contains(Material.DIAMOND_SWORD));
		assertTrue(Categories.WEAPONS.contains(Material.IRON_AXE));
		assertTrue(Categories.WEAPONS.contains(Material.BOW));
		assertTrue(Categories.WEAPONS.contains(Material.TRIDENT));
	}

	@DisplayName("Verifies that TOOLS group contains pickaxes, shovels, and hoes")
	@Test
	void testToolsGroup() {
		assertTrue(Categories.TOOLS.contains(Material.DIAMOND_PICKAXE));
		assertTrue(Categories.TOOLS.contains(Material.STONE_SHOVEL));
		assertTrue(Categories.TOOLS.contains(Material.GOLDEN_HOE));
	}

	@DisplayName("Verifies that IMPORTANT_DURABILITY group contains ARMOR, WEAPONS, and TOOLS")
	@Test
	void testImportantDurabilityGroup() {
		assertTrue(Categories.IMPORTANT_DURABILITY.contains(Material.DIAMOND_HELMET));
		assertTrue(Categories.IMPORTANT_DURABILITY.contains(Material.IRON_SWORD));
		assertTrue(Categories.IMPORTANT_DURABILITY.contains(Material.NETHERITE_PICKAXE));
		assertFalse(Categories.IMPORTANT_DURABILITY.contains(Material.APPLE));
	}



	@DisplayName("Verifies that isHelmet returns false for null item")
	@Test
	void testIsHelmetNull() {
		assertFalse(Categories.HELMETS.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isChestplate returns false for null item")
	@Test
	void testIsChestplateNull() {
		assertFalse(Categories.CHESTPLATES.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isLeggings returns false for null item")
	@Test
	void testIsLeggingsNull() {
		assertFalse(Categories.LEGGINGS.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isBoots returns false for null item")
	@Test
	void testIsBootsNull() {
		assertFalse(Categories.BOOTS.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isSword returns false for null item")
	@Test
	void testIsSwordNull() {
		assertFalse(Categories.SWORDS.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isAxe returns false for null item")
	@Test
	void testIsAxeNull() {
		assertFalse(Categories.AXES.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isPickaxe returns false for null item")
	@Test
	void testIsPickaxeNull() {
		assertFalse(Categories.PICKAXES.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isShovel returns false for null item")
	@Test
	void testIsShovelNull() {
		assertFalse(Categories.SHOVELS.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isHoe returns false for null item")
	@Test
	void testIsHoeNull() {
		assertFalse(Categories.HOES.contains((ItemStack) null));
	}

	@DisplayName("Verifies that isRanged returns false for null item")
	@Test
	void testIsRangedNull() {
		assertFalse(Categories.RANGED_WEAPONS.contains((ItemStack) null));
	}



	@DisplayName("Verifies that uncategorized materials return false for specific categories")
	@Test
	void testNonCategorizedMaterials() {
		assertFalse(Categories.HELMETS.contains(Material.DIAMOND_SWORD));
		assertFalse(Categories.CHESTPLATES.contains(Material.BOW));
		assertFalse(Categories.SWORDS.contains(Material.DIAMOND_HELMET));
		assertFalse(Categories.AXES.contains(Material.IRON_PICKAXE));
		assertFalse(Categories.HOES.contains(Material.STONE_AXE));
		assertFalse(Categories.RANGED_WEAPONS.contains(Material.DIAMOND_AXE));
	}


	@DisplayName("Verifies that ARMOR union does not contain tools")
	@Test
	void testArmorDoesNotContainTools() {
		assertFalse(Categories.ARMOR.contains(Material.DIAMOND_PICKAXE));
		assertFalse(Categories.ARMOR.contains(Material.STONE_SHOVEL));
		assertFalse(Categories.ARMOR.contains(Material.IRON_HOE));
	}

	@DisplayName("Verifies that WEAPONS union does not contain armor")
	@Test
	void testWeaponsDoesNotContainArmor() {
		assertFalse(Categories.WEAPONS.contains(Material.DIAMOND_HELMET));
		assertFalse(Categories.WEAPONS.contains(Material.IRON_CHESTPLATE));
	}

	@DisplayName("Verifies that TOOLS union does not contain weapons")
	@Test
	void testToolsDoesNotContainWeapons() {
		assertFalse(Categories.TOOLS.contains(Material.DIAMOND_SWORD));
		assertFalse(Categories.TOOLS.contains(Material.BOW));
	}
}