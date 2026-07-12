package fr.ludos.core.config;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigOptions<T> {
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

	public abstract @Nullable T getDefaultValue();
	public final @Nullable String getDefaultStringValue() {
		return toString(getDefaultValue());
	}
	public final @Nullable String getValueOrDefault(String key, ConfigurationSection container) {
		return container.getString(key, getDefaultStringValue());
	}
	public final @Nullable T getTypedValueOrDefault(String key, ConfigurationSection container) {
		String found = container.getString(key);
		if (found == null) return getDefaultValue();

		return fromString(found);
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

	protected abstract @Nullable T fromString(String value);
	protected abstract @Nullable String toString(T value);
}
