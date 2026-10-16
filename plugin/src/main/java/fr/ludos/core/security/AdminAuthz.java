package fr.ludos.core.security;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * {@link AccessAuthorization} implementation to authorize access to a Group's invite functionality.
 */
public class AdminAuthz implements AccessAuthorization {
	public AdminAuthz() { }

	@Override
	public @Nullable String getAccessError(CommandSender sender) {
		if (! sender.isOp()) {
			return "Only Server Operators are allowed to globally configure Ludos.";
		}

		return null;
	}
}