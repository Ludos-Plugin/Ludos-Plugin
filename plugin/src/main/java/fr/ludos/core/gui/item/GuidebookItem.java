package fr.ludos.core.gui.item;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuidebookProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * An Item used to open a Guidebook interface.
 */
public class GuidebookItem extends EventItem<GuidebookItem> {
	public final static AbstractItemBuilder<?> GUIDEBOOK_ITEM = new AbstractItemBuilder<>(Material.WRITTEN_BOOK) {{
		setDisplayName(new AdventureComponentWrapper(
			Component.text("Guidebook")
				.color(NamedTextColor.GOLD)
				.decoration(TextDecoration.ITALIC, false)
		));
		addLoreLines(
			new AdventureComponentWrapper(Component.text("Press ")
				.color(NamedTextColor.GRAY)
			.append(
				Component.keybind("key.attack")
					.color(NamedTextColor.YELLOW)
			)
			.append(
				Component.text(" to open Guidebook")
					.color(NamedTextColor.GRAY)
			)
			.decoration(TextDecoration.ITALIC, false)),

			new AdventureComponentWrapper(Component.text("Press ")
				.color(NamedTextColor.GRAY)
			.append(
				Component.keybind("key.use")
					.color(NamedTextColor.YELLOW)
			)
			.append(
				Component.text(" to give Guidebook")
					.color(NamedTextColor.GRAY)
			)
			.decoration(TextDecoration.ITALIC, false))
		);
	}};
	private final GuidebookProvider provider;

	public GuidebookItem(GuidebookProvider provider) {
		this.provider = Objects.requireNonNull(provider);
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (clickType.isLeftClick()) {
			player.openBook(provider.createGuidebook());
		} else if (clickType.isRightClick()) {
			player.getInventory().addItem(provider.createGuidebook());
		}
	}

	@Override
	public ItemProvider getItemProvider(Player viewer) {
		return GUIDEBOOK_ITEM;
	}
}
