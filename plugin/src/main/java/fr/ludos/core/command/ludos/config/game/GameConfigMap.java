package fr.ludos.core.command.ludos.config.game;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.persistence.config.ConfigEntry;
import fr.ludos.core.persistence.config.ConfigEntriesCollection;
import fr.ludos.core.persistence.config.ConfigEntriesMap;

/**
 * {@link ConfigEntriesMap} for {@link Game}-specific configuration.
 */
public class GameConfigMap extends ConfigEntriesCollection {
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
	public ConfigEntry getEntry(String name) {
		Game.Builder game = gameManager.getGameById(name);
		if (game == null) return null;

		return game.getConfig();
	}
}
