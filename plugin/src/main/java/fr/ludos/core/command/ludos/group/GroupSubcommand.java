package fr.ludos.core.command.ludos.group;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class GroupSubcommand extends SubcommandHandler {
	public GroupSubcommand(Ludos plugin) {
		super("group", "Manage Ludos Groups", false, getSubcommands(plugin));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos plugin) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GroupCreate(plugin));
			add(new GroupDisband(plugin));
			add(new GroupJoin(plugin));
			add(new GroupInvite(plugin));
			add(new GroupLeave(plugin));
			add(new GroupKick(plugin));
			add(new GroupConfig(plugin));
			add(new GroupInfo());
		}};
		HelpSubcommand help = new HelpSubcommand("group", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
