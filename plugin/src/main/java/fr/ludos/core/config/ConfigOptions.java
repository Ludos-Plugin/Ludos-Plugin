package fr.ludos.core.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigOptions {
	public abstract @NotNull Set<@NotNull String> getOptions(CommandSender sender);
	public boolean isValidOption(String option, CommandSender sender) {
		return getOptions(sender).contains(option);
	}

	public abstract boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config);

	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getOptions(sender).stream().toList();
		}

		return Collections.emptyList();
	}
}
