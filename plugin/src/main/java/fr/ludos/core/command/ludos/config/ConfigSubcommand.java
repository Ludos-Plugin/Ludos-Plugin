package fr.ludos.core.command.ludos.config;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class ConfigSubcommand extends SubcommandHandler {
	public ConfigSubcommand(Ludos plugin) {
		super("config", "Manage Ludos Configs", false, getSubcommands(plugin));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos plugin) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			// add(new ConfigStart());
			// add(new ConfigStop());
			// add(new ConfigConfig(plugin));
			// add(new ConfigGuidebook());
		}};
		HelpSubcommand help = new HelpSubcommand("config", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
