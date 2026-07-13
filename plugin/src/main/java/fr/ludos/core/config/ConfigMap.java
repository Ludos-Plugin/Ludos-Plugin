package fr.ludos.core.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigMap {
	private final String namespace;
	public final String namespace() {
		return namespace;
	}

	public ConfigMap(String namespace) {
		this.namespace = namespace;
	}

	public String namespacedPath(String key) {
		return namespace + "." + key;
	}

	public abstract @NotNull Set<@NotNull String> getKeys();

	public boolean hasValue(String name) {
		return getKeys().contains(name);
	}

	public abstract @Nullable ConfigOptions getOptions(String name);

	public final @NotNull Set<@NotNull String> getOptionValues(String name, CommandSender sender) {
		ConfigOptions options = getOptions(name);
		if (options == null) return Collections.emptySet();

		return options.getOptions(sender);
	}

	public final @Nullable String getDefaultOption(String name) {
		ConfigOptions options = getOptions(name);
		if (options == null) return null;

		return options.getDefaultValue();
	}

	public final @Nullable String getOrDefault(String name, ConfigurationSection container) {
		ConfigOptions options = getOptions(name);
		if (options == null) return null;

		return options.getValueOrDefault(namespacedPath(name), container);
	}

	public final boolean unset(String name, CommandSender sender, ConfigurationSection container) {
		ConfigOptions options = getOptions(name);
		if (options == null) return false;

		return options.unsetValue(namespacedPath(name), sender, container);
	}

	public final boolean set(String name, @NotNull String[] args, CommandSender sender, ConfigurationSection container) {
		ConfigOptions options = getOptions(name);
		if (options == null) return false;

		return options.setValue(namespacedPath(name), args, sender, container);
	}

	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getKeys().stream().toList();
		}
		String optionsKey = args[0];

		ConfigOptions options = getOptions(optionsKey);
		if (options == null) return null;

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}


	public final <T> T getTypedOptionValue(String key, TypedConfigOptions<T> options, ConfigurationSection config) {
		return options.getTypedValueOrDefault(namespacedPath(key), config);
	}
	public final <T> T getTypedOptionValue(TypedConfigEntry<T> entry, ConfigurationSection config) {
		return getTypedOptionValue(entry.key(), entry.options(), config);
	}
}
