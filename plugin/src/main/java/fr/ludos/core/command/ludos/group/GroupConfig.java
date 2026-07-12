package fr.ludos.core.command.ludos.group;

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
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupConfigMap;

public class GroupConfig implements Subcommand {
	private final static String id = "config";

	private final Ludos plugin;
	public GroupConfig(Ludos plugin) {
		this.plugin = plugin;
	}


	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Configure a game for this group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can configure groups.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		ConfigurationSection config = group.getConfig();
		String configKey = args[0];

		if (args.length == 1) {
			sender.sendMessage(GroupConfigMap.instance.getOrDefault(configKey, config));
			return true;
		}

		boolean membersCanConfig = GroupConfigMap.instance.getMembersAuth(config).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return true;
		}

		boolean success = GroupConfigMap.instance.set(configKey, Arrays.copyOfRange(args, 1, args.length), sender, config);

		if (success) {
			group.saveConfigGroup();
			plugin.saveConfig();
		}

		return success;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return GroupConfigMap.instance.tabComplete(args, sender);
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