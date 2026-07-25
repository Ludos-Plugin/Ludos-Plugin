package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;

/**
 * Provider-type interface to get and manage a {@link ConfigurationSection}, for use with {@link ConfigNode}.
 */
public interface ConfigSectionProvider {
	public ConfigurationSection getConfig(CommandSender sender);
	public boolean isAuthorized(CommandSender sender, ConfigNodeOperation op);
	public boolean saveConfig();
}
