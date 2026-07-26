package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.configValue.SubmitValueItem;
import fr.ludos.core.gui.configValue.display.StringDisplayItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSerializer;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.window.AnvilWindow;

/**
 * {@link ConfigEntry} for {@link String}s.
 */
public class StringConfigEntry extends ConfigEntry<String, String> {
	private final @NotNull Set<@NotNull String> suggestions;
	private final @Nullable String defaultValue;

	public StringConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, @Nullable Set<@NotNull String> suggestions, String defaultValue) {
		super(name, displayItem, key);
		this.suggestions = suggestions == null
			? Collections.emptySet()
			: suggestions;
		this.defaultValue = defaultValue;
	}
	public StringConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, @Nullable Set<@NotNull String> suggestions) {
		this(name, displayItem, key, suggestions, null);
	}
	public StringConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, String defaultValue) {
		this(name, displayItem, key, null, defaultValue);
	}

	@Override
	public final Serializer<String, String> getSerializer() {
		return StringSerializer.INSTANCE;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return suggestions;
	}
	@Override
	public String defaultValue() {
		return defaultValue;
	}

	@Override
	public AnvilWindow configWindow(Player player, ConfigSectionContext context) {
		StringDisplayItem currentValue = new StringDisplayItem(this, context);
		ResetValueItem<String, Gui> reset = new ResetValueItem<>(this, context).addResetHandler(() -> currentValue.notifyWindows());
		SubmitValueItem<String> submit = new SubmitValueItem<>(this, context).addSubmitHandler(e -> currentValue.notifyWindows());

		return AnvilWindow.single()
			.setTitle(new AdventureComponentWrapper(displayName()))
			.addRenameHandler(submit::setValue)
			.setGui(Gui.normal()
				.setStructure(new Structure("# X V")
					.addIngredient('#', currentValue)
					.addIngredient('X', reset)
					.addIngredient('V', submit)
				)
			)
			.addCloseHandler(() -> context.openPreviousWindow(player))
			.build(player);
	}
}
