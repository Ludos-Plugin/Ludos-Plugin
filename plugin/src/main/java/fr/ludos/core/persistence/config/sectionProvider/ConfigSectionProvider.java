package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Provider-type class to get and manage a {@link ConfigurationSection}, for use with {@link ConfigEntry}.
 */
public abstract class ConfigSectionProvider {
	public abstract ConfigurationSection getConfig(CommandSender sender);
	public abstract boolean saveConfig(ConfigurationSection config, CommandSender sender);
}
