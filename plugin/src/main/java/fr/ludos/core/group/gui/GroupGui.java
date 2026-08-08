package fr.ludos.core.group.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.item.CreateGroupItem;
import fr.ludos.core.group.gui.item.DisbandGroupItem;
import fr.ludos.core.group.gui.item.LeaveGroupItem;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.GuiObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.WindowItem;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the group command, which provides a GUI for the group command and its subcommands.
 */
public class GroupGui implements WindowProvider {
	private static final GuiObject CONFIG_OBJECT = new GuiObject() {
		@Override
		public AbstractItemBuilder<?> createItem(Player player) {
			return new ItemBuilder(Material.LEVER);
		}
		@Override
		public Component displayName() {
			return Component.text("Configure Group");
		}
	};
	private final GroupManager manager;
	private final GroupJoinGui joinGui;
	private final GroupInviteGui inviteGui;
	private final GroupKickGui kickGui;

	public GroupGui(GroupManager manager, GroupJoinGui joinGui, GroupInviteGui inviteGui, GroupKickGui kickGui) {
		this.manager = manager;
		this.joinGui = joinGui;
		this.inviteGui = inviteGui;
		this.kickGui = kickGui;
	}

	@Override
	public Window window(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper();
		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);

		ArrayList<Item> items = new ArrayList<>();

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			items.add(new CreateGroupItem(manager, this, context).addClickHandler(() -> settings.doReturn = false));
			items.add(WindowItem.of(joinGui, childrenContext).addClickHandler(() -> settings.doReturn = false));
		}
		else {
			if (inviteGui.checkAuthorizationSilent(player)) {
				items.add(WindowItem.of(inviteGui, childrenContext).addClickHandler(() -> settings.doReturn = false));
			}
			if (kickGui.checkAuthorizationSilent(player)) {
				items.add(WindowItem.of(kickGui, childrenContext).addClickHandler(() -> settings.doReturn = false));
			}
		}

		if (items.isEmpty()) return null;

		boolean canConfig = manager.getManageAuthz().checkAuthorizationSilent(player);
		Item configItem = canConfig
			? new WindowItem(manager.getConfigMap(), CONFIG_OBJECT, childrenContext).addClickHandler(() -> settings.doReturn = false)
			: BorderItem.INSTANCE;
		Item disbandItem = canConfig
			? new DisbandGroupItem<>(manager)
			: BorderItem.INSTANCE;

		settings
			.setStructure(
				new Structure(
					group == null ? "# # # # # # # # #" : "# # # # # # # # C",
					"# x x x x x x x #",
					group == null ? "# # # # P # # # #" : "L # # # P # # # D"
				)
				.addIngredient('#', BorderItem.INSTANCE)
				.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('P', ChangePageItem.INSTANCE)
				.addIngredient('C', configItem)
				.addIngredient('L', new LeaveGroupItem<>(manager))
				.addIngredient('D', disbandItem)
			);

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}

	@Override
	public Component displayName() {
		return Component.text("Group");
	}
}