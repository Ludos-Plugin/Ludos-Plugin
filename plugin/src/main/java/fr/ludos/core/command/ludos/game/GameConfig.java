package fr.ludos.core.command.ludos.game;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;

/**
 * {@link Subcommand} for {@link Game}-specific configuration.
 */
public class GameConfig extends ConfigSubcommand {
	public GameConfig(GameManager manager) {
		super(manager.getLudos(), "Configure a game.", new ScopeConfigMap(manager.getLudos(), manager.configMap));
	}
}