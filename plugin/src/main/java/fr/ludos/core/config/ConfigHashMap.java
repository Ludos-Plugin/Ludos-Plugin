package fr.ludos.core.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class ConfigHashMap extends ConfigMap {
	private final Map<String, ConfigOptions> values;

	public ConfigHashMap(String namespace, Map<String, ConfigOptions> values) {
		super(namespace);
		this.values = values;
	}
	public ConfigHashMap(String namespace, Collection<ConfigEntryInterface> entries) {
		this(
			namespace,
			entries.stream()
				.collect(Collectors.toMap(
					ConfigEntryInterface::key,
					ConfigEntryInterface::options
				))
		);
	}

	@Override
	public final @NotNull Set<@NotNull String> getKeys() {
		return values.keySet();
	}

	@Override
	public final ConfigOptions getOptions(String name) {
		return values.get(name);
	}
}
