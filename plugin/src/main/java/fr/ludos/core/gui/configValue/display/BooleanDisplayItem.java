package fr.ludos.core.gui.configValue.display;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.persistence.PersistentAccessor;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Display a boolean value as a {@link DisplayValueItem}.
 */
public class BooleanDisplayItem extends DisplayValueItem<Boolean> {
	public static final ItemBuilder createTrueItem(String name) {
		return new ItemBuilder(Material.EMERALD)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text(name)
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GREEN)
			));
	}
	public static final ItemBuilder createFalseItem(String name) {
		return new ItemBuilder(Material.REDSTONE)
			.setDisplayName(new AdventureComponentWrapper(
				Component.text(name)
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.RED)
			));
	}

	public static final String TRUE_STRING = "True";
	public static final ItemBuilder TRUE_ITEM = createTrueItem(TRUE_STRING);

	public static final String FALSE_STRING = "False";
	public static final ItemBuilder FALSE_ITEM = createFalseItem(FALSE_STRING);

	public final ItemBuilder trueItem;
	public final ItemBuilder falseItem;
	private final ItemBuilder thisTrueItem;
	private final ItemBuilder thisFalseItem;

	public BooleanDisplayItem(PersistentAccessor<Boolean> entry, String trueName, String falseName) {
		super(entry);
		this.trueItem = trueName == null
			? TRUE_ITEM
			: createTrueItem(trueName);
		this.falseItem = falseName == null
			? FALSE_ITEM
			: createFalseItem(falseName);

		thisTrueItem = createTrueItem(trueName == null
			? TRUE_STRING
			: trueName
		).addLoreLines(new AdventureComponentWrapper(
			Component.text("Current value")
				.color(NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false)
		));

		thisFalseItem = createFalseItem(falseName == null
			? FALSE_STRING
			: falseName
		).addLoreLines(new AdventureComponentWrapper(
			Component.text("Current value")
				.color(NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false)
		));
	}
	public BooleanDisplayItem(PersistentAccessor<Boolean> entry, ConfigSectionContext context) {
		this(entry, null, null);
	}

	@Override
	public ItemProvider getItemProvider(Boolean value, Player player) {
		return value ? thisTrueItem : thisFalseItem;
	}

}
