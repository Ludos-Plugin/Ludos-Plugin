package fr.ludos.core.command.ludos.game;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.group.GroupConfigs;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;

public class GameConfig implements Subcommand {
	private final static String id = "config";

	private final Ludos plugin;
	public GameConfig(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Configure a game.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can configure games.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		boolean membersCanConfig = GroupConfigs.getGroupRightsOption(group.getConfig()).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return true;
		}

		String configGameId = args[0].toLowerCase();
		Game.Builder configGame = Game.getRegistered().get(configGameId);
		if (configGame == null) {
			sender.sendMessage("Game not found: " + configGameId);
			return true;
		}

		ConfigurationSection configSection = group.getConfig();
		if (! configSection.isConfigurationSection(Game.namespace)) {
			configSection.createSection(Game.namespace);
		}
		ConfigurationSection gamesSection = configSection.getConfigurationSection(Game.namespace);

		boolean res = configGame.executeGameConfig(sender, command, label, gamesSection, Arrays.copyOfRange(args, 1, args.length));

		if (res) {
			group.saveConfigGroup();
			plugin.saveConfig();
		}

		return res;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		String configGameId = args[0].toLowerCase();
		Game.Builder configGame = Game.getRegistered().get(configGameId);
		if (configGame == null) return null;

		return configGame.gameConfigTabComplete(sender, command, label, java.util.Arrays.copyOfRange(args, 1, args.length));
	}
	@Override
	public String getUsage() {
		return "<" +
			Game.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}