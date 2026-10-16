package fr.ludos.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A NullCommand is a Command that does not require any arguments, and is used as a fallback for SubcommandManagers when no Subcommand is specified.
 */
public interface NullCommand {
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label);
}
