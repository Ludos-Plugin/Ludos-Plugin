package fr.ludos.core.command.ludos.role;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class RoleSubcommand extends SubcommandHandler {
	public RoleSubcommand(Ludos ludos) {
		super("role", "Manage Ludos Roles", false, getSubcommands(ludos));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos ludos) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new RoleGet());
			add(new RoleSet(ludos));
			add(new RoleReset(ludos));
			add(new RoleConfig(ludos));
			add(new RoleGuidebook());
		}};
		HelpSubcommand help = new HelpSubcommand("role", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
