package fr.ludos.core.command.ludos.config.role;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.ConfigEntry;
import fr.ludos.core.persistence.config.ConfigEntriesCollection;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * Config Options Map for Role-specific configuration.
 */
public class RoleConfigMap extends ConfigEntriesCollection {
	private RoleManager manager;

	public RoleConfigMap(RoleManager manager) {
		super(Role.NAMESPACE);
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return manager.getRoleIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigEntry getEntry(String name) {
		Role.Builder role = manager.getRoleById(name);
		if (role == null) return null;

		return role.getConfig();
	}
}
