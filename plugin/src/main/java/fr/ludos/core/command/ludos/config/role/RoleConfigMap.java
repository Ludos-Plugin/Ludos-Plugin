package fr.ludos.core.command.ludos.config.role;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * Config Options Map for Role-specific configuration.
 */
public class RoleConfigMap extends ConfigOptionsCollection {
	private RoleManager manager;

	public RoleConfigMap(RoleManager manager) {
		super("role");
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return manager.getRoleIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigOptions getOptionsValue(String name) {
		Role.Builder role = manager.getRoleById(name);
		if (role == null) return null;

		return role.getConfig();
	}
}
