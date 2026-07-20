package fr.ludos.core.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.Utility;
import fr.ludos.core.command.ludos.config.game.GameConfigMap;
import fr.ludos.core.group.Group;

/**
 * Manager class for {@link Game}s, used to maintain a registry of games for use in {@link Ludos}.
 */
public final class GameManager {
	public static final String NAMESPACE = "game";

	private final Ludos ludos;

	private final Map<String, Game.Builder> registered = new HashMap<>();
	private final Set<Game> active = new HashSet<>();

	public final GameConfigMap configMap = new GameConfigMap(this);


	public GameManager(Ludos ludos) {
		this.ludos = Objects.requireNonNull(ludos);
	}


	public final Ludos getLudos() {
		return ludos;
	}

	public Map<String, Game.Builder> getRegistered() {
		return registered;
	}

	@Nullable
	public Game.Builder getGameById(String gameId) {
		return registered.getOrDefault(gameId, null);
	}

	public List<String> getGameIds() {
		return registered.keySet().stream().collect( Collectors.toList() );
	}
	public List<Game.Builder> getGameBuilders() {
		return registered.values().stream().collect( Collectors.toList() );
	}

	public void registerGame(Game.Builder builder) {
		registered.put(builder.getId(), builder);
	}

	public Set<Game> getActiveGames() {
		return Collections.unmodifiableSet(active);
	}

	public boolean startGame(String id, Group group) {
		Game.Builder builder = registered.get(id);
		if (builder == null) return false;

		Game oldGame = group.getGame();
		if (oldGame != null) {
			oldGame.stop();
			if (! oldGame.isClear()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if (! oldGame.isClear()) return;

						startGame(builder, group);
						cancel();
					}
				}.runTaskTimer(oldGame.getPlugin(), 0, 20);
				return true;
			}
		}

		startGame(builder, group);

		return true;
	}

	private final void startGame(Game.Builder builder, Group group) {
		Game game = builder.build(group);
		game.addSetupListener(() -> {
			group.setGame(game);
			active.add(game);
		});
		game.addTeardownListener(() -> {
			group.setGame(null);
			active.remove(game);
		});
		game.setUp();
	}


	public ConfigurationSection getGlobalGameConfig(Game.Builder game) {
		return Utility.getOrCreateConfigSection(ludos.getConfig(), configMap.getNamespace() + "." + game.getId());
	}
}
