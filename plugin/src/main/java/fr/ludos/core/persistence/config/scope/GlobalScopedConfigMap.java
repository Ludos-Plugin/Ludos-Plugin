package fr.ludos.core.persistence.config.scope;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Config Options Map for Globally scoped configuration.
 * This is used to limit the subsequent config nodes to Global configuration.
 */
public class GlobalScopedConfigMap extends ConfigNodeMap {
	public GlobalScopedConfigMap(Ludos ludos) {
		super(
			Component.text("Server-wide configuration"), null,
			List.of(
				ludos.configMap,
				ludos.getGroupManager().configMap,
				ludos.getGameManager().configMap,
				ludos.getRoleManager().configMap,
				ludos.playerConfigMap
			)
		);
	}
	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.NETHER_STAR)
			.addLoreLines(new AdventureComponentWrapper(Component.text("Configure options for the entire server.")
				.decoration(TextDecoration.ITALIC, false)
				.color(NamedTextColor.GRAY)
			));
	}
}
