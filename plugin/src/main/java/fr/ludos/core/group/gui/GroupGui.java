package fr.ludos.core.group.gui;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.group.gui.item.CreateGroupItem;
import fr.ludos.core.group.gui.item.DisbandGroupItem;
import fr.ludos.core.group.gui.item.LeaveGroupItem;
import fr.ludos.core.gui.ConfigHolder;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.WindowItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the group command, which provides a GUI for the group command and its subcommands.
 */
public class GroupGui implements WindowProvider {
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
			items.add(new CreateGroupItem(manager, this, context).addClickHandler(settings::disableModalReturn));
			items.add(WindowItem.of(joinGui, childrenContext).addClickHandler(settings::disableModalReturn));
		}
		else {
			if (inviteGui.checkAuthorizationSilent(player)) {
				items.add(WindowItem.of(inviteGui, childrenContext).addClickHandler(settings::disableModalReturn));
			}
			if (kickGui.checkAuthorizationSilent(player)) {
				items.add(WindowItem.of(kickGui, childrenContext).addClickHandler(settings::disableModalReturn));
			}
		}

		boolean canConfig = manager.getConfigAuthz().checkAuthorizationSilent(player);
		Item configItem = canConfig
			? new WindowItem(manager.getScopeConfigMap(), ConfigHolder.CONFIG_GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn)
			: BorderItem.INSTANCE;
		boolean canManage = manager.getManageAuthz().checkAuthorizationSilent(player);
		Item disbandItem = canManage
			? new DisbandGroupItem<>(manager)
			: BorderItem.INSTANCE;

		settings
			.setStructure(
				group != null
					? new Structure(
						"# # # # # # # # C",
						"# x x x x x x x #",
						"L # # # P # # # D"
					)
					.addIngredient('#', BorderItem.INSTANCE)
					.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
					.addIngredient('P', ChangePageItem.INSTANCE)
					.addIngredient('C', configItem)
					.addIngredient('L', new LeaveGroupItem<>(manager))
					.addIngredient('D', disbandItem)

					: new Structure(
						"# # # # # # # # #",
						"# x x x x x x x #",
						"# # # # P # # # #"
					)
					.addIngredient('#', BorderItem.INSTANCE)
					.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
					.addIngredient('P', ChangePageItem.INSTANCE)
			);

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Group");
	}
}