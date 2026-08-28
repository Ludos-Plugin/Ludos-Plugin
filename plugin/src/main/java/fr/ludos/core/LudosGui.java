package fr.ludos.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.gui.ConfigHolder;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.WindowItem;
import fr.ludos.core.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the Ludos command, which provides a GUI for its subcommands.
 */
public class LudosGui implements WindowProvider {
	private final Ludos ludos;

	public LudosGui(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public Window window(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper();
		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);

		List<Item> items = new ArrayList<>() {{
			add(new WindowItem(ludos.getGroupManager().gui, Group.GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn));
			add(new WindowItem(ludos.getGameManager().gui, Game.GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn));
			add(new WindowItem(ludos.getRoleManager().gui, Role.GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn));
		}};

		if (items.isEmpty()) return null;

		Item configItem = new WindowItem(ludos.scopeConfigMap, ConfigHolder.CONFIG_GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn);

		settings
			.setStructure(
				new Structure(
					"# # # # # # # # C",
					"# x x x x x x x #",
					"# # # # P # # # #"
				)
				.addIngredient('#', BorderItem.INSTANCE)
				.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('P', ChangePageItem.INSTANCE)
				.addIngredient('C', configItem)
			);

		return WindowUtility.pagedItemsWindow(player, context, items, normalizedDisplayName(), settings);
	}

	@Override
	public TextComponent displayName() {
		return Component.text("Ludos");
	}
}