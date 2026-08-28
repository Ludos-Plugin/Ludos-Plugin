package fr.ludos.core.persistence.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Functions;

import net.kyori.adventure.text.TextComponent;

/**
 * {@link ConfigNode} implemented as a Map-like structure of sub-{@link ConfigNode}.
 */
public abstract class ConfigNodeMap extends ConfigNodeCollection {
	private final Map<String, ConfigNode> values;

	public ConfigNodeMap(@NotNull TextComponent name, String namespace, Map<String, ConfigNode> values) {
		super(name, namespace);
		this.values = values;
	}
	public ConfigNodeMap(@NotNull TextComponent name, String namespace, Collection<ConfigNode> entries) {
		this(
			name, namespace,
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

	@Override
	public Collection<ConfigNode> getNodes() {
		return values.values();
	}
	@Override
	public final ConfigNode getNode(String name) {
		return values.get(name);
	}
}
