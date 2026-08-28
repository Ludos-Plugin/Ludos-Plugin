package fr.ludos.core.game.gui;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.game.gui.item.StartGameItem;
import fr.ludos.core.gui.ConfigHolder;
import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.gui.WindowUtility;
import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import fr.ludos.core.gui.item.GuidebookItem;
import fr.ludos.core.gui.item.WindowItem;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * A Gui to represent all actions possible for a single {@link Game}.
 */
public class GameMenuGui implements WindowProvider {
	private final Game.Builder game;
	private final GameManager manager;

	public GameMenuGui(Game.Builder game, GameManager manager) {
		this.game = game;
		this.manager = manager;
	}

	public static WindowItem item(Game.Builder game, GameManager manager, GuiContext context) {
		return new WindowItem(new GameMenuGui(game, manager), game, context);
	}

	@Override
	public TextComponent displayName() {
		return game.displayName();
	}

	@Override
	public Window window(Player player, GuiContext context) {
		GuiContext childrenContext = context.setWindow(this).deeper();
		WindowUtility.WindowSettings settings = new WindowUtility.WindowSettings(true);

		ArrayList<Item> items = new ArrayList<>() {{
			add(new StartGameItem(game, manager).addClickHandler(settings::disableModalReturn));
			add(new GuidebookItem(game).addClickHandler(t -> {
				if (t.isLeftClick()) {
					settings.disableModalReturn();
				}
			}));
		}};

		if (items.isEmpty()) return null;

		ConfigNodeCollection gameConfig = game.getConfig();
		boolean canConfig = gameConfig != null && manager.getLudos().getGroupManager().getConfigAuthz().checkAuthorizationSilent(player);
		Item configItem = canConfig
			? new WindowItem(Game.scopeConfig(manager.getLudos(), gameConfig), ConfigHolder.CONFIG_GUI_OBJECT, childrenContext).addClickHandler(settings::disableModalReturn)
			: BorderItem.INSTANCE;

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

}
