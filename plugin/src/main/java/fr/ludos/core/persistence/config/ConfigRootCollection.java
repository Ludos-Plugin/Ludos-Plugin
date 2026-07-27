package fr.ludos.core.persistence.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * {@link ConfigRoot} implemented as a Collection of sub-{@link ConfigNode}s.
 */
public abstract class ConfigRootCollection implements ConfigRoot {
	public abstract @Nullable ConfigNode getNode(String key);
	public abstract @Nullable Collection<ConfigNode> getNodes();

	public final @NotNull Set<@NotNull String> getNodeOptions(String key, CommandSender sender) {
		ConfigNode node = getNode(key);
		if (node == null) return Collections.emptySet();

		return node.options(sender);
	}
}
