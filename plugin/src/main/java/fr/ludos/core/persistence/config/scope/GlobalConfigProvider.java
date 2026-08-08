package fr.ludos.core.persistence.config.scope;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;
import fr.ludos.core.security.AccessAuthorization;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigNode} within the Global Ludos config ({@link Ludos#getConfig}).
 */
public final class GlobalConfigProvider implements ConfigSectionProvider {
	private final Ludos ludos;

	public GlobalConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		return ludos.getConfig();
	}

	@Override
	public boolean saveConfig() {
		ludos.saveConfig();
		return true;
	}

	@Override
	public AccessAuthorization getAccessAuthorization() {
		return ludos.getAdminAuthz();
	}
}