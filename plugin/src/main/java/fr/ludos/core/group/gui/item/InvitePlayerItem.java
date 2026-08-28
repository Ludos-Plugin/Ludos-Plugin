package fr.ludos.core.group.gui.item;

import java.util.Objects;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.AddPlayerMethod;
import fr.ludos.core.group.Group.AddPlayerResult;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.item.PlayerItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * An item that represents a player that can be invited to a group. When clicked, it will attempt to invite the player to the group.
 */
public class InvitePlayerItem extends AbstractItem {
	private final GroupManager manager;
	private final OfflinePlayer target;
	private AddPlayerResult status = null;

	public InvitePlayerItem(GroupManager manager, OfflinePlayer target) {
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

		if (! manager.getInviteAuthz().checkAuthorizationNotify(player)) {
			WindowProvider.playDenySound(player);
			return;
		}

		status = group.requestAddPlayer(target, AddPlayerMethod.Invite);
		switch (status) {
			case Failed:
				player.sendMessage(Component.text("Failed to invite player.").color(NamedTextColor.RED));
				break;
			case Succeeded:
				manager.saveData();
			case Requested:
				notifyWindows();
				break;
		}
	}


	@Override
	public ItemProvider getItemProvider() {
		if (status == AddPlayerResult.Succeeded) return ItemBuilder.EMPTY;

		PlayerItemBuilder builder = new PlayerItemBuilder(target);
		if (status != null) {
			builder.addLoreLines(new AdventureComponentWrapper(
				Component.text("Invited").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)
			));
		}
		return builder;
	}

}
