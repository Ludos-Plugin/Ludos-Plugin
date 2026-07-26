package fr.ludos.core.command.ludos.config;

import java.util.List;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.persistence.config.ConfigRootMap;
import net.kyori.adventure.text.Component;

/**
 * Config Root Map for Player-scoped configuration.<br>
 * This is used to limit the subsequent config nodes to the scope of a single Player.
 */
public class PlayerScopedConfigMap extends ConfigRootMap {
	public PlayerScopedConfigMap(Ludos ludos) {
		super(List.of(
			ludos.getRoleManager().configMap,
			PlayerConfigMap.INSTANCE
		));
	}

	@Override
	public Component name() {
		return Component.text("Global Configuration");
	}
}
