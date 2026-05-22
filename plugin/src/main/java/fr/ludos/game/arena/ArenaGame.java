package fr.ludos.game.arena;

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
import fr.ludos.command.ConfigSubcommandManager;
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaController;
import fr.ludos.game.lobbyController.structure.StructureLobbyController;
import fr.ludos.game.waves.WaveController;
import fr.ludos.game.waves.WaveGame;
import fr.ludos.game.worldController.GameWorldController;
import fr.ludos.game.worldController.SingleWorldController;
import fr.ludos.group.Group;
import fr.ludos.structure.LobbyStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.TriState;

public class ArenaGame extends WaveGame {
	public static final String ID = "arena";

	private final Builder builder;
	public final Builder getBuilder() {
		return this.builder;
	}

	private final SingleWorldController worldController;
	@Override
	public SingleWorldController getWorldController() {
		return worldController;
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

		ConfigurationSection config = group.getConfig();

		Location returnLocation = GameWorldController.pickInitialLocation(group);


		this.waveController = new ArenaWaveController(
			this,
			ArenaGameConfigs.getRounds(config)
		);

		this.worldController = new SingleWorldController(
			this,
			new StructureLobbyController(
				this,
				false,
				GroupConfigs.getWaitPlayersOption(config),
				GroupConfigs.getWaitDurationOption(config).getDuration(),
				new LobbyStructure.Builder(),
				() -> {
					if (isStarted()) {
						waveController.startWave();
					} else {
						start();
					}
				}
			),
			new WorldBorderAreaController(
				this,
				ArenaGameConfigs.getArea(config)
			),
			returnLocation
		);
		this.teamController = new ArenaTeamController(
			this,
			ArenaGameConfigs.getMode(config),
			ArenaGameConfigs.getChosenTeam(config, 0),
			ArenaGameConfigs.getChosenTeam(config, 1)
		);
	}

	@Override
	protected void onGameSetup() {
		super.onGameSetup();

		getWorldController().transferToNewWorld(builder.createWorldCreator());
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
			return Component.text("Arena").color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, true);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Round-based PvP arena with team battles.");
		}

		private final ConfigSubcommandManager<ArenaGameConfigs> configsSubcommand = new ConfigSubcommandManager<>(ArenaGameConfigs.values());
		@Override
		protected ConfigSubcommandManager<?> getConfigsSubcommand() {
			return configsSubcommand;
		}

		public WorldCreator createWorldCreator() {
			String worldName = "arena_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			wc.keepSpawnLoaded(TriState.FALSE);
			return wc;
		}

		@Override
		public ArenaGame build(Group group) {
			return new ArenaGame(this, group);
		}
	}
}
