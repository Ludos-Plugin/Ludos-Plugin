package fr.ludos.core.gui.configValue.display;

import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * Displays a {@link PersistentEntry}'s value in the given {@link ConfigSectionContext}, needs to be manually updated using ({@link #notifyWindows()}).
 * @param <T> The type of the data of the entry
 */
public abstract class DisplayValueItem<T extends Object> extends AbstractItem {
	protected final PersistentEntry<T> entry;
	protected final ConfigSectionContext context;

	public DisplayValueItem(PersistentEntry<T> entry, ConfigSectionContext context) {
		this.entry = Objects.requireNonNull(entry);
		this.context = Objects.requireNonNull(context);
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		event.setCancelled(true);
	}

	@Override
	public ItemProvider getItemProvider(Player player) {
		ConfigurationSection config = context.getConfig(player);
		T value = entry.getValueOrDefault(config);

		return getItemProvider(value, player);
	}
	public abstract ItemProvider getItemProvider(T value, Player player);

}