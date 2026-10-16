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
 * The kinds of Authorisation that a {@link Group}'s members have.
 */
public enum GroupRightsOption {
	none,
	invite {
		@Override
		public boolean canInvite() {
			return true;
		}
	},
	game {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
	},
	config {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
		@Override
		public boolean canConfig() {
			return true;
		}
	},
	all {
		@Override
		public boolean canInvite() {
			return true;
		}
		@Override
		public boolean canRunGames() {
			return true;
		}
		@Override
		public boolean canConfig() {
			return true;
		}
		@Override
		public boolean canManage() {
			return true;
		}
	};

	public boolean canInvite() {
		return false;
	}
	public boolean canRunGames() {
		return false;
	}
	public boolean canConfig() {
		return false;
	}
	public boolean canManage() {
		return false;
	}

	public static List<String> getOptions() {
		return Arrays.stream(GroupRightsOption.values())
			.map(GroupRightsOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append('<');
		sb.append(
			Arrays.stream(GroupRightsOption.values()).map(GroupRightsOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append('>');

		return sb.toString();
	}

	public ItemBuilder getDisplayItem() {
		switch (this) {
			case none:
				return new ItemBuilder(Material.BARRIER)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("None")
							.color(NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members have no authorisation")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("besides leaving the group.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case invite:
				return new ItemBuilder(Material.WRITABLE_BOOK)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Invite")
							.color(NamedTextColor.BLUE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members can invite other")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("players to the group.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case game:
				return new ItemBuilder(Material.FIREWORK_ROCKET)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Start Games")
							.color(NamedTextColor.GREEN)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members can invite players")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("and start games.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case config:
				return new ItemBuilder(Material.COMPARATOR)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("Configure")
							.color(NamedTextColor.GOLD)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members can")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("invite players, start games")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("and configure the group.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			case all:
				return new ItemBuilder(Material.NETHER_STAR)
					.setDisplayName(new AdventureComponentWrapper(
						Component.text("All")
							.color(NamedTextColor.LIGHT_PURPLE)
							.decoration(TextDecoration.ITALIC, false)
					))
					.addLoreLines(
						new AdventureComponentWrapper(
							Component.text("Members have all authorisations")
								.decoration(TextDecoration.ITALIC, false)
						),
						new AdventureComponentWrapper(
							Component.text("same as the group leader.")
								.decoration(TextDecoration.ITALIC, false)
						)
					);
			default:
				return new ItemBuilder(Material.AIR);
		}
	}
}
