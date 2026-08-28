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
 * Config Root Map for Player-scoped configuration.<br>
 * This is used to limit the subsequent config nodes to the scope of a single Player.
 */
public class PlayerScopedConfigMap extends ConfigNodeMap {
	public PlayerScopedConfigMap(Ludos ludos) {
		super(
			Component.text("Player-specific Configuration"), null,
			List.of(
				ludos.getRoleManager().configMap,
				ludos.playerConfigMap
			)
		);
	}
	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.PLAYER_HEAD)
			.addLoreLines(new AdventureComponentWrapper(Component.text("Configure options for yourself.")
				.decoration(TextDecoration.ITALIC, false)
				.color(NamedTextColor.GRAY)
			));
	}
}
