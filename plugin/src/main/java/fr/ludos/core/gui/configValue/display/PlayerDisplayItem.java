package fr.ludos.core.gui.configValue.display;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.core.gui.item.PlayerItemBuilder;
import fr.ludos.core.persistence.PersistentAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Dummy item that automatically displays a {@link Player}'s head. Needs to be manually updated via {@link #notifyWindows()}.
 */
public class PlayerDisplayItem extends DisplayValueItem<OfflinePlayer> {
	public PlayerDisplayItem(PersistentAccessor<OfflinePlayer> entry) {
		super(entry);
	}

	@Override
	public ItemProvider getItemProvider(OfflinePlayer value, Player player) {
		OfflinePlayer current = entry.getOrDefault();
		if (current == null) {
			return new ItemBuilder(Material.STRUCTURE_VOID)
				.setDisplayName(new AdventureComponentWrapper(
					Component.text("No selection")
						.decoration(TextDecoration.ITALIC, false)
						.color(NamedTextColor.RED)
				));
		}
		return new PlayerItemBuilder(current);
	}
}
