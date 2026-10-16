package fr.ludos.core.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.valueEntry.ConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.GroupPlayersConfigEntry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * A static Utility class containing factory methods for various generic Game-specific {@link ConfigEntry}s / {@link ConfigNode}s.
 */
public class GameConfigs {
	private GameConfigs() { }

	public static final GroupPlayersConfigEntry createConfigForTeam(GroupManager groupManager, int teamNumber) {
		return new GroupPlayersConfigEntry(
			groupManager,
			Component.text("Team " + teamNumber + " players"),
			"team_" + teamNumber, "random"
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.BLUE_BANNER);
			}
		};
	}
}
