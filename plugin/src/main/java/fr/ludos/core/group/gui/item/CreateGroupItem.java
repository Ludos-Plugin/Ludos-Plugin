package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.GroupGui;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.GuiObject;
import fr.ludos.core.gui.item.EventItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * An item that allows any Player to create their own Group.
 */
public class CreateGroupItem extends EventItem<CreateGroupItem> implements GuiObject {
	private final GroupManager manager;
	private final GroupGui window;
	private final GuiContext context;

	public CreateGroupItem(GroupManager manager, GroupGui window, GuiContext context) {
		this.manager = Objects.requireNonNull(manager);
		this.window = Objects.requireNonNull(window);
		this.context = Objects.requireNonNull(context);
	}

	@Override
	public final void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		manager.createGroup(player, null);
		manager.saveData();

		window.openWindow(player, context);
	}


	@Override
	public ItemProvider getItemProvider(Player player) {
		return this.displayItem(player);
	}

	@Override
	public Component displayName() {
		return Component.text("Create new Group").color(NamedTextColor.GREEN);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.CRAFTING_TABLE);
	}
}
