package fr.ludos.item.berserker;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.BerserkerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class BerserkerAxe extends LevelItem<BerserkerAxeLevels> {
	public static final String ID = "berserkerAxe";

	@Override
	public String getTypeId() {
		return ID;
	}

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

	private final static NamespacedKey variantKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), VARIANT_KEY);

	private final Variant variant;
	public Variant getVariant() {
		return variant;
	}


	public static @Nullable Variant getSpecialItemVariant(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(variantKey, PersistentDataType.INTEGER) ) return null;

		return Variant.fromKey(getPersistentData(stack, variantKey, PersistentDataType.INTEGER));
	}


	@Nullable
	public static BerserkerAxe getItem(ItemStack stack, Game game) {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BerserkerAxe cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;
		Variant variant = getSpecialItemVariant(stack, game);
		if (variant == null) return null;

		BerserkerAxe axe = new BerserkerAxe(stack, owner, variant, levelState, game);
		// cachedItems.put(itemId, axe);

		return axe;
	}

	public static BerserkerAxe createItem(Player owner, Variant variant, LevelState level, Game game) {
		BerserkerAxeLevels lvl = BerserkerAxeLevels.values()[level.getLevel()];
		Material mat = lvl.getMaterialForVariant(variant);
		BerserkerAxe axe = new BerserkerAxe(new ItemStack(mat), owner, variant, level, game);
		UUID itemId = axe.initializeItem();

		// cachedItems.put(itemId, axe);

		return axe;
	}


	protected BerserkerAxe(ItemStack stack, Player owner, Variant variant, LevelItem.LevelState level, Game game) throws IllegalArgumentException {
		super(BerserkerAxeLevels.class, stack, owner, level, game);

		this.variant = variant;
	}


	@Override
	protected void onInitialize() {
		super.onInitialize();
		ItemMeta meta = getStack().getItemMeta();

		meta.getPersistentDataContainer().set(variantKey, PersistentDataType.INTEGER, variant.key());

		// Set damage to sword baseline: 1 (base) + 5 = 6 damage, same as iron sword
		if (variant == Variant.FIRST) {
			// meta.addAttributeModifier(
			// 	Attribute.GENERIC_ATTACK_DAMAGE,
			// 	new AttributeModifier(DAMAGE_MODIFIER_ID, "berserker_axe_damage", BASE_DAMAGE_BONUS, Operation.ADD_NUMBER, EquipmentSlot.HAND)
			// );
		} else {
			meta.addAttributeModifier(
				Attribute.GENERIC_ATTACK_DAMAGE,
				new AttributeModifier(UUID.randomUUID(), "berserker_variant_axe_extra_damage", -3.0, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
			);
			meta.addAttributeModifier(
				Attribute.GENERIC_ATTACK_SPEED,
				new AttributeModifier(UUID.randomUUID(), "berserker_variant_axe_less_speed", 0.5, Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
			);
		}

		getStack().setItemMeta(meta);
	}

	@Override
	public Component getName() {
		return Component.text("Berserker Axe")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		BerserkerAxeLevels level = getLvlObject();

		if (variant == Variant.FIRST) {
			// Base damage: 1 (player base) + 3 (modifier) + 0.5*sharpnessLevel = 4 + enchant
			double dmg = 4.0 + level.getDamageBonus();
			lore.add(
				Component.text(String.format("%.1f ❤  — vampirisme pendant la rage.", dmg))
					.color(NamedTextColor.RED)
					.decoration(TextDecoration.ITALIC, false)
			);
		} else {
			lore.add(
				Component.text("Lame secondaire.")
					.decoration(TextDecoration.ITALIC, false)
			);
		}

		return lore;
	}

	public static boolean isHoldingBerserkerAxe(Player player, Game game) {
		return getItem(player.getInventory().getItemInMainHand(), game) != null
			|| getItem(player.getInventory().getItemInOffHand(), game) != null;
	}


	public static class Events extends LevelItem.Events<BerserkerAxe, BerserkerAxeLevels> {

		private final BerserkerRole role;

		public Events(Game game, BerserkerRole role) {
			super(game);
			this.role = role;
		}

		@Override
		public void updateItemInInventory(Player player) {
			if (!canPlayerHaveItem(player)) return;

			LevelState playerLevel = deadPlayerLevels.get(player);
			if (playerLevel == null) {
				playerLevel = new LevelState();
			}

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
				BerserkerAxe secondAxe = BerserkerAxe.createItem(player, Variant.SECOND, playerLevel, game);
				if (currentOffHand == null || currentOffHand.getType().isAir()) {
					player.getInventory().setItemInOffHand(secondAxe.getStack());
				} else {
					player.getInventory().addItem(secondAxe.getStack());
				}
			}
		}

		@EventHandler(ignoreCancelled = true)
		public void onPlayerTakeDamage(EntityDamageEvent event) {
			if (!(event.getEntity() instanceof Player player)) return;

			double finalDamage = event.getFinalDamage();
			if (finalDamage <= 0) return;

			SpecialItem.findAllIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game))
				.forEach(axe -> {
					axe.addXp(finalDamage);
				});
		}

		@Override
		@Nullable
		protected BerserkerAxe getItem(ItemStack stack, Game game) {
			return BerserkerAxe.getItem(stack, game);
		}

		@Override
		protected BerserkerAxe createItem(Player owner, LevelState level, Game game) {
			return BerserkerAxe.createItem(owner, Variant.FIRST, level, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BerserkerRole.ID);
		}
	}
}
