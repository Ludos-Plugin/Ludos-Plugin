package fr.ludos.core.command.ludos.role;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.role.Role;

public class RoleConfigMap extends ConfigOptionsCollection {
	public static final RoleConfigMap INSTANCE = new RoleConfigMap();

	public RoleConfigMap() {
		super("role");
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return Role.getRoleIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigOptions getOptionsValue(String name) {
		Role.Builder role = Role.getRoleById(name);
		if (role == null) return null;

		return role.getConfig();
	}
}
