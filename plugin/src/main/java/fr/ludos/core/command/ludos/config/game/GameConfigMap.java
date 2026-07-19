package fr.ludos.core.command.ludos.config.game;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.game.Game;

/**
 * {@link ConfigOptionsMap} for {@link Game}-specific configuration.
 */
public class GameConfigMap extends ConfigOptionsCollection {
	public static final GameConfigMap INSTANCE = new GameConfigMap();

	public GameConfigMap() {
		super("game");
	}

	@Override
	public @NotNull Set<@NotNull String> getOptions(CommandSender sender) {
		return Game.getGameIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigOptions getOptionsValue(String name) {
		Game.Builder game = Game.getGameById(name);
		if (game == null) return null;

		return game.getConfig();
	}
}
