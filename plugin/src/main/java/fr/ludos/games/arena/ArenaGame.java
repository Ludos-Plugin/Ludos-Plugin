package fr.ludos.games.arena;

import java.time.Duration;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import fr.ludos.core.Ludos;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.persistence.config.ConfigEntriesCollection;
import fr.ludos.core.persistence.config.ConfigEntriesMap;
import fr.ludos.core.persistence.config.valueEntry.GroupPlayersConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;
import fr.ludos.core.wave.WaveController;
import fr.ludos.core.wave.WaveGame;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;

/**
 * Arena game implementation.
 */
public class ArenaGame extends WaveGame {
	public static final String ID = "arena";

	private final Builder builder;
	public final Builder builder() {
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
			builder.rounds.getGameConfig(group, builder)
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
			builder.team1Players.getGameConfig(group, builder),
			builder.team2Players.getGameConfig(group, builder)
		);
	}

	@Override
	protected void onGameSetup() {
		super.onGameSetup();
	}

	/**
	 * Builder for {@link ArenaGame}.
	 */
	public static class Builder extends Game.Builder {
		public final GroupPlayersConfigEntry team1Players =
			new GroupPlayersConfigEntry(getManager().getLudos().getGroupManager(), "Team 1 players", "team_1", "random");

		public final GroupPlayersConfigEntry team2Players =
			new GroupPlayersConfigEntry(getManager().getLudos().getGroupManager(), "Team 2 players", "team_2", "random");

		public final IntegerConfigEntry rounds =
			new IntegerConfigEntry("Number of Rounds", "rounds", null, 3, true);

		private final ConfigEntriesMap configMap =
			new ConfigEntriesMap(ID, Set.of(team1Players, team2Players, ArenaModeOption.CONFIG, rounds, WorldBorderArea.CONFIG));

		public Builder(GameManager manager) {
			super(manager);
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
		public ConfigEntriesCollection getConfig() {
			return configMap;
		}

		@Override
		public ArenaGame build(Group group) {
			return new ArenaGame(this, group);
		}
	}
}
