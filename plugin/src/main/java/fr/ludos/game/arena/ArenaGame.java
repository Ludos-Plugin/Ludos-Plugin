package fr.ludos.game.arena;

import java.time.Duration;
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
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.Game;
import fr.ludos.game.raid.RaidGameConfigs;
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

		ConfigurationSection config = group.getConfig();

		Location returnLocation = group.pickReturnLocation();


		this.waveController = new ArenaWaveController(
			this,
			ArenaGameConfigs.getRounds(config)
		);

		this.worldManager = WorldManager.within(getPlugin(), returnLocation)
			.of(builder.createWorldCreator())
			.withLobby(
				Lobby.within(getPlugin())
					.waitFor(group)
					.clear(ClearMode.ALL)
					.wait(Duration.ofSeconds(GroupConfigs.getWaitDurationOption(config).getDuration()))
					.then(this::start)
			)
			.inArea(
				WorldBorderArea.within(getPlugin())
					.ofSize(RaidGameConfigs.getArea(config))
			)
			.build();
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
