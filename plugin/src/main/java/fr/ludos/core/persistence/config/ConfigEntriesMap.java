package fr.ludos.core.persistence.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Functions;

/**
 * {@link ConfigEntry} implemented as a Map-like structure of sub-{@link ConfigEntry}.
 */
public class ConfigEntriesMap extends ConfigEntriesCollection {
	private final Map<String, ConfigEntry> values;

	public ConfigEntriesMap(String namespace, Map<String, ConfigEntry> values) {
		super(namespace);
		this.values = values;
	}
	public ConfigEntriesMap(String namespace, Collection<ConfigEntry> entries) {
		this(
			namespace,
			entries.stream()
				.filter(e -> e != null && e.key() != null)
				.collect(Collectors.toMap(ConfigEntry::key, Functions.identity()))
		);
	}

	@Override
	public final @NotNull Set<@NotNull String> options(CommandSender sender) {
		return values.keySet();
	}

	@Override
	public final ConfigEntry getEntry(String name) {
		return values.get(name);
	}
}
