package fr.ludos.core.command.ludos.role;

import java.util.ArrayList;

import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.SubcommandHandler;
import fr.ludos.core.role.RoleManager;

/**
 * Subcommand encapsulating all Role-specific Ludos subcommands.
 */
public final class RoleSubcommand extends SubcommandHandler {
	public RoleSubcommand(RoleManager manager) {
		super("role", "Manage Ludos Roles", false, new ArrayList<>() {{
			add(new RoleGet(manager));
			add(new RoleSet(manager));
			add(new RoleReset(manager));
			add(new RoleConfig(manager));
			add(new RoleGuidebook(manager));
			add(new HelpSubcommand("role", this));
		}});
	}
}
