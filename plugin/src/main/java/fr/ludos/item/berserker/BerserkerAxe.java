package fr.ludos.item.berserker;

import java.util.List;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
	private final NamespacedKey variantKey;
	private final Variant variant;


	public Variant getVariant() {
		return variant;
	}


	public BerserkerAxe(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);

		variantKey = new NamespacedKey(game.getPlugin(), VARIANT_KEY);
		PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
		if (!container.has(variantKey, PersistentDataType.INTEGER)) {
			throw new IllegalArgumentException("Variant not found");
		}

		variant = Variant.fromKey(getPersistentData(stack, variantKey, PersistentDataType.INTEGER));
	}

	public BerserkerAxe(Player owner, Variant variant, Game game) {
		super(new ItemStack(Material.IRON_AXE), owner, game);

		this.variant = variant;
		this.variantKey = new NamespacedKey(game.getPlugin(), VARIANT_KEY);

		ItemMeta meta = getStack().getItemMeta();
		meta.getPersistentDataContainer().set(variantKey, PersistentDataType.INTEGER, variant.key());
		getStack().setItemMeta(meta);
	}


	@Override
	public String getId() {
		return "berserkerAxe";
	}

	@Override
	protected Component getName() {
		return Component.text("Berserker Axe")
			.decoration(TextDecoration.ITALIC, false);
	}

	@Override
	protected List<Component> getLore() {
		return List.of(
			Component.text("Twin axes with independent swings.")
				.decoration(TextDecoration.ITALIC, false)
		);
	}


	@Nullable
	public static BerserkerAxe getItem(ItemStack stack, Game game) {
		try {
			return new BerserkerAxe(stack, game);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static BerserkerAxe createItem(Player owner, Variant variant, Game game) {
		return new BerserkerAxe(owner, variant, game);
	}

	public static boolean isHoldingBerserkerAxe(Player player, Game game) {
		return getItem(player.getInventory().getItemInMainHand(), game) != null
			|| getItem(player.getInventory().getItemInOffHand(), game) != null;
	}


	public static class Events extends SpecialItem.Events<BerserkerAxe> {

		public Events(Game game) {
			super(game);
		}

		@Override
		public void updateItemInInventory(Player player) {
			if (!canPlayerHaveItem(player)) return;

			List<BerserkerAxe> axes = SpecialItem.findAllIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
			boolean hasFirst = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.FIRST);
			boolean hasSecond = axes.stream().anyMatch(axe -> axe.getVariant() == Variant.SECOND);

			if (!hasFirst) {
				player.getInventory().addItem(createItem(player, Variant.FIRST, game).getStack());
			}

			if (!hasSecond) {
				player.getInventory().addItem(createItem(player, Variant.SECOND, game).getStack());
			}
		}

		@Override
		@Nullable
		protected BerserkerAxe getItem(ItemStack stack, Game game) {
			return BerserkerAxe.getItem(stack, game);
		}

		@Override
		protected BerserkerAxe createItem(Player owner, Game game) {
			return BerserkerAxe.createItem(owner, Variant.FIRST, game);
		}

		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BerserkerRole.id);
		}
	}
}
