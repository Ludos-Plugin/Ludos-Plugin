package fr.ludos.core.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigOptionsMap extends ConfigOptionsCollection {
	private final Map<String, ConfigOptions> values;

	public ConfigOptionsMap(String namespace, Map<String, ConfigOptions> values) {
		super(namespace);
		this.values = values;
	}
	public ConfigOptionsMap(String namespace, Collection<ConfigEntryInterface> entries) {
		this(
			namespace,
			entries.stream()
				.collect(Collectors.toMap(
					ConfigEntryInterface::key,
					ConfigEntryInterface::options
				))
		);
	}
	public ConfigOptionsMap(Map<String, ConfigOptions> values) {
		this(null, values);
	}
	public ConfigOptionsMap(Collection<ConfigEntryInterface> entries) {
		this(null, entries);
	}

	@Override
	public final @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return values.keySet();
	}

	@Override
	public final ConfigOptions getOptionsValue(String name) {
		return values.get(name);
	}
}
