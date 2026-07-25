package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.Utility;
import fr.ludos.core.persistence.config.ConfigNodeOperation;

/**
 * .
 */
public class ConfigSectionContext implements ConfigSectionProvider {
	private final ConfigSectionProvider provider;
	private final String path;

	private ConfigSectionContext(ConfigSectionProvider provider, String path) {
		this.provider = provider;
		this.path = path;
	}
	public ConfigSectionContext(ConfigSectionProvider provider) {
		this(provider, null);
	}

	public ConfigSectionContext getDeeper(String path) {
		if (this.path == null) {
			return new ConfigSectionContext(provider, path);
		}
		return new ConfigSectionContext(provider, this.path + '.' + path);
	}


	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		return Utility.getOrCreateConfigSection(provider.getConfig(sender), path);
	}

	@Override
	public boolean isAuthorized(CommandSender sender, ConfigNodeOperation op) {
		return provider.isAuthorized(sender, op);
	}

	@Override
	public boolean saveConfig() {
		return provider.saveConfig();
	}
}