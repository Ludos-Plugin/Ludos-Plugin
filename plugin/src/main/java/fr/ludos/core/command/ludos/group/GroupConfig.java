package fr.ludos.core.command.ludos.group;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} for {@link Group}-specific configuration.
 */
public class GroupConfig extends ConfigSubcommand {
	public GroupConfig(GroupManager manager) {
		super(manager.getLudos(), "Configure for this group.", manager.getScopeConfigMap());
	}
}