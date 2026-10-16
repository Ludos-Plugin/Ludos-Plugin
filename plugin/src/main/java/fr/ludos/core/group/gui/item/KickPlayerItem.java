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
import fr.ludos.core.gui.WindowProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * An item that represents the action of kicking a player from a group. When clicked, it will attempt to kick the player from the group.
 */
public class KickPlayerItem extends AbstractItem implements GuiObject {
	private final GroupManager manager;
	private final OfflinePlayer target;
	private boolean wasKicked = false;

	public KickPlayerItem(GroupManager manager, OfflinePlayer target) {
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

		if (group.isLeader(target)) {
			player.sendMessage(Component.text("Cannot kick the group leader.").color(NamedTextColor.RED));
			return;
		}

		if (! manager.getManageAuthz().checkAuthorizationNotify(player)) {
			WindowProvider.playDenySound(player);
			return;
		}

		if (! group.removePlayer(target, true)) {
			player.sendMessage(Component.text("Failed to kick player.").color(NamedTextColor.RED));
		} else {
			wasKicked = true;
			manager.saveData();
			notifyWindows();
		}
	}


	@Override
	public ItemProvider getItemProvider() {
		if (wasKicked) {
			return ItemProvider.EMPTY;
		}
		return this.displayItem(null);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Kick Player").color(NamedTextColor.RED);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.BARRIER);
	}
}
