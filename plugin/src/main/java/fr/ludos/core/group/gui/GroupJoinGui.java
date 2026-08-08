package fr.ludos.core.group.gui;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.item.JoinPlayerItem;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the group invite command, which provides a GUI for inviting players to a group.
 */
public class GroupJoinGui implements WindowObject {
	private final GroupManager manager;

	public GroupJoinGui(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public Component displayName() {
		return Component.text("Join another Player's Group");
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.WRITABLE_BOOK);
	}

	@Override
	public Window window(Player player, GuiContext context) {
		Group group = manager.getGroupOfPlayer(player);
		Predicate<Player> isNotInSameGroup = group != null ? Predicate.not(group::isPlayer) : p -> true;

		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);
		List<Item> items = manager.getLudos().getServer().getOnlinePlayers().stream()
			.filter(Objects::nonNull)
			.filter(p -> p != player)
			.filter(isNotInSameGroup)
			.filter(p -> manager.getGroupOfPlayer(p) != null)
			.map(target -> new JoinPlayerItem<>(manager, target))
			.collect(Collectors.toList());

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}
}
