package fr.ludos.core.game.teamController;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Material;

import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Behaviour in the event that a Player joins a {@link Group} when a {@link Game} is in progress.
 */
public enum GameJoinOption {
	yes {},
	spectator {},
	no {};

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(
			Arrays.stream(GameJoinOption.values()).map(GameJoinOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append('>');

		return sb.toString();
	}

	public ItemBuilder getDisplayItem() {
		switch (this) {
			case yes:
				return new ItemBuilder(Material.REDSTONE_TORCH)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Yes")
							.color(NamedTextColor.GREEN)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(new AdventureComponentWrapper(
						Component.text("Members can join ongoing games.")
							.decoration(TextDecoration.ITALIC, false)
					));
			case spectator:
				return new ItemBuilder(Material.WRITABLE_BOOK)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Spectator")
							.color(NamedTextColor.BLUE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("New members will join")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("ongoing games as spectator.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case no:
				return new ItemBuilder(Material.BARRIER)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("No")
							.color(NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("New members cannot join")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("a game in progress.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			default:
				return new ItemBuilder(Material.AIR);
		}
	}
}
