package fr.ludos.core.command.ludos.role;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;

public class RoleSet implements Subcommand {
	private final static String id = "set";

	private final Ludos plugin;
	public RoleSet(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Set a player's role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		String roleId = args[0].toLowerCase();
		Role.Builder setRole = Role.getRoleById(roleId);
		if (setRole == null) {
			sender.sendMessage("Role not found: " + roleId);
			return true;
		}

		Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
		if (setTarget == null) {
			sender.sendMessage("Player not found.");
			return true;
		}

		Role.setRole(setTarget, roleId, plugin);
		if (setTarget != sender) {
			sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + roleId);
		} else {
			sender.sendMessage("Your role is now " + roleId);
		}
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return Role.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		if (args.length == 2)
			return CommandUtility.getOnlinePlayerNames();

		return null;
	}
	@Override
	public String getUsage() {
		return "<role_id> [player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}