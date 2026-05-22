package fr.ludos.game.raid;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.Ludos;
import fr.ludos.command.ConfigSubcommandManager;
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaController;
import fr.ludos.game.arena.ArenaGameConfigs;
import fr.ludos.game.lobbyController.structure.StructureLobbyController;
import fr.ludos.game.waves.WaveController;
import fr.ludos.game.waves.WaveGame;
import fr.ludos.game.worldController.GameWorldController;
import fr.ludos.game.worldController.MultiWorldController;
import fr.ludos.group.Group;
import fr.ludos.structure.LobbyStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RaidGame extends WaveGame {
	public static final String ID = "raid";

	private final MultiWorldController worldController;
	@Override
	public MultiWorldController getWorldController() {
		return worldController;
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

		ConfigurationSection config = group.getConfig();

		Location returnLocation = GameWorldController.pickInitialLocation(group);

		this.waveController = new RaidWaveController(
			this,
			RaidGameConfigs.getWaves(config)
		);

		this.worldController = new MultiWorldController(
			this,
			new StructureLobbyController(
				this,
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
		this.teamController = new RaidTeamController(
			this,
			RaidGameConfigs.getChosenPlayers(config)
		);
	}

	@Override
	protected void onGameSetup() {
		super.onGameSetup();

		getWorldController().transferToNewWorld(waveController.getCurrentWaveTheme().getWorldCreator());
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

		@Override
		public RaidGame build(Group group) {
			return new RaidGame(this, group);
		}
	}
}
