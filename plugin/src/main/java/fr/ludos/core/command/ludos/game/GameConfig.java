package fr.ludos.core.command.ludos.game;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.ConfigSubcommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * {@link Subcommand} for {@link Game}-specific configuration.
 */
public class GameConfig extends ConfigSubcommand {
	public static final TextComponent WINDOW_TITLE = Component.text("Game Configuration");

	public GameConfig(GameManager manager) {
		super(manager.getLudos(), "Configure a game.", new ScopeConfigMap(
			WINDOW_TITLE,
			manager.getLudos(),
			manager.configMap,
			manager.configMap,
			null
		));
	}
}