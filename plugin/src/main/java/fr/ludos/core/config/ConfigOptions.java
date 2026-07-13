package fr.ludos.core.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigOptions {
	private final @NotNull String name;
	public @NotNull String getName() {
		return name;
	}

	public ConfigOptions(@NotNull String name) {
		this.name = name;
	}

	public abstract @NotNull Set<@NotNull String> getOptions(CommandSender sender);
	public boolean isValidOption(String option, CommandSender sender) {
		return getOptions(sender).contains(option);
	}

	public abstract @Nullable String getDefaultValue();
	public final @Nullable String getValueOrDefault(String key, ConfigurationSection container) {
		return container.getString(key, getDefaultValue());
	}

	public boolean unsetValue(String key, CommandSender sender, ConfigurationSection container) {
		container.set(key, null);
		sender.sendMessage(getName() + " reset");
		return true;
	}
	public boolean setValue(String key, @NotNull String[] args, CommandSender sender, ConfigurationSection container) {
		String value = args[0];
		if (! isValidOption(value, sender)) return false;

		container.set(key, value);
		sender.sendMessage(getName() + " set to " + value);
		return true;
	}

	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getOptions(sender).stream().toList();
		}

		return Collections.emptyList();
	}
}
