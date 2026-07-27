package fr.ludos.core.command.ludos.config.game;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.ludos.game.GameConfig;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * {@link ConfigNodeMap} for {@link Game}-specific configuration.
 */
public class GameConfigMap extends ConfigNodeCollection {
	private final GameManager manager;

	public GameConfigMap(GameManager manager) {
		super(
			GameConfig.WINDOW_TITLE,
			Game.NAMESPACE,
			new ItemBuilder(Material.MUSIC_DISC_CHIRP)
		);
		this.manager = Objects.requireNonNull(manager);
	}

	@Override
	public @NotNull Set<@NotNull String> options(CommandSender sender) {
		return manager.getGameIds().stream().collect(Collectors.toSet());
	}

	@Override
	public ConfigNode getNode(String name) {
		Game.Builder game = manager.getGameById(name);
		if (game == null) return null;

		return game.getConfig();
	}
	@Override
	public Collection<ConfigNode> getNodes() {
		return manager.getBuilders().stream()
			.map(b -> (ConfigNode) b.getConfig())
			.toList();
	}
}
