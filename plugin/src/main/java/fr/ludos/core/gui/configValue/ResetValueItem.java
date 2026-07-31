package fr.ludos.core.gui.configValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.WindowProvider;
import fr.ludos.core.persistence.PersistentAccessor;
import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.gui.SlotElement.ItemSlotElement;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * Resets a {@link PersistentEntry} to its default value, in the given {@link ConfigSectionContext}.
 * @param <T> The type of the data of the entry
 * @param <G> The type of Gui
 */
public class ResetValueItem<T, G extends Gui> extends ControlItem<G> {
	private final PersistentAccessor<T> entry;

	private List<Runnable> resetHandlers;

	public ResetValueItem(PersistentAccessor<T> entry) {
		this.entry = Objects.requireNonNull(entry);
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		entry.unset();
		WindowProvider.playClickSound(player);
		entry.save();


		for (SlotElement element : getGui().getSlotElements()) {
			if (element instanceof ItemSlotElement item) {
				item.getItem().notifyWindows();
			}
		}
		if (resetHandlers != null) {
			for (Runnable handler : resetHandlers) {
				handler.run();
			}
		}
	}

	public @NotNull ResetValueItem<T, G> setResetHandlers(@NotNull List<@NotNull Runnable> resetHandlers) {
		this.resetHandlers = resetHandlers;
		return this;
	}

	public @NotNull ResetValueItem<T, G> addResetHandler(@NotNull Runnable resetHandler) {
		if (resetHandlers == null)
			resetHandlers = new ArrayList<>();

		resetHandlers.add(resetHandler);
		return this;
	}

	@Override
	public ItemProvider getItemProvider(Gui gui) {
		return new ItemBuilder(Material.BARRIER)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Reset")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.DARK_RED)
			));
	}
}
