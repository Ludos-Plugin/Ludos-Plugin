package fr.ludos.core.persistence.config;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigNode extends ConfigRoot {
	public @Nullable String key();

	public AbstractItemBuilder<?> item(Player player, ConfigSectionContext context);
	public default AbstractItemBuilder<?> displayItem(Player player, ConfigSectionContext context) {
		AbstractItemBuilder<?> builder = item(player, context);
		builder.setDisplayName(new AdventureComponentWrapper(displayName()));
		builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
		return builder;
	}
}
