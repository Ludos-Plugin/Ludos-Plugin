package fr.ludos.core.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a Command's usage.
 */
public interface CommandUsageProvider {
	public abstract String getUsage(@NotNull CommandSender sender);
}
