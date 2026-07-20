package fr.ludos.roles.berserker.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.roles.berserker.BerserkerRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Berserker Axe, for use by any Player with {@link BerserkerRole}.
 */
public class BerserkerAxe extends LevelItem<BerserkerAxeLevels> {
	public static final String ID = "berserker_axe";

	@Override
	public String getTypeId() {
		return ID;
	}

	/**
	 * The variant of a {@link BerserkerAxe}.
	 */
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

	public static final String VARIANT_KEY_STRING = "variant";

	private final static NamespacedKey VARIANT_KEY = new NamespacedKey(Ludos.NAMESPACE, VARIANT_KEY_STRING);

	private final Variant variant;
	public Variant getVariant() {
		return variant;
	}


	public static @Nullable Variant getSpecialItemVariant(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(VARIANT_KEY, PersistentDataType.INTEGER) ) return null;

		return Variant.fromKey(container.get(VARIANT_KEY, PersistentDataType.INTEGER));
	}


	@Nullable
	public static BerserkerAxe getItem(Events events, ItemStack stack, Game game) {
		UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BerserkerAxe cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;

		LevelValue levelValue = LevelItemInterface.levelFromItemStack(stack, game);
		if (levelValue == null) return null;
		Variant variant = getSpecialItemVariant(stack, game);
		if (variant == null) return null;

		BerserkerAxe axe = new BerserkerAxe(events, variant, levelValue, stack, owner, game);
		// cachedItems.put(itemId, axe);

		return axe;
	}

	public static BerserkerAxe createItem(Events events, Variant variant, LevelValue level, Player owner, Game game) {
		BerserkerAxeLevels lvl = BerserkerAxeLevels.values()[level.level()];
		Material mat = lvl.getMaterialForVariant(variant);
		BerserkerAxe axe = new BerserkerAxe(events, variant, level, new ItemStack(mat), owner, game);
		UUID itemId = axe.initializeItem();

		// cachedItems.put(itemId, axe);

		return axe;
	}


	protected BerserkerAxe(Events events, Variant variant, LevelValue level, ItemStack stack, Player owner, Game game) throws IllegalArgumentException {
		super(events.getLevels(), level, stack, owner, game);

		this.variant = variant;
	}


	@Override
	protected void onInitialize() {
		super.onInitialize();
		ItemMeta meta = getStack().getItemMeta();

		meta.getPersistentDataContainer().set(VARIANT_KEY, PersistentDataType.INTEGER, variant.key());

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

		BerserkerAxeLevels level = lvlObject();

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

	/**
	 * Events for the {@link BerserkerAxe}.
	 */
	public static class Events extends LevelItem.Events<BerserkerAxe, BerserkerAxeLevels> {
		private static final List<BerserkerAxeLevels> LEVELS = List.of(BerserkerAxeLevels.values());
		private final BerserkerRole role;

		public Events(Game game, BerserkerRole role) {
			super(game, new Events.Info());
			this.role = role;
		}

		@Override
		public List<BerserkerAxeLevels> getLevels() {
			return LEVELS;
		}

		@Override
		public void refreshPlayerInventory(Player player) {
			if (!isPlayerValid(player)) return;

			LevelValue playerLevel = deadPlayerLevels.get(player);
			if (playerLevel == null) {
				playerLevel = new LevelValue();
			}

			List<BerserkerAxe> axes = SpecialItem.findAllIn(player.getInventory(), this::getItem);
			BerserkerAxe offHandAxe = getItem(player.getInventory().getItemInOffHand());

			boolean hasFirst = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.FIRST);
			boolean hasSecond = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.SECOND)
				|| (offHandAxe != null && offHandAxe.getVariant() == Variant.SECOND);

			if (!hasFirst) {
				player.getInventory().addItem(createItem(Variant.FIRST, playerLevel, player).getStack());
			}

			if (!hasSecond) {
				ItemStack currentOffHand = player.getInventory().getItemInOffHand();
				BerserkerAxe secondAxe = createItem(Variant.SECOND, playerLevel, player);
				if (currentOffHand == null || currentOffHand.getType().isAir()) {
					player.getInventory().setItemInOffHand(secondAxe.getStack());
				} else {
					player.getInventory().addItem(secondAxe.getStack());
				}
			}
		}

		public boolean isHoldingBerserkerAxe(Player player) {
			return getItem(player.getInventory().getItemInMainHand()) != null
				|| getItem(player.getInventory().getItemInOffHand()) != null;
		}

		@EventHandler(ignoreCancelled = true)
		public void onPlayerTakeDamage(EntityDamageEvent event) {
			if (!(event.getEntity() instanceof Player player)) return;

			double finalDamage = event.getFinalDamage();
			if (finalDamage <= 0) return;

			SpecialItem.findAllIn(player.getInventory(), this::getItem)
				.forEach(axe -> {
					axe.addXp(finalDamage);
				});
		}

		@Override
		@Nullable
		public BerserkerAxe getItem(ItemStack stack) {
			return BerserkerAxe.getItem(this, stack, game);
		}

		@Override
		public BerserkerAxe createItem(LevelValue level, Player owner) {
			return createItem(Variant.FIRST, level, owner);
		}
		public BerserkerAxe createItem(Variant variant, LevelValue level, Player owner) {
			return BerserkerAxe.createItem(this, variant, level, owner, game);
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, BerserkerRole.ID);
		}
	}
}
