package fr.ludos.core.command.ludos.config.role;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * Config Nodes Map for Role-specific configuration.
 */
public class RoleConfigMap extends ConfigNodeCollection {
	private RoleManager manager;

	public RoleConfigMap(RoleManager manager) {
		super(Role.NAMESPACE);
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return manager.getRoleIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigNode getEntry(String name) {
		Role.Builder role = manager.getRoleById(name);
		if (role == null) return null;

		return role.getConfig();
	}
}
