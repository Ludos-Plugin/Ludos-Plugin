package fr.ludos.core.game.gui;

import java.util.List;

import org.bukkit.entity.Player;

import fr.ludos.core.game.GameManager;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * A {@link WindowProvider} for the game command, which provides a GUI for the game command and its subcommands.
 */
public class GameGui implements WindowProvider {
	private final GameManager manager;

	public GameGui(GameManager manager) {
		this.manager = manager;
	}

	@Override
	public Window window(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper();
		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);

		List<Item> items = manager.getBuilders().stream()
			.map(g -> (Item) GameMenuGui.item(g, manager, childrenContext).addClickHandler(settings::disableModalReturn))
			.toList();

		if (items.isEmpty()) return null;

		settings
			.setStructure(
				new Structure(
					"# # # # # # # # #",
					"# x x x x x x x #",
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
		return Component.text("Games");
	}
}