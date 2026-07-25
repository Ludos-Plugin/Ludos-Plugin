package fr.ludos.core.persistence.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Functions;

/**
 * {@link ConfigNode} implemented as a Map-like structure of sub-{@link ConfigNode}.
 */
public class ConfigNodeMap extends ConfigNodeCollection {
	private final Map<String, ConfigNode> values;

	public ConfigNodeMap(String namespace, Map<String, ConfigNode> values) {
		super(namespace);
		this.values = values;
	}
	public ConfigNodeMap(String namespace, Collection<ConfigNode> entries) {
		this(
			namespace,
			entries.stream()
				.filter(e -> e != null && e.key() != null)
				.collect(Collectors.toMap(ConfigNode::key, Functions.identity()))
		);
	}

	@Override
	public final @NotNull Set<@NotNull String> options(CommandSender sender) {
		return values.keySet();
	}

	@Override
	public final ConfigNode getEntry(String name) {
		return values.get(name);
	}
}
