package fr.ludos.core.gui;

import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.gui.item.BorderItem;
import fr.ludos.core.gui.item.ChangePageItem;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.window.Window;

/**
 * Utility for {@link Window}s and {@link WindowProvider}s.
 */
public class WindowUtility {
	private WindowUtility() {}

	/**
	 * A Stateful settings data-type for {@link Window}s, mainly used to allow for Modal-like advancing and returning.
	 */
	public static class WindowSettings {
		private static final Structure BASE_STRUCTURE =
			new Structure(
				"# # # # # # # # #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# # # # P # # # #"
			)
			.addIngredient('#', BorderItem.INSTANCE)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('P', ChangePageItem.INSTANCE);

		private PagedGui.Builder<Item> guiBuilder;
		private @Nullable Structure structure;
		public boolean doReturn;

		public WindowSettings(boolean doModalReturn) {
			this.doReturn = doModalReturn;
		}
		public WindowSettings() {
			this(false);
		}

		public final WindowSettings setStructure(Structure structure) {
			this.structure = structure;
			return this;
		}
		public final WindowSettings setGui(PagedGui.Builder<Item> guiBuilder) {
			this.guiBuilder = guiBuilder;
			return this;
		}

		public final Structure getStructure() {
			return structure != null
				? structure
				: BASE_STRUCTURE;
		}
		public final PagedGui.Builder<Item> getGuiBuilder() {
			if (guiBuilder == null) return PagedGui.items()
				.setStructure(getStructure());
			return guiBuilder;
		}
	}

	public static Window pagedItemsWindow(Player player, GuiContext context, List<Item> items, Component name, WindowSettings settings) {
		if (items.isEmpty()) return null;

		Window.Builder.Normal.Single windowBuilder = Window.single()
			.setTitle(new AdventureComponentWrapper(name))
			.setGui(settings.getGuiBuilder()
				.setContent(items)
			);
		if (settings != null) {
			windowBuilder.addCloseHandler(() -> {
				if (settings.doReturn) {
					context.openPreviousWindow(player);
					settings.doReturn = false;
				}
			});
		}
		return windowBuilder.build(player);
	}
}