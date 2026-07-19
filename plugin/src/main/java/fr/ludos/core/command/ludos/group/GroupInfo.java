package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;

/**
 * {@link Subcommand} to get info about the current {@link Group}, as a member.
 */
public class GroupInfo implements Subcommand {
	private final static String ID = "info";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Get information about your current group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can get group information.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		sender.sendMessage("Group leader: " + group.getLeader().getName());
		sender.sendMessage("Group members: " + group.getMembers().stream().map(OfflinePlayer::getName).collect(Collectors.joining(", ")));
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return null;
	}
	@Override
	public String getUsage() {
		return "";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}