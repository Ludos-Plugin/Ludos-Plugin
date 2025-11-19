package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.role.Role;

public final class RoleSubcommand implements TabExecutor {
	public static final String arg = "role";
	private static final String noRoleLabel = "none";


	protected final Ludos plugin;


	public RoleSubcommand(Ludos plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		RoleSubcommandOptions option = Arrays.stream(RoleSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return false;

		return onCommandOption(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}
	private boolean onCommandOption(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull RoleSubcommandOptions option) {
		switch (option) {
		case get:
			Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
			if (getTarget == null) {
				sender.sendMessage(noRoleLabel); // TODO: Translate
				return true;
			}

			Role.Builder getRole = Role.getRole(getTarget);
			sender.sendMessage(getRole == null ? noRoleLabel : getRole.getId());
			return true;
		case set:
			if (args.length < 1) break;

			String roleId = args[0].toLowerCase();
			Role.Builder setRole = Role.getRoleById(roleId);
			if (setRole == null) {
				sender.sendMessage("Role not found: " + roleId);
				break;
			}

			Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (setTarget == null) {
				sender.sendMessage("Player not found.");
				break;
			}

			Role.setRole(setTarget, roleId, plugin);
			if (setTarget != sender) {
				sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + roleId);
			} else {
				sender.sendMessage("Your role is now " + roleId);
			}
			return true;
		case config:
			if (args.length < 1) break;

			String configRoleId = args[0].toLowerCase();
			Role.Builder configRole = Role.getRoleById(configRoleId);
			if (configRole == null) {
				sender.sendMessage("Role not found: " + configRoleId);
				break;
			}

			return configRole.executeRoleConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case guidebook:
			if (args.length < 1) break;

			String guidebookRoleId = args[0].toLowerCase();
			Role.Builder guidebookRole = Role.getRoleById(guidebookRoleId);
			if (guidebookRole == null) break;

			Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (player == null) break;

			ItemStack book = guidebookRole.createGuidebook();
			player.getInventory().addItem(book);
			return true;
		case reset:
			Player target = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
			Role.removeRole(target, plugin);

			return true;
		case help:
			sender.sendMessage(getUsage(sender, command, label));

			return true;
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(RoleSubcommandOptions.values())
				.map(RoleSubcommandOptions::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0].toLowerCase();
		RoleSubcommandOptions option = Arrays.stream(RoleSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		return onRoleTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}
	private List<String> onRoleTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull RoleSubcommandOptions option) {
		switch (option) {
		case get:
			if (args.length == 1)
				return CommandUtility.getOnlinePlayerNames();
		case set:
			if (args.length == 1)
				return Role.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			if (args.length == 2)
				return CommandUtility.getOnlinePlayerNames();
		case config:
			if (args.length == 1)
				return Role.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			String roleId = args[0].toLowerCase();
			Role.Builder configRole = Role.getRoleById(roleId);
			if (configRole == null) break;

			return configRole.roleConfigTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case guidebook:
			if (args.length == 1)
				return Role.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

			if (args.length == 2)
				return CommandUtility.getOnlinePlayerNames();
		case reset:
			if (args.length == 1)
				return CommandUtility.getOnlinePlayerNames();
		case help:
			break;
		}

		return null;
	}


	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " role ");

		usage.append('<');
		usage.append(
			Arrays.stream(RoleSubcommandOptions.values()).sorted().map(RoleSubcommandOptions::toString)
				.collect(Collectors.joining(" | ") )
		);
		usage.append('>');

		usage.append(' ');

		usage.append('[');
		usage.append(
			Role.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining( " | "))
		);
		usage.append(']');

		usage.append(' ');

		usage.append("[player]");

		return usage.toString();
	}
}
