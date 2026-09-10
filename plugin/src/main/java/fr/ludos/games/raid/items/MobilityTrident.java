package fr.ludos.games.raid.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.games.raid.RaidGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of MobilityTrident, managed by {@link RaidGame}.
 */
public class MobilityTrident extends SpecialItem<MobilityTrident> {
	private static final String ID = "mobility_trident";


	MobilityTrident(SpecialItem.ItemData info, Events events) {
		super(info, events);
	}

	static MobilityTrident createItem(Player owner, Events events) {
		return new MobilityTrident(new SpecialItem.ItemData(createItemStack(), owner), events);
	}



	@Override
	public TextComponent displayName() {
		return Component.text("Mobility Trident");
	}

	@Override
	public List<Component> getLore() {
		return new ArrayList<Component>(){{
			add(
				Component.keybind("key.use")
					.append(Component.text(" to lunge in the Water.")
				)
				.decoration(TextDecoration.ITALIC, false)
			);
		}};
	}


	private static ItemStack createItemStack() {
		ItemStack mobilityTrident = new ItemStack(Material.TRIDENT);
		mobilityTrident.addUnsafeEnchantment(Enchantment.RIPTIDE, 2);
		mobilityTrident.addUnsafeEnchantment(Enchantment.DURABILITY, 3);

		return mobilityTrident;
	}

	/**
	 * Events for {@link MobilityTrident}.
	 */
	public static class Events extends SpecialItem.Events<MobilityTrident> {

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_2));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		@Nullable
		protected MobilityTrident getItemInternal(SpecialItem.ItemData info) {
			return new MobilityTrident(info, this);
		}

		@Override
		protected MobilityTrident createItemInternal(Player owner) {
			return MobilityTrident.createItem(owner, this);
		}
	}
}
