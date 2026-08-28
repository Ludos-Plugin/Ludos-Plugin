package fr.ludos.core.persistence.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Functions;

/**
 * {@link ConfigNode} implemented as a Map-like structure of sub-{@link ConfigNode}.
 */
public abstract class ConfigRootMap implements ConfigRoot {
	private final Map<String, ConfigNode> values;

	public ConfigRootMap(Map<String, ConfigNode> values) {
		super();
		this.values = values;
	}
	public ConfigRootMap(Collection<ConfigNode> entries) {
		this(
			(Map<String, ConfigNode>) entries.stream()
				.filter(e -> e != null && e.key() != null)
				.collect(Collectors.toMap(
					ConfigNode::key, Functions.identity(),
					(a, b) -> a,
					LinkedHashMap::new
				))
		);
	}

	@Override
	public final @NotNull Set<@NotNull String> options(CommandSender sender) {
		return values.keySet();
	}
}
