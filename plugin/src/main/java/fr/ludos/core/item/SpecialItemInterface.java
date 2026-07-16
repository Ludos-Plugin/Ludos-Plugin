package fr.ludos.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public interface SpecialItemInterface {
	public static final String TYPE_ID_KEY_STRING = "type_id";
	public static final NamespacedKey TYPE_ID_KEY = new NamespacedKey(Ludos.NAMESPACE, TYPE_ID_KEY_STRING);

	public static final String ITEM_ID_KEY_STRING = "item_id";
	public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(Ludos.NAMESPACE, ITEM_ID_KEY_STRING);

	public static final String OWNER_KEY_STRING = "owner";
	public static final NamespacedKey OWNER_KEY = new NamespacedKey(Ludos.NAMESPACE, OWNER_KEY_STRING);


	Game getGame();
	public ItemStack getStack();
	public Player getOwner();

	public String getTypeId();
	public Component getName();
	public default List<Component> getLore() {
		return new ArrayList<>();
	}

	public void update();

	public static @Nullable UUID getSpecialItemId(ItemStack stack, String typeId, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(TYPE_ID_KEY, PersistentDataType.STRING) ) return null;
		String found = container.get(TYPE_ID_KEY, PersistentDataType.STRING);

		if (! found.equals(typeId)) return null;
		if (! container.has(ITEM_ID_KEY, PersistentDataType.STRING) ) return null;

		return UUID.fromString(
			container.get(ITEM_ID_KEY, PersistentDataType.STRING)
		);
	}

	public static @Nullable Player getSpecialItemOwner(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(OWNER_KEY, PersistentDataType.STRING) ) return null;

		Player owner = Bukkit.getPlayer(
			UUID.fromString(
				container.get(OWNER_KEY, PersistentDataType.STRING)
			)
		);

		return owner;
	}
	public static Component getActionAnnotation(final @NotNull String keybind, Component action) {
		return Component.text("Press ")
				.color(NamedTextColor.GRAY)
			.append(
				Component.keybind(keybind)
					.color(NamedTextColor.YELLOW)
			)
			.append(
				Component.text(" to ")
					.color(NamedTextColor.GRAY)
			)
			.append(action)
			.decoration(TextDecoration.ITALIC, false);
	}
}
