package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.item.PlayerItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * An item that represents a player that can be kicked from a group. When clicked, it will attempt to kick the player from the group.
 */
public class KickPlayerItem extends AbstractItem {
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
		return new PlayerItemBuilder(target);
	}

}
