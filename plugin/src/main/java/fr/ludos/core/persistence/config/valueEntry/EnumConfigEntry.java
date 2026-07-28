package fr.ludos.core.persistence.config.valueEntry;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.BorderItem;
import fr.ludos.core.gui.ChangePageItem;
import fr.ludos.core.gui.SinglePickerItem;
import fr.ludos.core.gui.configValue.ResetValueItem;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.persistence.serializer.EnumSerializer;
import fr.ludos.core.persistence.serializer.Serializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

/**
 * {@link ConfigEntry} for {@link Enum} values.
 * @param <T> The Enum type of the values
 */
public abstract class EnumConfigEntry<T extends Enum<T>> extends ConfigEntry<T, String> {
	private final @NotNull Class<T> clazz;
	private final EnumSerializer<T> serializer;
	private final @Nullable T defaultValue;

	public EnumConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Class<T> clazz, @Nullable T defaultValue) {
		super(name, key);
		this.clazz = Objects.requireNonNull(clazz);
		this.serializer = Objects.requireNonNull(new EnumSerializer<>(clazz));
		this.defaultValue = defaultValue;
	}
	public EnumConfigEntry(@NotNull TextComponent name, @NotNull String key, @NotNull Class<T> clazz) {
		this(name, key, clazz, null);
	}

	@Override
	public final Serializer<T, String> getSerializer() {
		return serializer;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender player) {
		return Arrays.stream(clazz.getEnumConstants())
			.map(Enum::name)
			.collect(Collectors.toSet());
	}
	@Override
	public T defaultValue() {
		return defaultValue != null
			? defaultValue
			: clazz.getEnumConstants()[0];
	}


	@Override
	public Window configWindow(Player player, ConfigSectionContext context) {
		ConfigurationSection config = context.getConfig(player);

		PagedGui.Builder<Item> gui = PagedGui.items()
			.setStructure(new Structure(
				"# # # # # # # # #",
				"# x x x x x x x #",
				"# x x x x x x x #",
				"# # # # P # # # R")
			)
			.addIngredient('#', BorderItem.INSTANCE)
			.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('P', ChangePageItem.INSTANCE)
			.addIngredient('R', new ResetValueItem<>(this, context));

		List<Item> items = Arrays.stream(clazz.getEnumConstants())
			.map(value ->
				new SinglePickerItem<T, Item>(value, this, context) {
					@Override
					public ItemProvider getItemProvider(PagedGui<Item> gui) {
						ItemBuilder item = getItemForEnumValue(value);
						T currentValue = getValueOrDefault(config);

						return currentValue == value
							? item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
								.addEnchantment(Enchantment.CHANNELING, 1, false)
								.addLoreLines(new AdventureComponentWrapper(
									Component.text("Currently selected")
										.decoration(TextDecoration.ITALIC, false)
										.color(NamedTextColor.GRAY)
								))
							: item;
					}
				}
			)
			.collect(Collectors.toList());

		return Window.single()
			.setTitle(new AdventureComponentWrapper(displayName()))
			.setGui(gui.setContent(items))
			.addCloseHandler(() -> context.openPreviousWindow(player))
			.build(player);
	}

	public abstract ItemBuilder getItemForEnumValue(T value);
}
