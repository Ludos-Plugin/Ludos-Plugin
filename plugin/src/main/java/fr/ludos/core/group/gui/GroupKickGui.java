package fr.ludos.core.group.gui;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.item.KickPlayerItem;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.security.AccessAuthorization;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the group kick command, which provides a GUI for kicking players from a group.
 */
public class GroupKickGui implements WindowObject {
	private final GroupManager manager;

	public GroupKickGui(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public Component displayName() {
		return Component.text("Kick Players from Group");
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.BARRIER);
	}

	@Override
	public Window window(Player player, GuiContext context) {
		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return null;
		}

		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);
		List<Item> items = manager.getLudos().getServer().getOnlinePlayers().stream()
			.filter(Objects::nonNull)
			.filter(p -> p != player)
			.filter(group::isPlayer)
			.map(target -> new KickPlayerItem(manager, target))
			.collect(Collectors.toList());

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}

	@Override
	public AccessAuthorization getAccessAuthorization() {
		return manager.getManageAuthz();
	}
}
