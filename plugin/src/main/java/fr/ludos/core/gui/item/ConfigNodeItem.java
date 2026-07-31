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
import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.persistence.config.ConfigNode;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

/**
 * Clickable sub-menu item to interact with a {@link ConfigNode}'s config.
 */
public class ConfigNodeItem extends AbstractItem {
	private final ConfigNode node;
	private final GuiContext context;
	private List<Runnable> clickHandlers;
	private List<Runnable> openHandlers;
	private List<Runnable> closeHandlers;
	private List<Consumer<InventoryClickEvent>> outsideClickHandlers;

	public ConfigNodeItem(ConfigNode node, GuiContext context) {
		this.node = Objects.requireNonNull(node);
		this.context = Objects.requireNonNull(context);
	}

	public @NotNull ConfigNodeItem setClickHandlers(@NotNull List<@NotNull Runnable> clickHandlers) {
		this.clickHandlers = clickHandlers;
		return this;
	}
	public @NotNull ConfigNodeItem addClickHandler(@NotNull Runnable clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(clickHandler);
		return this;
	}

	public @NotNull ConfigNodeItem setOpenHandlers(@NotNull List<@NotNull Runnable> openHandlers) {
		this.openHandlers = openHandlers;
		return this;
	}
	public @NotNull ConfigNodeItem addOpenHandler(@NotNull Runnable openHandler) {
		if (openHandlers == null)
			openHandlers = new ArrayList<>();

		openHandlers.add(openHandler);
		return this;
	}

	public @NotNull ConfigNodeItem setCloseHandlers(@NotNull List<@NotNull Runnable> closeHandlers) {
		this.closeHandlers = closeHandlers;
		return this;
	}
	public @NotNull ConfigNodeItem addCloseHandler(@NotNull Runnable closeHandler) {
		if (closeHandlers == null)
			closeHandlers = new ArrayList<>();

		closeHandlers.add(closeHandler);
		return this;
	}

	public @NotNull ConfigNodeItem setOutsideClickHandlers(@NotNull List<@NotNull Consumer<InventoryClickEvent>> outsideClickHandlers) {
		this.outsideClickHandlers = outsideClickHandlers;
		return this;
	}
	public @NotNull ConfigNodeItem addOutsideClickHandler(@NotNull Consumer<InventoryClickEvent> outsideClickHandler) {
		if (outsideClickHandlers == null)
			outsideClickHandlers = new ArrayList<>();

		outsideClickHandlers.add(outsideClickHandler);
		return this;
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		Window window = node.configWindow(player, context.withWindow(node));
		if (window == null) {
			WindowProvider.playDenySound(player);
			return;
		}

		if (clickHandlers != null) {
			for (Runnable clickHandler : clickHandlers) {
				clickHandler.run();
			}
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
		return node.displayItem(viewer, context);
	}

}
