package fr.ludos.games.arena;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.persistence.config.valueEntry.EnumConfigEntry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Enum representing the different {@link ArenaGame} mode options.
 */
public enum ArenaModeOption {
	duel("Duel", "1v1 rounds"),
	multi("Multi", "team rounds");

	public static final EnumConfigEntry<ArenaModeOption> CONFIG =
		new EnumConfigEntry<>(
			Component.text("Arena Mode"),
			"arena_mode",
			ArenaModeOption.class
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.WHITE_BANNER);
			}
			@Override
			public ItemBuilder getItemForEnumValue(ArenaModeOption value) {
				return value.getDisplayItem();
			}
		};

	private final String displayName;
	private final String description;

	ArenaModeOption(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public static Optional<ArenaModeOption> resolve(String value) {
		if (value == null || value.isBlank()) return Optional.empty();

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(ArenaModeOption.values())
			.filter(option -> option.name().equalsIgnoreCase(normalized) || option.name().equalsIgnoreCase(normalized))
			.findFirst();
	}

	public static ArenaModeOption fromConfig(String value, ArenaModeOption fallback) {
		return resolve(value).orElse(fallback);
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(
			Arrays.stream(ArenaModeOption.values())
			.map(ArenaModeOption::name)
			.collect(Collectors.joining(" | "))
		);
		sb.append('>');

		return sb.toString();
	}

	public static final List<String> OPTIONS = Arrays.stream(ArenaModeOption.values())
		.map(Enum::name)
		.collect(Collectors.toList());


	public ItemBuilder getDisplayItem() {
		switch (this) {
			case duel:
				return new ItemBuilder(Material.IRON_SWORD)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Duel")
							.color(NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(new AdventureComponentWrapper(
						Component.text("Two players fight each other.")
							.decoration(TextDecoration.ITALIC, false)
					));
			case multi:
				return new ItemBuilder(Material.BLUE_BANNER)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Team vs Team")
							.color(NamedTextColor.BLUE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Two teams fight each other.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			default:
				return new ItemBuilder(Material.AIR);
		}
	}
}
