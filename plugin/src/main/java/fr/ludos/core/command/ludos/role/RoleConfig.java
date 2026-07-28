package fr.ludos.core.command.ludos.role;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.persistence.config.scope.ScopeConfigMap;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * {@link Subcommand} for {@link Role}-specific configuration.
 */
public class RoleConfig extends ConfigSubcommand {
	public static final TextComponent WINDOW_TITLE = Component.text("Role configuration");

	public RoleConfig(RoleManager manager) {
		super(manager.getLudos(), "Configure a role.", new ScopeConfigMap(
			WINDOW_TITLE,
			manager.getLudos(),
			manager.configMap
		));
	}
}