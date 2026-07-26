package fr.ludos.core.command.ludos.config.player;

import java.util.List;

import org.bukkit.Material;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.BooleanConfigEntry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Config Options Map for Player-specific configuration.
 */
public final class PlayerConfigMap extends ConfigNodeMap {
	public static final BooleanConfigEntry GUIDEBOOK_MESSAGE =
		new BooleanConfigEntry(
			Component.text("Show login guidebook message"),
			new ItemBuilder(Material.WRITTEN_BOOK),
			"guidebook_message", true,
			"Enabled", "Disabled"
		);

	public static final PlayerConfigMap INSTANCE = new PlayerConfigMap();

	private PlayerConfigMap() {
		super(
			Component.text("Player Configuration"),
			new ItemBuilder(Material.PLAYER_HEAD),
			Ludos.PLAYER_NAMESPACE,
			List.of(
				GUIDEBOOK_MESSAGE
			)
		);
	}
}