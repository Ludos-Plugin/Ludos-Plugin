package fr.ludos.core.command.ludos.game;

import java.util.ArrayList;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;
import fr.ludos.core.game.Game;

/**
 * {@link Subcommand} encapsulating all {@link Game}-specific Ludos subcommands.
 */
public final class GameSubcommand extends SubcommandHandler {
	public GameSubcommand(Ludos ludos) {
		super("game", "Manage Ludos Games", false, new ArrayList<>() {{
			add(new GameStart(ludos));
			add(new GameStop(ludos));
			add(new GameConfig(ludos));
			add(new GameGuidebook(ludos));
			add(new HelpSubcommand("game", this));
		}});
	}
}
