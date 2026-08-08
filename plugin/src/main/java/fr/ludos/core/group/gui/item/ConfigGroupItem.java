package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.GroupManager;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * An item that allows a Player to leave their current Group.
 */
public class ConfigGroupItem extends AbstractItem {
	private final GroupManager manager;

	public ConfigGroupItem(GroupManager manager) {
		this.manager = Objects.requireNonNull(manager);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		manager.getConfigMap().openWindow(player, manager.getLudos());
	}


	@Override
	public ItemProvider getItemProvider() {
		return new ItemBuilder(Material.LEVER);
	}
}
