package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import fr.ludos.core.Utility;
import fr.ludos.core.persistence.config.ConfigNodeOperation;

/**
 * .
 */
public class ConfigSectionContext implements ConfigSectionProvider {
	private final ConfigSectionProvider provider;
	private final Plugin plugin;
	private final String path;

	private ConfigSectionContext(ConfigSectionProvider provider, Plugin plugin, String path) {
		this.provider = provider;
		this.plugin = plugin;
		this.path = path;
	}
	public ConfigSectionContext(ConfigSectionProvider provider, Plugin plugin) {
		this(provider, plugin, null);
	}

	public ConfigSectionContext getDeeper(String path) {
		if (this.path == null) {
			return new ConfigSectionContext(provider, plugin, path);
		}
		return new ConfigSectionContext(provider, plugin, this.path + '.' + path);
	}

	public Plugin plugin() {
		return plugin;
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