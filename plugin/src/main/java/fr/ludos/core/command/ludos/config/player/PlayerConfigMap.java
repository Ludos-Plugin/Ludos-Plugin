package fr.ludos.core.command.ludos.config.player;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.BooleanConfigEntry;
import net.kyori.adventure.text.Component;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Config Options Map for Player-specific configuration.
 */
public final class PlayerConfigMap extends ConfigNodeMap {
	public static final BooleanConfigEntry GUIDEBOOK_MESSAGE =
		new BooleanConfigEntry(
			Component.text("Show login guidebook message"),
			"guidebook_message", true,
			"Enabled", "Disabled"
		) {
			@Override
			public AbstractItemBuilder<?> createItem(Player player) {
				return new ItemBuilder(Material.WRITTEN_BOOK);
			}
		};

	public static final PlayerConfigMap INSTANCE = new PlayerConfigMap();

	private PlayerConfigMap() {
		super(
			Component.text("Player Configuration"),
			Ludos.PLAYER_NAMESPACE,
			List.of(
				GUIDEBOOK_MESSAGE
			)
		);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return new ItemBuilder(Material.PLAYER_HEAD);
	}
}