package fr.ludos.core.command.ludos.group;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * {@link Subcommand} for {@link Group}-specific configuration.
 */
public class GroupConfig extends ConfigSubcommand {
	public static final TextComponent WINDOW_TITLE = Component.text("Group configuration");

	public GroupConfig(GroupManager manager) {
		super(manager.getLudos(), "Configure for this group.", new ScopeConfigMap(
			WINDOW_TITLE,
			manager.getLudos(),
			GroupConfigMap.INSTANCE,
			GroupConfigMap.INSTANCE,
			null
		));
	}
}