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
import fr.ludos.core.config.ConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupConfigMap;

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
			sender.sendMessage("Only players can configure games through a group.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		String configGameId = args[0].toLowerCase();
		Game.Builder game = Game.getRegistered().get(configGameId);
		if (game == null) {
			sender.sendMessage("Game not found: " + configGameId);
			return true;
		}

		ConfigurationSection config = group.getConfig();
		String configKey = args[1];

		if (args.length == 2) {
			sender.sendMessage(game.getConfig().getOrDefault(configKey, config));
			return true;
		}

		boolean membersCanConfig = GroupConfigMap.instance.getMembersAuth(group.getConfig()).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return true;
		}

		boolean success = game.getConfig().set(configKey, Arrays.copyOfRange(args, 2, args.length), sender, config);

		if (success) {
			group.saveConfigGroup();
			plugin.saveConfig();
		}

		return success;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return Game.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		String configGameId = args[0].toLowerCase();
		Game.Builder game = Game.getRegistered().get(configGameId);
		if (game == null) return null;

		ConfigMap config = game.getConfig();
		return config.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
	@Override
	public String getUsage() {
		return "<" +
			Game.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}