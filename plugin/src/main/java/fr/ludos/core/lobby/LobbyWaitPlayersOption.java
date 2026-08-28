package fr.ludos.core.lobby;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import fr.ludos.core.Utility;
import fr.ludos.core.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Which of the Group's player's to wait for, when in a Game's lobby.
 */
public enum LobbyWaitPlayersOption {
	online () {
		@Override
		public final Set<OfflinePlayer> getPlayers(Group group) {
			return Utility.getOnline(group.getPlayers().stream())
				.collect(Collectors.toSet());
		}
	}, all () {
		@Override
		public final Set<OfflinePlayer> getPlayers(Group group) {
			return group.getPlayers().stream()
				.collect(Collectors.toSet());
		}
	};

	public abstract Set<OfflinePlayer> getPlayers(Group group);

	public static List<String> getOptions() {
		return Arrays.stream(LobbyWaitPlayersOption.values())
			.map(LobbyWaitPlayersOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(
			Arrays.stream(LobbyWaitPlayersOption.values()).map(LobbyWaitPlayersOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append('>');

		return sb.toString();
	}

	public ItemBuilder getDisplayItem() {
		switch (this) {
			case online:
				return new ItemBuilder(Material.CLOCK)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Online members")
							.color(NamedTextColor.GREEN)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Only wait for already")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("online members to join.")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("Will start immediately.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case all:
				return new ItemBuilder(Material.ENDER_PEARL)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("All members")
							.color(NamedTextColor.BLUE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Wait for all members")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("to log in and join.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			default:
				return new ItemBuilder(Material.AIR);
		}
	}
}