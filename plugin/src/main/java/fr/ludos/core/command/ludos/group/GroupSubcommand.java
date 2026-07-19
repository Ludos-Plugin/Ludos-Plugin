package fr.ludos.core.command.ludos.group;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

/**
 * {@link Subcommand} encapsulating all {@link Group}-specific Ludos subcommands.
 */
public final class GroupSubcommand extends SubcommandHandler {
	public GroupSubcommand(Ludos ludos) {
		super("group", "Manage Ludos Groups", false, getSubcommands(ludos));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos ludos) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GroupCreate(ludos));
			add(new GroupDisband(ludos));
			add(new GroupJoin(ludos));
			add(new GroupInvite(ludos));
			add(new GroupLeave(ludos));
			add(new GroupKick(ludos));
			add(new GroupConfig(ludos));
			add(new GroupInfo());
		}};
		HelpSubcommand help = new HelpSubcommand("group", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
