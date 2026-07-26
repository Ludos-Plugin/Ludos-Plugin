package fr.ludos.core.persistence.config.valueEntry;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.BorderItem;
import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.gui.configValue.SubmitValueItem;
import fr.ludos.core.gui.configValue.display.BooleanDisplayItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.BooleanSerializer;
import fr.ludos.core.persistence.serializer.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigEntry} for {@link Boolean}s.
 */
public class BooleanConfigEntry extends ConfigEntry<Boolean, Boolean> {
	public static final String TRUE_STRING = "True";
	public static final String FALSE_STRING = "False";
	private final boolean defaultValue;

	private final String trueString;
	private final String falseString;

	public BooleanConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, Boolean defaultValue, String trueName, String falseName) {
		super(name, displayItem, key);
		this.defaultValue = defaultValue;
		this.trueString = trueName == null
			? TRUE_STRING
			: trueName;
		this.falseString = falseName == null
			? FALSE_STRING
			: falseName;
	}
	public BooleanConfigEntry(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @NotNull String key, Boolean defaultValue) {
		this(name, displayItem, key, defaultValue, null, null);
	}

	@Override
	public final Serializer<Boolean, Boolean> getSerializer() {
		return BooleanSerializer.INSTANCE;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return Set.of(BooleanSerializer.FALSE_STRING, BooleanSerializer.TRUE_STRING);
	}
	@Override
	public Boolean defaultValue() {
		return defaultValue;
	}

	public static final ItemBuilder TOGGLE_ITEM = new ItemBuilder(Material.LEVER)
		.setDisplayName(new AdventureComponentWrapper(
			Component.text("Toggle")
				.decoration(TextDecoration.ITALIC, false)
				.color(NamedTextColor.GRAY)
		));

	@Override
	public Window configWindow(Player player, ConfigSectionContext context) {
		ConfigurationSection config = context.getConfig(player);

		BooleanDisplayItem currentValue = new BooleanDisplayItem(this, context, trueString, falseString);
		SubmitValueItem<Boolean> toggleItem = new SubmitValueItem<Boolean>(this, context) {
			public ItemProvider getItemProvider(Gui gui) {
				return TOGGLE_ITEM;
			};
		}.setValue(() -> ! getValueOrDefault(config)).addSubmitHandler(e -> currentValue.notifyWindows());

		SubmitValueItem<Boolean> trueItem = new SubmitValueItem<Boolean>(this, context) {
			public ItemProvider getItemProvider(Gui gui) { return currentValue.trueItem; };
		}.setValue(true).addSubmitHandler(e -> currentValue.notifyWindows());

		SubmitValueItem<Boolean> falseItem = new SubmitValueItem<Boolean>(this, context) {
			public ItemProvider getItemProvider(Gui gui) { return currentValue.falseItem; };
		}.setValue(false).addSubmitHandler(e -> currentValue.notifyWindows());

		ResetValueItem<Boolean, Gui> reset = new ResetValueItem<>(this, context);

		return Window.single()
			.setTitle(new AdventureComponentWrapper(displayName()))
			.setGui(Gui.normal()
				.setStructure(new Structure(
					"# V #",
					"t # R",
						"T # F"
					)
					.addIngredient('#', BorderItem.INSTANCE)
					.addIngredient('V', currentValue)
					.addIngredient('T', trueItem)
					.addIngredient('F', falseItem)
					.addIngredient('R', reset)
					.addIngredient('t', toggleItem)
				)
			)
			.addCloseHandler(() -> context.openPreviousWindow(player))
			.build(player);
	}
}
