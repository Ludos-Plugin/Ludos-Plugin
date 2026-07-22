package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} to promote another player in the current {@link Group} to Group leader.
 */
public class GroupPromote implements Subcommand {
	private final static String ID = "promote";

	private final GroupManager manager;
	public GroupPromote(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Promote the role of Group leader to another player.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can promote groups.");
			return true;
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		if (! group.isLeader(player)) {
			sender.sendMessage("Only the group leader can promote a member to leader.");
			return true;
		}

		Player target = CommandUtility.getPlayerFromArg(args, 0);
		if (target == null) {
			player.sendMessage("Could not find Player.");
			return true;
		}

		if (group.isLeader(target)) {
			sender.sendMessage(target.getName() + " is already group leader.");
			return true;
		}

		if (! group.isMember(target)) {
			sender.sendMessage(target.getName() + " is not a member of your group.");
			return true;
		}

		if (group.promoteToLeader(target)) {
			manager.saveConfig();
		}

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) return null;

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) return null;

		if (! group.isLeader(player)) return null;

		return group.getMembers().stream()
			.map(OfflinePlayer::getName)
			.collect(Collectors.toList());
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<player>";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}