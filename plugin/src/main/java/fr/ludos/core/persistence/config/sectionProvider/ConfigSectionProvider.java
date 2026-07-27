package fr.ludos.core.persistence.config.sectionProvider;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.persistence.config.ConfigNode;

/**
 * Provider-type interface to get and manage a {@link ConfigurationSection}, for use with {@link ConfigNode}.
 */
public interface ConfigSectionProvider {
	public ConfigurationSection getConfig(CommandSender sender);
	public @Nullable String getValidationError(CommandSender sender);
	public default boolean checkAuthorizationNotify(CommandSender sender) {
		String notification = getValidationError(sender);
		if (notification == null) return true;
		sender.sendMessage(notification);
		return false;
	}
	public default boolean checkAuthorizationSilent(CommandSender sender) {
		return getValidationError(sender) == null;
	}
	public boolean saveConfig();
}
