package fr.ludos.core.command.ludos.role;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.role.Role;

public class RoleReset implements Subcommand {
	private final static String id = "reset";

	private final Ludos plugin;
	public RoleReset(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Reset a player's role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		Player target = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
		if (target == null) {
			sender.sendMessage("Could not find Player");
			return true;
		}

		if (sender.isOp() || sender == target) {
			Role.removeRole(target, plugin);
			return true;
		}

		if (sender instanceof Player player) {
			final Group group = Group.getGroupOfPlayer(player);
			if (group.isLeader(player) && group == Group.getGroupOfPlayer(target)) {
				Role.removeRole(target, plugin);
				return true;
			}
		}

		sender.sendMessage("You are not authorized to reset this player's role");
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
		return "[player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}