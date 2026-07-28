package fr.ludos.core.command.ludos.config.ludos;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Config Options Map for Plugin configuration.
 */
public final class LudosConfigMap extends ConfigNodeMap {
	public static final LudosConfigMap INSTANCE = new LudosConfigMap();

	private LudosConfigMap() {
		super(
			Component.text("Ludos Configuration"),
			Ludos.NAMESPACE,
			List.of()
		);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.NETHER_STAR);
	}
}