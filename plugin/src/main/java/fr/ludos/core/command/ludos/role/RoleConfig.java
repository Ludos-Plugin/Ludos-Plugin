package fr.ludos.core.command.ludos.role;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * {@link Subcommand} for {@link Role}-specific configuration.
 */
public class RoleConfig extends ConfigSubcommand {
	public RoleConfig(RoleManager manager) {
		super(manager.getLudos(), "Configure a role.", new ScopeConfigMap(manager.getLudos(), manager.configMap));
	}
}