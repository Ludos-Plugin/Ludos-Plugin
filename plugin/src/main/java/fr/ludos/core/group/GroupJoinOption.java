package fr.ludos.core.group;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Behaviour for a Player attempting to join a {@link Group}.
 */
public enum GroupJoinOption {
	auto_accept {},
	manual_accept {};

	public static List<String> getOptions() {
		return Arrays.stream(GroupJoinOption.values())
			.map(GroupJoinOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(
			Arrays.stream(GroupJoinOption.values()).map(GroupJoinOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append('>');

		return sb.toString();
	}

	public ItemBuilder getDisplayItem() {
		switch (this) {
			case auto_accept:
				return new ItemBuilder(Material.REDSTONE_TORCH)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Auto-accept")
							.color(NamedTextColor.GREEN)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members can join the group")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("automatically.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case manual_accept:
				return new ItemBuilder(Material.WRITABLE_BOOK)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Manual accept")
							.color(NamedTextColor.BLUE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("The leader has to manually")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("accept requests to join the group.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			default:
				return new ItemBuilder(Material.AIR);
		}
	}
}
