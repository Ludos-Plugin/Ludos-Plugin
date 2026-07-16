package fr.ludos.core.command.ludos.role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;

public class RoleConfig implements Subcommand {
	private final static String ID = "config";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Configure a role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		String configRoleId = args[0].toLowerCase();
		Role.Builder configRole = Role.getRoleById(configRoleId);
		if (configRole == null) {
			sender.sendMessage("Role not found: " + configRoleId);
			return true;
		}

		return configRole.executeRoleConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return Role.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		String roleId = args[0].toLowerCase();
		Role.Builder configRole = Role.getRoleById(roleId);
		if (configRole == null) return null;

		return configRole.roleConfigTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}
	@Override
	public String getUsage() {
		return "<" +
			Role.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}