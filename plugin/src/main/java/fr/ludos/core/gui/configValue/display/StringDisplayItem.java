package fr.ludos.core.gui.configValue.display;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.persistence.PersistentAccessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Dummy item that automatically fetches a {@link PersistentAccessor}'s value. Needs to be manually updated via {@link #notifyWindows()}.
 */
public class StringDisplayItem extends DisplayValueItem<String> {
	public StringDisplayItem(PersistentAccessor<String> entry) {
		super(entry);
	}

	@Override
	public ItemProvider getItemProvider(String value, Player player) {
		return new ItemBuilder(Material.PAPER)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text(value)
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
			))
			.addLoreLines(new AdventureComponentWrapper(
				Component.text("Current value")
					.decoration(TextDecoration.ITALIC, true)
					.color(NamedTextColor.DARK_GRAY)
			));
	}

}
