package fr.ludos.core.command.ludos.group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} to kick members of the current {@link Group}, as the Group Leader, or an explicitly allowed member.
 */
public class GroupKick implements Subcommand {
	private final static String ID = "kick";

	private final GroupManager manager;
	public GroupKick(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Kick a player from the group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can kick members from groups.");
			return true;
		}
		if (args.length < 1) return false;

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		boolean membersCanManage = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canManage();
		if (! group.isLeader(player) && ! membersCanManage) {
			sender.sendMessage("Only the group leader can kick members.");
			return true;
		}

		boolean success = false;
		List<OfflinePlayer> targets = CommandUtility.getOfflinePlayersFromArgs(args, sender).stream()
			.collect(Collectors.toCollection(ArrayList::new));
		for (OfflinePlayer target : targets) {
			if (group.isMember(target)) {
				if (group.removePlayer(target, true)) {
					success = true;
					player.sendMessage("Kicked " + target.getName() + " from the group.");
				}
			} else {
				sender.sendMessage(target.getName() + " is not a member of your group.");
			}
		}

		if (success) {
			manager.saveConfig();
		}

		return true;
	}
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
		return "[member1] [member2] ...";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}