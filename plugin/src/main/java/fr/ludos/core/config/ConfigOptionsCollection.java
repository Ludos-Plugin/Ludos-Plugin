package fr.ludos.core.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Utility;

public abstract class ConfigOptionsCollection extends ConfigOptions implements ConfigEntryInterface {
	private final @Nullable String namespace;
	public @Nullable String getNamespace() {
		return namespace;
	}

	public ConfigOptionsCollection(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String key() {
		return namespace;
	}
	@Override
	public ConfigOptions options() {
		return this;
	}

	public abstract @Nullable ConfigOptions getOptionsValue(String key);

	public final @NotNull Set<@NotNull String> getOptions(String key, CommandSender sender) {
		ConfigOptions options = getOptionsValue(key);
		if (options == null) return Collections.emptySet();

		return options.getOptions(sender);
	}

	@Override
	public final boolean set(@NotNull String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigOptions options = getOptionsValue(key);
		if (options == null) return false;

		if (namespace != null) {
			config = Utility.getOrCreateConfigSection(config, namespace);
		}

		return options.set(Arrays.copyOfRange(args, 1, args.length), sender, config);
	}

	@Override
	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getOptions(sender).stream().toList();
		}

		ConfigOptions options = getOptionsValue(args[0]);
		if (options == null) return null;

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
}
