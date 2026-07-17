package fr.ludos.core.command.ludos.game;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;

public final class GameSubcommand extends SubcommandHandler {
	public GameSubcommand(Ludos ludos) {
		super("game", "Manage Ludos Games", false, getSubcommands(ludos));
	}

	private static final ArrayList<Subcommand> getSubcommands(Ludos ludos) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GameStart());
			add(new GameStop());
			add(new GameConfig(ludos));
			add(new GameGuidebook());
		}};
		HelpSubcommand help = new HelpSubcommand("game", subcommands);
		subcommands.add(help);
		return subcommands;
	}
}
