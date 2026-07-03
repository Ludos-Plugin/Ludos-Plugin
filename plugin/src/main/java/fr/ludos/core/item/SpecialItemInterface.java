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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public interface SpecialItemInterface {
	public static final String TYPE_ID = "type_id";
	public static final NamespacedKey typeIdKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), TYPE_ID);

	public static final String ITEM_ID_KEY = "item_id";
	public static final NamespacedKey itemIdKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ITEM_ID_KEY);

	public static final String OWNER_KEY = "owner";
	public static final NamespacedKey ownerKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), OWNER_KEY);


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

		if (! container.has(typeIdKey, PersistentDataType.STRING) ) return null;
		String found = container.get(typeIdKey, PersistentDataType.STRING);

		if (! found.equals(typeId)) return null;
		if (! container.has(itemIdKey, PersistentDataType.STRING) ) return null;

		return UUID.fromString(
			container.get(itemIdKey, PersistentDataType.STRING)
		);
	}

	public static @Nullable Player getSpecialItemOwner(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(ownerKey, PersistentDataType.STRING) ) return null;

		Player owner = Bukkit.getPlayer(
			UUID.fromString(
				container.get(ownerKey, PersistentDataType.STRING)
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
