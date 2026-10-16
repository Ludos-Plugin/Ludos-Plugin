package fr.ludos.core.security;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * Content access authorization Strategy, to prevent specific players from accessing content, beyond their authorization level.
 */
public interface AccessAuthorization {
	public @Nullable String getAccessError(CommandSender sender);
	public default boolean checkAuthorizationNotify(CommandSender sender) {
		String notification = getAccessError(sender);
		if (notification == null) return true;
		sender.sendMessage(notification);
		return false;
	}
	public default boolean checkAuthorizationSilent(CommandSender sender) {
		return getAccessError(sender) == null;
	}
}
