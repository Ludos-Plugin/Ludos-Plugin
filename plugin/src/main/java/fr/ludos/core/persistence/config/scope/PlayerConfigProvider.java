package fr.ludos.core.persistence.config.scope;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigNode} within a Player's config ({@link Ludos#getPlayerConfigSection}).
 */
public final class PlayerConfigProvider implements ConfigSectionProvider {
	private final Ludos ludos;
	public PlayerConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can set player configuration.");
			return null;
		}

		return ludos.getPlayerScopedConfig(player);
	}

	@Override
	public String getAccessError(CommandSender sender) {
		return null;
	}

	@Override
	public boolean saveConfig() {
		ludos.savePlayersConfig();
		return true;
	}

}
