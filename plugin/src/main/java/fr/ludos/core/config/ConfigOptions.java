package fr.ludos.core.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public abstract class ConfigOptions {
	public abstract @NotNull Set<@NotNull String> getOptions(CommandSender sender);

	public abstract boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config);

	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getOptions(sender).stream().toList();
		}

		return Collections.emptyList();
	}
}
