package fr.ludos.core.command.ludos.config.game;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;

/**
 * {@link ConfigNodeMap} for {@link Game}-specific configuration.
 */
public class GameConfigMap extends ConfigNodeCollection {
	private final GameManager gameManager;

	public GameConfigMap(GameManager gameManager) {
		super(Game.NAMESPACE);
		this.gameManager = gameManager;
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return gameManager.getGameIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigNode getEntry(String name) {
		Game.Builder game = gameManager.getGameById(name);
		if (game == null) return null;

		return game.getConfig();
	}
}
