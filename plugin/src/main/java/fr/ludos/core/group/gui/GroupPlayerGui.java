package fr.ludos.core.group.gui;

import java.util.ArrayList;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.item.KickPlayerItem;
import fr.ludos.core.group.gui.item.PromotePlayerItem;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.PlayerItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} which provides a GUI to see all {@link Group}-specific options for a player.
 */
public class GroupPlayerGui implements WindowObject {
	private final GroupManager manager;
	private final OfflinePlayer target;

	public GroupPlayerGui(GroupManager manager, OfflinePlayer target) {
		this.manager = manager;
		this.target = target;
	}

	@Override
	public Window window(Player player, GuiContext context) {
		if (player == target) {
			return null;
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return null;
		}

		context.setWindow(this);
		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);

		ArrayList<Item> items = new ArrayList<>();

		if (group.isLeader(player)) {
			items.add(new PromotePlayerItem(manager, target));
		}

		if (! group.isLeader(target) && manager.getManageAuthz().checkAuthorizationSilent(player)) {
			items.add(new KickPlayerItem(manager, target));
		}

		settings
			.setStructure(
				new Structure(
					"D # # # # # # # #",
					"# x x x x x x x #",
					"# # # # P # # # #"
				)
				.addIngredient('#', BorderItem.INSTANCE)
				.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('P', ChangePageItem.INSTANCE)
				.addIngredient('D', new SimpleItem(new PlayerItemBuilder(target)))
			);

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}

	@Override
	public TextComponent displayName() {
		return Component.text(target.getName());
	}

	@Override
	public @NotNull AbstractItemBuilder<?> createItem(Player player) {
		PlayerItemBuilder builder = new PlayerItemBuilder(target);
		Group group = manager.getGroupOfPlayer(target);

		if (group != null && group.isLeader(target)) {
			builder.addLoreLines(new AdventureComponentWrapper(
				Component.text("Group Leader").color(NamedTextColor.GOLD).decorate(TextDecoration.ITALIC)
			));
		}
		return builder;
	}
}
