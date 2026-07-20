package fr.ludos.core.command.ludos.config.game;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;

/**
 * {@link ConfigOptionsMap} for {@link Game}-specific configuration.
 */
public class GameConfigMap extends ConfigOptionsCollection {
	private final GameManager gameManager;

	public GameConfigMap(GameManager gameManager) {
		super("game");
		this.gameManager = gameManager;
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return gameManager.getGameIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigOptions getOptionsValue(String name) {
		Game.Builder game = gameManager.getGameById(name);
		if (game == null) return null;

		return game.getConfig();
	}
}
