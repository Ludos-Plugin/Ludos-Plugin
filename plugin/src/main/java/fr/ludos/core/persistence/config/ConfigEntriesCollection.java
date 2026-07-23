package fr.ludos.core.persistence.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Utility;

/**
 * {@link ConfigEntry} implemented as a Collection of sub-{@link ConfigEntry}.
 */
public abstract class ConfigEntriesCollection extends ConfigEntry {
	private final @Nullable String namespace;

	public ConfigEntriesCollection(@Nullable String namespace) {
		this.namespace = namespace;
	}

	public @Nullable String namespace() {
		return namespace;
	}
	@Override
	public String key() {
		return namespace();
	}

	public abstract @Nullable ConfigEntry getEntry(String key);

	public final @NotNull Set<@NotNull String> getEntryOptions(String key, CommandSender sender) {
		ConfigEntry options = getEntry(key);
		if (options == null) return Collections.emptySet();

		return options.options(sender);
	}

	@Override
	public final boolean execute(@NotNull String[] args, CommandSender sender, ConfigurationSection config) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigEntry options = getEntry(key);
		if (options == null) return false;

		if (namespace != null) {
			config = Utility.getOrCreateConfigSection(config, namespace);
		}

		return options.execute(Arrays.copyOfRange(args, 1, args.length), sender, config);
	}

	@Override
	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		ConfigEntry options = getEntry(args[0]);
		if (options == null) return null;

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
}
