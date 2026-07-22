package fr.ludos.core.command.ludos.group;

import java.util.ArrayList;

import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} encapsulating all {@link Group}-specific Ludos subcommands.
 */
public final class GroupSubcommand extends SubcommandHandler {
	public GroupSubcommand(GroupManager manager) {
		super("group", "Manage Ludos Groups", false, new ArrayList<>() {{
			add(new GroupCreate(manager));
			add(new GroupDisband(manager));
			add(new GroupJoin(manager));
			add(new GroupInvite(manager));
			add(new GroupLeave(manager));
			add(new GroupKick(manager));
			add(new GroupPromote(manager));
			add(new GroupConfig(manager));
			add(new GroupInfo(manager));
			add(new HelpSubcommand("group", this));
		}});
	}
}
