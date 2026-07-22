package fr.ludos.core.command.ludos.group;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} to disband the current {@link Group}, as the Group leader.
 */
public class GroupDisband implements Subcommand {
	private final static String ID = "disband";

	private final GroupManager manager;
	public GroupDisband(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Disband the current group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can disband groups.");
			return true;
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		boolean membersCanManage = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canManage();
		if (! group.isLeader(player) && ! membersCanManage) {
			sender.sendMessage("Only the group leader can disband the group.");
			return true;
		}

		group.disband();

		manager.saveConfig();

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}