package fr.ludos.games.raid;

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
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.wave.WaveController;
import fr.ludos.core.wave.WaveGame;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

public class RaidGame extends WaveGame {
	public static final String ID = "raid";

	private final Builder builder;
	public final Builder getBuilder() {
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
			RaidGameConfigMap.waves.getGameConfig(group, builder)
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
					WorldBorderArea.config.getGameConfig(group, builder)
				)
			)
			.build();

		this.teamController = new RaidTeamController(
			this,
			RaidGameConfigMap.players.getGameConfig(group, builder)
		);
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
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.namespace, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			return wc;
		}

		@Override
		public ConfigOptionsCollection getConfig() {
			return RaidGameConfigMap.instance;
		}

		@Override
		public RaidGame build(Group group) {
			return new RaidGame(this, group);
		}
	}
}
