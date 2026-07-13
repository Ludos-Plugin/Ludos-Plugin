package fr.ludos.core.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public abstract class ConfigSectionProvider {
	public abstract ConfigurationSection getConfig(CommandSender sender);
	public abstract boolean saveConfig(ConfigurationSection config, CommandSender sender);
}
