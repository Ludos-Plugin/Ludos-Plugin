package fr.ludos.item.berserker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import fr.ludos.item.SpecialItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.BerserkerRole;


public class BerserkerAxe extends SpecialItem {

	public enum Variant {
		FIRST(0),
		SECOND(1);

		private final int key;
		Variant(int key) {
			this.key = key;
		}

		public int key() {
			return key;
		}

		public static Variant fromKey(int key) {
			return key == SECOND.key ? SECOND : FIRST;
		}
	}

	public static final String VARIANT_KEY = "variant";
	public static final String LEVEL_KEY = "level";

	// +3 ADD_NUMBER → 1 (base) + 3 = 4 damage, weaker than a sword by design
	private static final UUID DAMAGE_MODIFIER_ID = UUID.fromString("3f8a1b2c-4d5e-6f70-8901-a2b3c4d5e6f7");
	private static final double BASE_DAMAGE_BONUS = 3.0;

	private final NamespacedKey variantKey;
	private final NamespacedKey levelKey;
	private final Variant variant;
	private final int level;


	public Variant getVariant() {
		return variant;
	}

	public int getLevel() {
		return level;
	}


	// Used to read/detect an existing ItemStack
	protected BerserkerAxe(ItemStack stack, Player owner, Game game) throws IllegalArgumentException {
		super(stack, owner, game);

		variantKey = new NamespacedKey(game.getPlugin(), VARIANT_KEY);
		levelKey = new NamespacedKey(game.getPlugin(), LEVEL_KEY);

		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (!container.has(variantKey, PersistentDataType.INTEGER)) {
			throw new IllegalArgumentException("Variant not found");
		}

		variant = Variant.fromKey(getPersistentData(stack, variantKey, PersistentDataType.INTEGER));

		Integer storedLevel = container.get(levelKey, PersistentDataType.INTEGER);
		level = (storedLevel != null) ? storedLevel : 0;
	}

	// Used to create a new axe
	protected BerserkerAxe(Player owner, Variant variant, int level, Game game) {
		super(new ItemStack(getMaterial(variant, level)), owner, game);

		this.variant = variant;
		this.level = level;
		this.variantKey = new NamespacedKey(game.getPlugin(), VARIANT_KEY);
		this.levelKey = new NamespacedKey(game.getPlugin(), LEVEL_KEY);
	}

	public static Material getMaterial(Variant variant, int level) {
		if (variant == Variant.SECOND) return Material.GOLDEN_AXE;
		if (level >= 3) return Material.DIAMOND_AXE;
		if (level >= 2) return Material.GOLDEN_AXE;
		return Material.IRON_AXE;
	}

	@Override
	protected void onInitialize() {
		ItemMeta meta = getStack().getItemMeta();
		meta.getPersistentDataContainer().set(variantKey, PersistentDataType.INTEGER, variant.key());
		meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);

		// Set damage to sword baseline: 1 (base) + 5 = 6 damage, same as iron sword
		// Attack speed is NOT set here — timing is managed via player.setCooldown() in BerserkerRole
		if (variant == Variant.FIRST) {
			meta.addAttributeModifier(
				Attribute.GENERIC_ATTACK_DAMAGE,
				new AttributeModifier(DAMAGE_MODIFIER_ID, "berserker_axe_damage", BASE_DAMAGE_BONUS, Operation.ADD_NUMBER, EquipmentSlot.HAND)
			);
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		if (level >= 1) {
			Enchantment sharpness = Enchantment.getByKey(NamespacedKey.minecraft("sharpness"));
			if (sharpness != null) {
				meta.addEnchant(sharpness, getSharpnessLevel(), true);
			}
		}

		getStack().setItemMeta(meta);
	}

	public int getSharpnessLevel() {
		if (level >= 3) return 2;
		return 1; // level 1 or 2
	}


	@Override
	public String getId() {
		return "berserkerAxe";
	}

