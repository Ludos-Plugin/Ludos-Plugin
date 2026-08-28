package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.GuiObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * An item that allows a Player to leave their current Group.
 * @param <G> The type of Gui
 */
public class LeaveGroupItem<G extends Gui> extends ControlItem<G> implements GuiObject {
	private final GroupManager manager;

	public LeaveGroupItem(GroupManager manager) {
		this.manager = Objects.requireNonNull(manager);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			player.sendMessage(Component.text("You are not in a group.").color(NamedTextColor.RED));
			return;
		}

		if (group.removePlayer(player, false)) {
			manager.saveData();
		}

		getGui().closeForAllViewers();
	}


	@Override
	public ItemProvider getItemProvider(G gui) {
		return this.displayItem(null);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Leave current Group").color(NamedTextColor.DARK_PURPLE);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.STRUCTURE_VOID);
	}
}
