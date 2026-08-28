package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Utility;
import fr.ludos.core.security.AccessAuthorization;

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

	public ConfigSectionContext getDeeper(@Nullable String path) {
		String finalPath = path == null
			? this.path
			: this.path == null
				? path
				: this.path + '.' + path;
		return new ConfigSectionContext(provider, finalPath);
	}


	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		return Utility.getOrCreateConfigSection(provider.getConfig(sender), path);
	}

	@Override
	public boolean saveConfig() {
		return provider.saveConfig();
	}

	@Override
	public AccessAuthorization getAccessAuthorization() {
		return provider.getAccessAuthorization();
	}
}