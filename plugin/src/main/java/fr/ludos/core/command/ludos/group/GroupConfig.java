package fr.ludos.core.command.ludos.group;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} for {@link Group}-specific configuration.
 */
public class GroupConfig extends ConfigSubcommand {
	public GroupConfig(GroupManager manager) {
		super("Configure for this group.", new ScopeConfigMap(manager.getLudos(), GroupConfigMap.INSTANCE));
	}
}