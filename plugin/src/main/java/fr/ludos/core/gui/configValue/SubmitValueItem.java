package fr.ludos.core.gui.configValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.ControlItem;

/**
 * Submits a new value to a {@link PersistentEntry} in the given {@link ConfigSectionContext}.
 * @param <T> The type of the data of the entry
 */
public class SubmitValueItem<T> extends ControlItem<Gui> {
	private final PersistentAccessor<T> entry;

	private List<Consumer<T>> submitHandlers;

	private T value;
	private Supplier<T> valueProvider;

	public SubmitValueItem(PersistentAccessor<T> entry) {
		this.entry = Objects.requireNonNull(entry);
	}

	public SubmitValueItem<T> setValue(T value) {
		this.value = value;
		return this;
	}
	public SubmitValueItem<T> setValue(Supplier<T> valueProvider) {
		this.valueProvider = Objects.requireNonNull(valueProvider);
		this.value = null;
		return this;
	}

	public T getValue() {
		if (valueProvider != null) return valueProvider.get();
		return value;
	}

	public @NotNull SubmitValueItem<T> setSubmitHandlers(@NotNull List<@NotNull Consumer<T>> submitHandlers) {
		this.submitHandlers = submitHandlers;
		return this;
	}

	public @NotNull SubmitValueItem<T> addSubmitHandler(@NotNull Consumer<T> submitHandler) {
		if (submitHandlers == null)
			submitHandlers = new ArrayList<>();

		submitHandlers.add(submitHandler);
		return this;
	}

	@Override
	public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
		final T newValue = getValue();
		if (newValue == null) {
			WindowProvider.playDenySound(player);
			return;
		}

		entry.set(newValue);
		WindowProvider.playClickSound(player);
		entry.save();

		notifyWindows();
		for (Consumer<T> handler : submitHandlers) {
			handler.accept(newValue);
		}
	}

	@Override
	public ItemProvider getItemProvider(Gui gui) {
		return new ItemBuilder(Material.PAPER)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text("Submit")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GOLD)
			));
	}
}
