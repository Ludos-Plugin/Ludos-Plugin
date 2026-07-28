package fr.ludos.core.persistence.config.scope;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * {@link ConfigNodeMap} for {@link Group}-scoped configuration.
 * This is used to limit the subsequent {@link ConfigNode} to the scope of a single {@link Group}.
 */
public class GroupScopedConfigMap extends ConfigNodeMap {
	public GroupScopedConfigMap(Ludos ludos) {
		super(
			Component.text("Group-wide Configuration"), null,
			List.of(
				GroupConfigMap.INSTANCE,
				ludos.getGameManager().configMap,
				ludos.getRoleManager().configMap,
				PlayerConfigMap.INSTANCE
			)
		);
	}
	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.BLUE_BANNER)
			.addLoreLines(new AdventureComponentWrapper(Component.text("Configure options for your group.")
				.decoration(TextDecoration.ITALIC, false)
				.color(NamedTextColor.GRAY)
			));
	}
}
