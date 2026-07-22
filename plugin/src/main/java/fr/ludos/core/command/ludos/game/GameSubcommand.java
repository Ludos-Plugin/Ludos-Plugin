package fr.ludos.core.command.ludos.game;

import java.util.ArrayList;

import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandHandler;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;

/**
 * {@link Subcommand} encapsulating all {@link Game}-specific Ludos subcommands.
 */
public final class GameSubcommand extends SubcommandHandler {
	public GameSubcommand(GameManager manager) {
		super("game", "Manage Ludos Games", false, new ArrayList<>() {{
			add(new GameStart(manager));
			add(new GameStop(manager));
			add(new GameConfig(manager));
			add(new GameGuidebook(manager));
			add(new HelpSubcommand("game", this));
		}});
	}
}
