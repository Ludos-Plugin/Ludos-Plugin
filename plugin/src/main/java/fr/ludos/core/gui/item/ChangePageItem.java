package fr.ludos.core.gui.item;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * Item used to 1.) display the current {@link PagedGui}'s page, 2.) navigate between the pages using right-click and left-click.
 */
public class ChangePageItem extends ControlItem<PagedGui<?>> {
	public static final ChangePageItem INSTANCE = new ChangePageItem();

	private ChangePageItem() {}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (clickType == ClickType.LEFT && getGui().hasNextPage()) {
			getGui().goForward();
			player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.1f, 1.1f);
		} else if (clickType == ClickType.RIGHT && getGui().hasPreviousPage()) {
			getGui().goBack();
			player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.1f, 0.9f);
		}
	}

	@Override
	public ItemProvider getItemProvider(PagedGui<?> gui) {
		return new ItemBuilder(Material.PAPER)
			.setDisplayName(
				new AdventureComponentWrapper(
					Component.text("Current page: ")
						.append(
							Component.text((gui.getCurrentPage() + 1) + "/" + gui.getPageAmount())
								.color(NamedTextColor.GOLD)
						)
				)
			)
			.addLoreLines(
				new AdventureComponentWrapper(
					Component.text("Left-click to go forward")
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
				),
				new AdventureComponentWrapper(
					Component.text("Right-click to go back")
						.color(NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)
				)
			);
	}
}