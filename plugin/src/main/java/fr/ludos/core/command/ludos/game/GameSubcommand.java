package fr.ludos.core.command.ludos.game;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class GameSubcommand extends SubcommandHandler {
	public GameSubcommand(Ludos plugin) {
		super("game", "Manage Ludos Games", false, getSubcommands(plugin));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos plugin) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GameStart());
			add(new GameStop());
			add(new GameConfig(plugin));
			add(new GameGuidebook());
		}};
		HelpSubcommand help = new HelpSubcommand("game", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