	@Override
	public Component getName() {
		Component name = Component.text("Berserker Axe")
			.decoration(TextDecoration.ITALIC, false);

		if (level > 0) {
			name = name.append(
				Component.text(" [Lv." + level + "]")
					.color(NamedTextColor.YELLOW)
					.decoration(TextDecoration.ITALIC, false)
			);
		}

		return name;
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = new ArrayList<>();

		if (variant == Variant.FIRST) {
			// Base damage: 1 (player base) + 3 (modifier) + 0.5*sharpnessLevel = 4 + enchant
			double dmg = 4.0 + (level >= 1 ? 0.5 * getSharpnessLevel() : 0);
			lore.add(
				Component.text(String.format("%.1f ❤  — vampirisme pendant la rage.", dmg))
					.color(NamedTextColor.RED)
					.decoration(TextDecoration.ITALIC, false)
			);
			if (level > 0) {
				String matName = level >= 3 ? "Diamant" : (level >= 2 ? "Or" : "Fer");
				lore.add(
					Component.text(matName + " · Tranchant " + getSharpnessLevel())
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
				);
			}
		} else {
			lore.add(
				Component.text("Lame secondaire.")
					.decoration(TextDecoration.ITALIC, false)
			);
			if (level > 0) {
				lore.add(
					Component.text("Tranchant " + getSharpnessLevel())
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
				);
			}
		}

		return lore;
	}


	@Nullable
	public static BerserkerAxe getItem(ItemStack stack, Game game) {
		Player owner = SpecialItem.getSpecialItemOwner(stack, "berserkerAxe", game);
		if (owner == null) return null;
		try {
			return new BerserkerAxe(stack, owner, game);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static BerserkerAxe createItem(Player owner, Variant variant, Game game) {
		return createItem(owner, variant, 0, game);
	}

	public static BerserkerAxe createItem(Player owner, Variant variant, int level, Game game) {
		BerserkerAxe axe = new BerserkerAxe(owner, variant, level, game);
		axe.initializeItem();
		return axe;
	}

	public static boolean isHoldingBerserkerAxe(Player player, Game game) {
		return getItem(player.getInventory().getItemInMainHand(), game) != null
			|| getItem(player.getInventory().getItemInOffHand(), game) != null;
	}


	public static class Events extends SpecialItem.Events<BerserkerAxe> {

		private final BerserkerRole role;

		public Events(Game game, BerserkerRole role) {
			super(game);
			this.role = role;
		}

		@Override
		public void updateItemInInventory(Player player) {
			if (!canPlayerHaveItem(player)) return;

			int playerLevel = role.getPlayerLevel(player);

			List<BerserkerAxe> axes = SpecialItem.findAllIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			BerserkerAxe offHandAxe = getItem(player.getInventory().getItemInOffHand(), game);

			boolean hasFirst = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.FIRST);
			boolean hasSecond = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.SECOND)
				|| (offHandAxe != null && offHandAxe.getVariant() == Variant.SECOND);

			if (!hasFirst) {
				player.getInventory().addItem(BerserkerAxe.createItem(player, Variant.FIRST, playerLevel, game).getStack());
			}

			if (!hasSecond) {
				ItemStack currentOffHand = player.getInventory().getItemInOffHand();
				if (currentOffHand == null || currentOffHand.getType().isAir()) {
					player.getInventory().setItemInOffHand(BerserkerAxe.createItem(player, Variant.SECOND, playerLevel, game).getStack());
				} else {
					player.getInventory().addItem(BerserkerAxe.createItem(player, Variant.SECOND, playerLevel, game).getStack());
				}
			}
		}

		@Override
		@Nullable
		protected BerserkerAxe getItem(ItemStack stack, Game game) {
			return BerserkerAxe.getItem(stack, game);
		}

		@Override
		protected BerserkerAxe createItem(Player owner, Game game) {
			return BerserkerAxe.createItem(owner, Variant.FIRST, role.getPlayerLevel(owner), game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BerserkerRole.id);
		}
	}
}
