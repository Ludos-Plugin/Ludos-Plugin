package fr.ludos.core.persistence.config;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.WindowProvider;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigRoot extends WindowProvider {
	public @NotNull Set<@NotNull String> options(CommandSender sender);
}
