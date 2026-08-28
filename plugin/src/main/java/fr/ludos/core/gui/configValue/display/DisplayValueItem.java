package fr.ludos.core.gui.configValue.display;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.PersistentAccessor;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * Displays a {@link PersistentAccessor}'s value, needs to be manually updated using ({@link #notifyWindows()}).
 * @param <T> The type of the data of the entry
 */
public abstract class DisplayValueItem<T extends Object> extends AbstractItem {
	protected final PersistentAccessor<T> entry;

	public DisplayValueItem(PersistentAccessor<T> entry) {
		this.entry = Objects.requireNonNull(entry);
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@Override
	public ItemProvider getItemProvider(Player player) {
		T value = entry.getOrDefault();

		return getItemProvider(value, player);
	}
	public abstract ItemProvider getItemProvider(T value, Player player);

}