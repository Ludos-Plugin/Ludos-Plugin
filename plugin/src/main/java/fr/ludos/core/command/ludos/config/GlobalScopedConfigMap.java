package fr.ludos.core.command.ludos.config;

import java.util.List;

import org.bukkit.Material;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.command.ludos.config.ludos.LudosConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Config Options Map for Globally scoped configuration.
 * This is used to limit the subsequent config nodes to Global configuration.
 */
public class GlobalScopedConfigMap extends ConfigNodeMap {
	public GlobalScopedConfigMap(Ludos ludos) {
		super(
			Component.text("Server-wide Configuration"), null,
			new ItemBuilder(Material.NETHER_STAR).addLoreLines(new AdventureComponentWrapper(Component.text("Configure options for the entire server.")
					.decoration(TextDecoration.ITALIC, false)
					.color(NamedTextColor.GRAY)
				)),
			List.of(
				LudosConfigMap.INSTANCE,
				GroupConfigMap.INSTANCE,
				ludos.getGameManager().configMap,
				ludos.getRoleManager().configMap,
				PlayerConfigMap.INSTANCE
			)
		);
	}
}
