package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ConfigSubcommandManager;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupRightsOption;

public class GroupConfig implements Subcommand {
	private final static String id = "config";

	protected final ConfigSubcommandManager<GroupConfigs> configCommand = new ConfigSubcommandManager<>(GroupConfigs.values());

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

		GroupRightsOption opt = GroupConfigs.getGroupRightsOption(group.getConfig());
		boolean membersCanConfig = opt.canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group. " + opt);
			return true;
		}

		boolean res = configCommand.onCommand(sender, command, label, group.getConfig(), args);

		if (res) {
			group.saveConfigGroup();
			plugin.saveConfig();
		}

		return res;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return configCommand.onTabComplete(sender, command, label, args);
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