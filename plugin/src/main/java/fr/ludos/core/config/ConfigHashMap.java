package fr.ludos.core.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public final class ConfigHashMap extends ConfigMap {
	private final Map<String, ? extends ConfigOptions<?>> values;

	public ConfigHashMap(String namespace, Map<String, ? extends ConfigOptions<?>> values) {
		super(namespace);
		this.values = values;
	}

	@Override
	public final ConfigOptions<?> getOptions(String name) {
		return values.get(name);
	}

	@Override
	public final @NotNull Set<@NotNull String> getValues() {
		return values.entrySet().stream()
			.map(Entry::getKey)
			.collect(Collectors.toSet());
	}
}
