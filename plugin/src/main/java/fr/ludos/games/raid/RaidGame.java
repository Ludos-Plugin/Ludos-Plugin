package fr.ludos.games.raid;

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
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.persistence.config.ConfigEntriesCollection;
import fr.ludos.core.persistence.config.ConfigEntriesMap;
import fr.ludos.core.persistence.config.valueEntry.GroupPlayersConfigEntry;
import fr.ludos.core.persistence.config.valueEntry.NumberConfigEntry;
import fr.ludos.core.wave.WaveController;
import fr.ludos.core.wave.WaveGame;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Implementation of the Raid {@link Game}.
 */
public class RaidGame extends WaveGame {
	public static final String ID = "raid";

	private final Builder builder;
	public final Builder builder() {
		return this.builder;
	}

	private final WorldManager worldManager;
	@Override
	public WorldManager getWorldManager() {
		return worldManager;
	}

	private final RaidTeamController teamController;
	@Override
	public RaidTeamController getTeamController() {
		return teamController;
	}

	private final RaidWaveController waveController;
	@Override
	public WaveController getWaveController() {
		return this.waveController;
	}


	protected RaidGame(Builder builder, Group group) {
		super(builder, group);
		this.builder = builder;

		Location returnLocation = group.pickReturnLocation();


		this.waveController = new RaidWaveController(
			this,
			builder.waves.getGameConfig(group, builder)
		);

		this.worldManager = WorldManager.within(this, returnLocation)
			.of(builder.createWorldCreator())
			.withLobby(
				Lobby.within(this)
					.clear(ClearMode.ALL)
					.waitFor(group)
					.then(this::start)
			)
			.inArea(
				WorldBorderArea.within(
					this,
					WorldBorderArea.CONFIG.getGameConfig(group, builder)
				)
			)
			.build();

		this.teamController = new RaidTeamController(
			this,
			builder.players.getGameConfig(group, builder)
		);
	}

	/**
	 * Builder for {@link RaidGame}.
	 */
	public static class Builder extends Game.Builder {
		public final GroupPlayersConfigEntry players =
			new GroupPlayersConfigEntry(getManager().getLudos().getGroupManager(), "Players", "players", "all");

		public final NumberConfigEntry waves =
			new NumberConfigEntry("Number of Waves", "waves", null, 0, true);

		public final ConfigEntriesMap config =
			new ConfigEntriesMap(ID, Set.of(players, waves, WorldBorderArea.CONFIG));


		public Builder(GameManager manager) {
			super(manager);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Raid")
				.color(NamedTextColor.GOLD);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Fight against hordes of Enemies with your friends.\n\n" +
				"Every few waves, you will have to defeat an especially ferocious enemy..."
			);
		}

		public WorldCreator createWorldCreator() {
			String worldName = "raid_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.NAMESPACE, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			return wc;
		}

		@Override
		public ConfigEntriesCollection getConfig() {
			return config;
		}

		@Override
		public RaidGame build(Group group) {
			return new RaidGame(this, group);
		}
	}
}
