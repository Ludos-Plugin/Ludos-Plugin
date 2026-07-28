package fr.ludos.core.persistence.config.valueEntry;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.configValue.SubmitValueItem;
import fr.ludos.core.gui.configValue.display.IntegerDisplayItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.IntegerSerializer;
import fr.ludos.core.persistence.serializer.Serializer;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.window.AnvilWindow;

/**
 * {@link ConfigEntry} for {@link Number}s.
 */
public abstract class IntegerConfigEntry extends ConfigEntry<Integer, Integer> {
	private final static Set<String> NUMBERS = new HashSet<>() {{add("1"); add("2"); add("3");}};
	private final IntegerSerializer serializer;
	private final @Nullable Set<@NotNull String> suggestions;
	private final @Nullable Integer defaultValue;

	public IntegerConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions, boolean unsigned) {
		super(name, key);
		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.serializer = unsigned ? IntegerSerializer.UNSIGNED : IntegerSerializer.SIGNED;
		this.suggestions = suggestions != null
			? suggestions.stream().map(i -> i.toString()).collect(Collectors.toSet())
			: null;
	}
	public IntegerConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Integer defaultValue, @Nullable Set<@NotNull Integer> suggestions) {
		this(name, key, defaultValue, suggestions, false);
	}
	public IntegerConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Integer defaultValue, boolean unsigned) {
		this(name, key, defaultValue, null, unsigned);
	}
	public IntegerConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Integer defaultValue) {
		this(name, key, defaultValue, null);
	}
	@Override
	public final Serializer<Integer, Integer> getSerializer() {
		return serializer;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return suggestions != null
			? suggestions
			: NUMBERS;
	}
	@Override
	public @Nullable Integer defaultValue() {
		return defaultValue;
	}

	@Override
	public AnvilWindow configWindow(Player player, ConfigSectionContext context) {
		IntegerDisplayItem currentValue = new IntegerDisplayItem(this, context);
		ResetValueItem<Integer, Gui> reset = new ResetValueItem<>(this, context).addResetHandler(() -> currentValue.notifyWindows());
		SubmitValueItem<Integer> submit = new SubmitValueItem<>(this, context).addSubmitHandler(e -> currentValue.notifyWindows());

		return AnvilWindow.single()
			.setTitle(new AdventureComponentWrapper(displayName()))
			.addRenameHandler(str -> {
				submit.setValue(getSerializer().fromString(str));
			})
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
