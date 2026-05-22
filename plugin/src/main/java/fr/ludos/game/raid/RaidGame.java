package fr.ludos.game.raid;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.Ludos;
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaController;
import fr.ludos.game.arena.ArenaGameConfigs;
import fr.ludos.game.lobbyController.structure.StructureLobbyController;
import fr.ludos.game.worldController.GameWorldController;
import fr.ludos.game.worldController.MultiWorldController;
import fr.ludos.group.Group;
import fr.ludos.structure.LobbyStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class RaidGame extends Game {
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

	private final RaidWaveController wavesController;

	protected RaidGame(Builder builder, Group group) {
		super(builder, group);

		ConfigurationSection config = group.getConfig();

		Location returnLocation = GameWorldController.pickInitialLocation(group);

		this.wavesController = new RaidWaveController(
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
						wavesController.startWave();
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

		getWorldController().transferToNewWorld(wavesController.getCurrentWaveTheme().getWorldCreator());
	}

	@Override
	protected void onGameStart() {
		super.onGameStart();

		wavesController.start();
		wavesController.startWave();
	}

	@Override
	protected void onGameStop() {
		super.onGameStop();

		wavesController.stop();
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

		@Override
		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game " + getId() + " config <config> [value]");
			for (RaidGameConfigs config : RaidGameConfigs.values()) {
				usage.append("\n  ").append(config.name()).append(" ").append(config.getUsage());
			}
			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) return false;
			RaidGameConfigs option = Arrays.stream(RaidGameConfigs.values()).filter(o -> o.name().equalsIgnoreCase(args[0])).findFirst().orElse(null);
			if (option == null) return false;
			return option.handleConfigsCommand(sender, command, label, config, Arrays.copyOfRange(args, 1, args.length));
		}

		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(RaidGameConfigs.values()).map(RaidGameConfigs::name).collect(Collectors.toList());
			}
			if (!EnumUtils.isValidEnumIgnoreCase(RaidGameConfigs.class, args[0])) return null;
			RaidGameConfigs option = EnumUtils.getEnumIgnoreCase(RaidGameConfigs.class, args[0]);
			return option.handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		}

		@Override
		public RaidGame build(Group group) {
			return new RaidGame(this, group);
		}
	}
}
