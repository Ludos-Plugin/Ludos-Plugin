package fr.ludos.core.persistence.config;

import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * {@link ConfigNode} implemented as a Collection of sub-{@link ConfigNode}.
 */
public abstract class ConfigNodeCollection extends ConfigRootCollection implements ConfigNode {
	private final TextComponent name;
	private final AbstractItemBuilder<?> displayItem;
	private final String namespace;

	public ConfigNodeCollection(@NotNull TextComponent name, AbstractItemBuilder<?> displayItem, @Nullable String namespace) {
		this.name = Objects.requireNonNull(name);
		this.displayItem = Objects.requireNonNull(displayItem);
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
	public Component name() {
		return name;
	}
	@Override
	public AbstractItemBuilder<?> item(Player player, ConfigSectionContext context) {
		return displayItem;
	}

	@Override
	protected ConfigSectionContext prepareForChildren(ConfigSectionContext context) {
		return context.getDeeper(namespace, this);
	}
}
