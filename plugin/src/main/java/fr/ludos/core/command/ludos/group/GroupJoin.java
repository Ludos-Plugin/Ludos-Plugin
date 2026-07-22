package fr.ludos.core.command.ludos.group;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.AddPlayerMethod;
import fr.ludos.core.group.Group.AddPlayerResult;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} to request joining a Player's {@link Group}.
 */
public class GroupJoin implements Subcommand {
	private final static String ID = "join";

	private final GroupManager manager;
	public GroupJoin(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Join a group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can join groups.");
			return true;
		}
		if (args.length < 1) return false;

		Player target = CommandUtility.getPlayerFromArg(args, 0);
		if (target == null) {
			sender.sendMessage("Could not find Player.");
			return true;
		}

		Group group = manager.getGroupOfPlayer(target);
		if (group == null) {
			sender.sendMessage(target.getName() + " is not in a group.");
			return true;
		}

		if (group == manager.getGroupOfPlayer(player)) {
			sender.sendMessage("You are already in this group.");
			return true;
		}

		AddPlayerResult res = group.requestAddPlayer(player, AddPlayerMethod.Join);
		switch (res) {
			case Succeeded:
				manager.saveConfig();
				break;
			case Requested:
				player.sendMessage("Requested to join " + target.getName() + "'s group.");
			default:
				break;
		}

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return CommandUtility.getOnlinePlayerNames();

		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<memberName>";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}