package fr.ludos.core.group.gui;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.WindowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} which provides a GUI to see all players in a group.
 */
public class GroupPlayerListGui implements WindowObject {
	private final GroupManager manager;

	public GroupPlayerListGui(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Players list");
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.PLAYER_HEAD);
	}

	@Override
	public Window window(Player player, GuiContext context) {
		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return null;
		}

		GuiContext childrenContext = context.deeper();

		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);
		List<Item> items = manager.getLudos().getServer().getOnlinePlayers().stream()
			.filter(Objects::nonNull)
			.filter(group::isPlayer)
			.map(target -> WindowItem.of(new GroupPlayerGui(manager, target), childrenContext).addActionHandler(settings::disableModalReturn))
			.collect(Collectors.toList());

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}
}
