package fr.ludos.core.command.ludos.role;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class RoleSubcommand extends SubcommandHandler {
	public RoleSubcommand(Ludos plugin) {
		super("role", "Manage Ludos Groups", false, getSubcommands(plugin));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos plugin) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new RoleGet());
			add(new RoleSet(plugin));
			add(new RoleReset(plugin));
			add(new RoleConfig());
			add(new RoleGuidebook());
		}};
		HelpSubcommand help = new HelpSubcommand("role", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
