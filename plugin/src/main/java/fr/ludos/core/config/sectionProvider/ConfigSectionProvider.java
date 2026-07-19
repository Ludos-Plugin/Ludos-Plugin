package fr.ludos.core.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Provider-type class to get and manage a {@link ConfigurationSection}, for use with {@link ConfigOptions}.
 */
public abstract class ConfigSectionProvider {
	public abstract ConfigurationSection getConfig(CommandSender sender);
	public abstract boolean saveConfig(ConfigurationSection config, CommandSender sender);
}
