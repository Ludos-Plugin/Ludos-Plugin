package fr.ludos.core.command.ludos.config.role;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;

/**
 * Config Nodes Map for Role-specific configuration.
 */
public class RoleConfigMap extends ConfigNodeCollection {
	private final RoleManager manager;

	public RoleConfigMap(RoleManager manager) {
		super(
			Role.CONFIG_WINDOW_TITLE,
			Role.NAMESPACE
		);
		this.manager = Objects.requireNonNull(manager);
	}

	@Override
	public AbstractItemBuilder<?> createItem(Player player) {
		return Role.createItem();
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return manager.getRoleIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigNode getNode(String name) {
		Role.Builder role = manager.getRoleById(name);
		if (role == null) return null;

		return role.getConfig();
	}
	@Override
	public Collection<ConfigNode> getNodes() {
		return manager.getBuilders().stream()
			.map(b -> (ConfigNode) b.getConfig())
			.toList();
	}
}
