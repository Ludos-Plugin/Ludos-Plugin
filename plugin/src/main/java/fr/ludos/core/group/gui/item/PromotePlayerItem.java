package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * An item that represents a player that can be promoted to Leader status, in a {@link Group}. When clicked, it will attempt to transfer the Group leader status to the target player.
 */
public class PromotePlayerItem extends AbstractItem implements GuiObject {
	private final GroupManager manager;
	private final OfflinePlayer target;
	private boolean wasPromoted = false;

	public PromotePlayerItem(GroupManager manager, OfflinePlayer target) {
		this.manager = Objects.requireNonNull(manager);
		this.target = Objects.requireNonNull(target);
	}


	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			player.sendMessage(Component.text("You are not in a group.").color(NamedTextColor.RED));
			return;
		}

		if (! group.isLeader(player)) {
			player.sendMessage(Component.text("Only the Group leader can Promote another player.").color(NamedTextColor.RED));
			return;
		}

		if (! group.promoteToLeader(target)) {
			player.sendMessage(Component.text("Could not promote Player.").color(NamedTextColor.RED));
		} else {
			wasPromoted = true;
			manager.saveData();
			notifyWindows();
		}
	}


	@Override
	public ItemProvider getItemProvider() {
		if (wasPromoted) {
			return ItemProvider.EMPTY;
		}
		return this.displayItem(null);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Promote Player").color(NamedTextColor.GOLD);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.NAME_TAG);
	}
}
