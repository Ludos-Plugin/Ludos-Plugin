package fr.ludos.core.persistence.config.valueEntry;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.configValue.SubmitValueItem;
import fr.ludos.core.gui.configValue.display.StringDisplayItem;
import fr.ludos.core.persistence.PersistentAccessor;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.Serializer;
import fr.ludos.core.persistence.serializer.StringSerializer;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.window.AnvilWindow;

/**
 * {@link ConfigEntry} for {@link String}s.
 */
public abstract class StringConfigEntry extends ConfigEntry<String, String> {
	private final @NotNull Set<@NotNull String> suggestions;
	private final @Nullable String defaultValue;

	public StringConfigEntry(@NotNull TextComponent name, @NotNull String key, @Nullable Set<@NotNull String> suggestions, String defaultValue) {
		super(name, key);
		this.suggestions = suggestions == null
			? Collections.emptySet()
			: suggestions;
		this.defaultValue = defaultValue;
	}
	public StringConfigEntry(@NotNull TextComponent name, @NotNull String key, @Nullable Set<@NotNull String> suggestions) {
		this(name, key, suggestions, null);
	}
	public StringConfigEntry(@NotNull TextComponent name, @NotNull String key, String defaultValue) {
		this(name, key, null, defaultValue);
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
	public AnvilWindow configWindow(Player player, GuiContext context) {
		ConfigSectionContext configContext = context.configContext();
		if (configContext == null) return null;

		PersistentAccessor<String> accessor = asAccessor(configContext, player);

		StringDisplayItem currentValue = new StringDisplayItem(accessor);
		ResetValueItem<String, Gui> reset = new ResetValueItem<>(accessor).addResetHandler(() -> currentValue.notifyWindows());
		SubmitValueItem<String> submit = new SubmitValueItem<>(accessor).addSubmitHandler(e -> currentValue.notifyWindows());

		return AnvilWindow.single()
			.setTitle(new AdventureComponentWrapper(normalizedDisplayName()))
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
