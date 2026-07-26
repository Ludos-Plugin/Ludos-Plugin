package fr.ludos.core.gui.configValue.display;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.core.gui.PlayerItemBuilder;
import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Dummy item that automatically fetches a {@link PersistentEntry}'s value. Needs to be manually updated via {@link #notifyWindows()}.
 */
public class PlayerDisplayItem extends DisplayValueItem<OfflinePlayer> {
	public PlayerDisplayItem(PersistentEntry<OfflinePlayer> entry, ConfigSectionContext context) {
		super(entry, context);
	}

	@Override
	public ItemProvider getItemProvider(OfflinePlayer value, Player player) {
		OfflinePlayer current = entry.getValueOrDefault(context.getConfig(player));
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
