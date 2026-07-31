package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.security.AccessAuthorization;

/**
 * Provider-type interface to get and manage a {@link ConfigurationSection}, for use with {@link ConfigNode}.
 */
public interface ConfigSectionProvider extends AccessAuthorization {
	public ConfigurationSection getConfig(CommandSender sender);
	public boolean saveConfig();
}
