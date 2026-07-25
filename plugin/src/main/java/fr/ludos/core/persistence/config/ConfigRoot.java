package fr.ludos.core.persistence.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigRoot {
	public @NotNull Set<@NotNull String> options(CommandSender sender);

	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode);

	public default @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		return Collections.emptyList();
	}
}
