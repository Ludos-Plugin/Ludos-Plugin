package fr.ludos.core.persistence.config;

import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;

/**
 * {@link ConfigNode} implemented as a Collection of sub-{@link ConfigNode}.
 */
public abstract class ConfigNodeCollection extends ConfigRootCollection implements ConfigNode {
	private final @Nullable String namespace;

	public ConfigNodeCollection(@Nullable String namespace) {
		this.namespace = Objects.requireNonNull(namespace);
	}
	public String namespace() {
		return namespace;
	}
	@Override
	public String key() {
		return namespace;
	}

	@Override
	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode) {
		return super.execute(args, sender, context.getDeeper(namespace), mode);
	}
}
