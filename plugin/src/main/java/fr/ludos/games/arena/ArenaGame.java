package fr.ludos.games.arena;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupConfigMap;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.wave.WaveController;
import fr.ludos.core.wave.WaveGame;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;

public class ArenaGame extends WaveGame {
	public static final String ID = "arena";

	private final Builder builder;
	public final Builder getBuilder() {
		return this.builder;
	}

	private final WorldManager worldManager;
	@Override
	public WorldManager getWorldManager() {
		return worldManager;
	}

	private final ArenaTeamController teamController;
	@Override
	public ArenaTeamController getTeamController() {
		return teamController;
	}

	private final ArenaWaveController waveController;
	@Override
	public WaveController getWaveController() {
		return this.waveController;
	}


	protected ArenaGame(Builder builder, Group group) {
		super(builder, group);
		this.builder = builder;

		Location returnLocation = group.pickReturnLocation();


		this.waveController = new ArenaWaveController(
			this,
			ArenaGameConfigMap.ROUNDS.getGameConfig(group, builder)
		);

		this.worldManager = WorldManager.within(this, returnLocation)
			.of(builder.createWorldCreator())
			.withLobby(
				Lobby.within(this)
					.waitFor(group)
					.clear(ClearMode.ALL)
					.wait(Duration.ofSeconds(GroupConfigMap.START_DELAY.getGroupConfig(group)))
					.then(this::start)
			)
			.inArea(
				WorldBorderArea.within(this, WorldBorderArea.CONFIG.getGameConfig(group, builder))
			)
			.build();
		this.teamController = new ArenaTeamController(
			this,
			ArenaModeOption.CONFIG.getGameConfig(group, builder),
			ArenaGameConfigMap.TEAM_1_PLAYERS.getGameConfig(group, builder),
			ArenaGameConfigMap.TEAM_2_PLAYERS.getGameConfig(group, builder)
		);
	}

	@Override
	protected void onGameSetup() {
		super.onGameSetup();
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		return teamController.contains(player);
	}


	public static class Builder extends Game.Builder {
		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Arena")
				.color(NamedTextColor.DARK_GRAY);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Fight against each other.\n\n" +
				"Play through multiple rounds of Combat, as one team against another, or in a duel."
			);
		}

		public WorldCreator createWorldCreator() {
			String worldName = "arena_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.NAMESPACE, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			wc.keepSpawnLoaded(TriState.FALSE);
			return wc;
		}

		@Override
		public ConfigOptionsCollection getConfig() {
			return ArenaGameConfigMap.INSTANCE;
		}

		@Override
		public ArenaGame build(Group group) {
			return new ArenaGame(this, group);
		}
	}
}
