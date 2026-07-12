package fr.ludos.core.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class ConfigHashMap extends ConfigMap {
	private final Map<String, ? extends ConfigOptions<?>> values;

	public ConfigHashMap(String namespace, Map<String, ? extends ConfigOptions<?>> values) {
		super(namespace);
		this.values = values;
	}
	public ConfigHashMap(String namespace, Collection<? extends ConfigEntry<?>> entries) {
		this(
			namespace,
			entries.stream()
				.collect(Collectors.toMap(
					ConfigEntry::key,
					ConfigEntry::options
				))
		);
	}

	@Override
	public final @NotNull Set<@NotNull String> getValues() {
		return values.keySet();
	}

	@Override
	public final ConfigOptions<?> getOptions(String name) {
		return values.get(name);
	}
}
