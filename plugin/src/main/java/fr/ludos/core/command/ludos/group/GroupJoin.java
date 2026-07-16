package fr.ludos.core.command.ludos.group;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.JoinMethod;
import fr.ludos.core.group.Group.JoinResult;

public class GroupJoin implements Subcommand {
	private final static String ID = "join";

	private final Ludos plugin;
	public GroupJoin(Ludos plugin) {
		this.plugin = plugin;
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

		String leaderName = args[0];
		Player leader = plugin.getServer().getPlayerExact(leaderName);
		if (leader == null) {
			sender.sendMessage("Player not found: " + leaderName);
			return true;
		}

		Group group = Group.getGroupOfPlayer(leader);
		if (group == null) {
			sender.sendMessage(leader.getName() + " is not in a group.");
			return true;
		}

		JoinResult res = group.requestPlayerJoin(player, JoinMethod.Join);
		switch (res) {
			case Succeeded:
				plugin.saveGroups();
				break;
			case Requested:
				player.sendMessage("Requested to join " + leaderName + "'s group.");
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
	public String getUsage() {
		return "<memberName>";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}