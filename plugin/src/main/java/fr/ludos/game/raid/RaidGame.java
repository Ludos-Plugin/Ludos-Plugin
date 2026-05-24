package fr.ludos.game.raid;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.Ludos;
import fr.ludos.area.WorldBorderArea;
import fr.ludos.command.ConfigSubcommandManager;
import fr.ludos.game.Game;
import fr.ludos.game.waves.WaveController;
import fr.ludos.game.waves.WaveGame;
import fr.ludos.group.Group;
import fr.ludos.lobby.Lobby;
import fr.ludos.lobby.Lobby.ClearMode;
import fr.ludos.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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

		ConfigurationSection config = group.getConfig();

		Location returnLocation = group.pickReturnLocation();


		this.waveController = new RaidWaveController(
			this,
			RaidGameConfigs.getWaves(config)
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
				WorldBorderArea.within(this)
					.ofSize(RaidGameConfigs.getArea(config))
			)
			.build();

		this.teamController = new RaidTeamController(
			this,
			RaidGameConfigs.getChosenPlayers(config)
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
			return Component.text("Raid").color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Co-op survival waves with monster bosses.");
		}

		private final ConfigSubcommandManager<RaidGameConfigs> configsSubcommand = new ConfigSubcommandManager<>(RaidGameConfigs.values());
		@Override
		protected ConfigSubcommandManager<?> getConfigsSubcommand() {
			return configsSubcommand;
		}

		public WorldCreator createWorldCreator() {
			String worldName = "raid_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			return wc;
		}

		@Override
		public RaidGame build(Group group) {
			return new RaidGame(this, group);
		}
	}
}
