package fr.ludos.roles.berserker.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

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
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.roles.berserker.BerserkerRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Berserker Axe, for use by any Player with {@link BerserkerRole}.
 */
public class BerserkerAxe extends LevelItem<BerserkerAxe, BerserkerAxeLevels> {
	public static final String ID = "berserker_axe";

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


	protected BerserkerAxe(Variant variant, LevelItem.ItemData<BerserkerAxeLevels> info, Events events) throws IllegalArgumentException {
		super(info, events);

		this.variant = variant;
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

		public Events(Game game) {
			super(game, new Events.Info());
		}

		@Override
		public String getTypeId() {
			return ID;
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

			List<BerserkerAxe> axes = SpecialItem.findAll(player.getInventory(), this::getItem);
			BerserkerAxe offHandAxe = getItem(player.getInventory().getItemInOffHand());

			boolean hasFirst = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.FIRST);
			boolean hasSecond = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.SECOND)
				|| (offHandAxe != null && offHandAxe.getVariant() == Variant.SECOND);

			if (!hasFirst) {
				createVariant = Variant.FIRST;
				player.getInventory().addItem(createItem(player).getStack());
			}

			if (!hasSecond) {
				ItemStack currentOffHand = player.getInventory().getItemInOffHand();

				createVariant = Variant.SECOND;
				BerserkerAxe secondAxe = createItem(player);

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

			SpecialItem.findAll(player.getInventory(), this::getItem)
				.forEach(axe -> {
					axe.addXp(finalDamage);
				});
		}

		@Override
		protected BerserkerAxe getItemInternal(LevelItem.ItemData<BerserkerAxeLevels> info) {
			Variant variant = getSpecialItemVariant(info.info().stack());
			if (variant == null) return null;

			return new BerserkerAxe(variant, info, this);
		}

		public final BerserkerAxe createItemInternal(Variant variant, LevelData<BerserkerAxeLevels> data, Player owner) {
			BerserkerAxeLevels currentLevel = data.getCurrentLevelOr(BerserkerAxeLevels.IRON);
			BerserkerAxe created = new BerserkerAxe(variant,
				new LevelItem.ItemData<>(
					data,
					new SpecialItem.ItemData(new ItemStack(
						currentLevel.getMaterialForVariant(variant)
					), owner)
				), this
			);
			ItemMeta meta = created.getStack().getItemMeta();

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

			created.getStack().setItemMeta(meta);

			return created;
		}

		private Variant createVariant = Variant.FIRST;
		@Override
		protected BerserkerAxe createItemInternal(LevelData<BerserkerAxeLevels> data, Player owner) {
			return createItemInternal(createVariant, data, owner);
		}

		public static @Nullable Variant getSpecialItemVariant(ItemStack stack) {
			if (stack == null) return null;

			ItemMeta meta = stack.getItemMeta();
			if (meta == null) return null;


			PersistentDataContainer container = meta.getPersistentDataContainer();

			if (! container.has(VARIANT_KEY, PersistentDataType.INTEGER) ) return null;
			return Variant.fromKey(container.get(VARIANT_KEY, PersistentDataType.INTEGER));
		}

		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.ludos().getRoleManager().isPlayerRole(owner, BerserkerRole.ID);
		}
	}
}
