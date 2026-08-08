package fr.ludos.core.gui.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.GuiObject;
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.persistence.config.ConfigNode;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.window.Window;

/**
 * Clickable sub-menu item to interact with a {@link ConfigNode}'s config.
 */
public class WindowItem extends EventItem<WindowItem> {
	private final WindowProvider provider;
	private final GuiObject object;
	private final GuiContext context;
	private List<Runnable> openHandlers;
	private List<Runnable> closeHandlers;
	private List<Consumer<InventoryClickEvent>> outsideClickHandlers;

	public WindowItem(WindowProvider provider, GuiObject object, GuiContext context) {
		this.provider = Objects.requireNonNull(provider);
		this.object = Objects.requireNonNull(object);
		this.context = Objects.requireNonNull(context);
	}

	public static <T extends WindowProvider & GuiObject> WindowItem of(T object, GuiContext context) {
		return new WindowItem(object, object, context);
	}

	public @NotNull WindowItem setOpenHandlers(@NotNull List<@NotNull Runnable> openHandlers) {
		this.openHandlers = openHandlers;
		return this;
	}
	public @NotNull WindowItem addOpenHandler(@NotNull Runnable openHandler) {
		if (openHandlers == null)
			openHandlers = new ArrayList<>();

		openHandlers.add(openHandler);
		return this;
	}

	public @NotNull WindowItem setCloseHandlers(@NotNull List<@NotNull Runnable> closeHandlers) {
		this.closeHandlers = closeHandlers;
		return this;
	}
	public @NotNull WindowItem addCloseHandler(@NotNull Runnable closeHandler) {
		if (closeHandlers == null)
			closeHandlers = new ArrayList<>();

		closeHandlers.add(closeHandler);
		return this;
	}

	public @NotNull WindowItem setOutsideClickHandlers(@NotNull List<@NotNull Consumer<InventoryClickEvent>> outsideClickHandlers) {
		this.outsideClickHandlers = outsideClickHandlers;
		return this;
	}
	public @NotNull WindowItem addOutsideClickHandler(@NotNull Consumer<InventoryClickEvent> outsideClickHandler) {
		if (outsideClickHandlers == null)
			outsideClickHandlers = new ArrayList<>();

		outsideClickHandlers.add(outsideClickHandler);
		return this;
	}

	@Override
	public void handleClickInternal(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		Window window = provider.window(player, context.withWindow(provider));
		if (window == null) {
			WindowProvider.playDenySound(player);
			return;
		}

		if (closeHandlers != null) {
			for (Runnable closeHandler : closeHandlers) {
				window.addCloseHandler(closeHandler);
			}
		}
		if (openHandlers != null) {
			for (Runnable openHandler : openHandlers) {
				window.addOpenHandler(openHandler);
			}
		}
		if (outsideClickHandlers != null) {
			for (Consumer<InventoryClickEvent> outsideClickHandler : outsideClickHandlers) {
				window.addOutsideClickHandler(outsideClickHandler);
			}
		}
		window.open();
	}

	@Override
	public ItemProvider getItemProvider(Player viewer) {
		return object.displayItem(viewer, context);
	}

}
