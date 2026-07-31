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
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.window.Window;

/**
 * A clickable sub-menu item used to pick a {@link ConfigSectionProvider} for all subsequent Config {@link WindowProvider}s.
 */
public abstract class ConfigProviderItem extends AbstractItem {
	private final GuiContext context;
	private final ConfigSectionProvider provider;
	private final ConfigNode node;
	private List<Runnable> clickHandlers;
	private List<Runnable> openHandlers;
	private List<Runnable> closeHandlers;
	private List<Consumer<InventoryClickEvent>> outsideClickHandlers;

	public ConfigProviderItem(GuiContext context, ConfigSectionProvider provider, ConfigNode node) {
		this.context = Objects.requireNonNull(context);
		this.provider = Objects.requireNonNull(provider);
		this.node = Objects.requireNonNull(node);
	}

	public @NotNull ConfigProviderItem setClickHandlers(@NotNull List<@NotNull Runnable> clickHandlers) {
		this.clickHandlers = clickHandlers;
		return this;
	}
	public @NotNull ConfigProviderItem addClickHandler(@NotNull Runnable clickHandler) {
		if (clickHandlers == null)
			clickHandlers = new ArrayList<>();

		clickHandlers.add(clickHandler);
		return this;
	}

	public @NotNull ConfigProviderItem setOpenHandlers(@NotNull List<@NotNull Runnable> openHandlers) {
		this.openHandlers = openHandlers;
		return this;
	}
	public @NotNull ConfigProviderItem addOpenHandler(@NotNull Runnable openHandler) {
		if (openHandlers == null)
			openHandlers = new ArrayList<>();

		openHandlers.add(openHandler);
		return this;
	}

	public @NotNull ConfigProviderItem setCloseHandlers(@NotNull List<@NotNull Runnable> closeHandlers) {
		this.closeHandlers = closeHandlers;
		return this;
	}
	public @NotNull ConfigProviderItem addCloseHandler(@NotNull Runnable closeHandler) {
		if (closeHandlers == null)
			closeHandlers = new ArrayList<>();

		closeHandlers.add(closeHandler);
		return this;
	}

	public @NotNull ConfigProviderItem setOutsideClickHandlers(@NotNull List<@NotNull Consumer<InventoryClickEvent>> outsideClickHandlers) {
		this.outsideClickHandlers = outsideClickHandlers;
		return this;
	}
	public @NotNull ConfigProviderItem addOutsideClickHandler(@NotNull Consumer<InventoryClickEvent> outsideClickHandler) {
		if (outsideClickHandlers == null)
			outsideClickHandlers = new ArrayList<>();

		outsideClickHandlers.add(outsideClickHandler);
		return this;
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		if (! context.checkAuthorizationNotify(player)) {
			WindowProvider.playDenySound(player);
			return;
		}

		Window window = node.configWindow(player, context.deeper(new ConfigSectionContext(provider)));
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
	public abstract ItemProvider getItemProvider(Player viewer);
}
