package fr.ludos.core.gui.item;

import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.GuiObject;
import xyz.xenondevs.invui.item.ItemProvider;

/**
 * Clickable action item to execute a {@link Runnable}.
 */
public class ActionItem extends EventItem<ActionItem> {
	private final Runnable action;
	private final GuiObject object;
	private final GuiContext context;

	public ActionItem(Runnable action, GuiObject object, GuiContext context) {
		this.action = Objects.requireNonNull(action);
		this.object = Objects.requireNonNull(object);
		this.context = Objects.requireNonNull(context);
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		action.run();
	}

	@Override
	public ItemProvider getItemProvider(Player viewer) {
		return object.displayItem(viewer, context);
	}
}
