package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		if (option != null) {
			switch (option) {
			case get:
				Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
				if (getTarget == null) {
					sender.sendMessage(noRoleLabel); // TODO: Translate
					return true;
				}

				Role.Builder role = Role.getRole(getTarget);
				sender.sendMessage(role == null ? noRoleLabel : role.getId());
			case none:
				Player target = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
				Role.removeRole(target, plugin);
				return true;
			case help:
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			}
		}

		Role.Builder role = Role.getRoleById(arg);
		if (role == null) {
			sender.sendMessage("Role not found: " + arg);
			return false;
		}

		return onRoleCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), role);
	}

	private boolean onRoleCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull Role.Builder role) {
		RoleOptions option;
		if (args.length == 0) {
			option = RoleOptions.set;
		}
		else {
			String arg = args[0];
			option = Arrays.stream(RoleOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
			if (option == null) return false;
		}

		switch (option) {
		case set:
			Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (setTarget == null) return false;

			Role.setRole(setTarget, role.getId(), plugin);
			if (setTarget != sender) {
				sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + role.getId());
			}
			return true;
		case config:
			return role.executeRoleConfig(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		case guidebook:
			if (args.length == 0) return false;
			if (!(sender instanceof Player player)) return false;

			String guidebookRoleId = args[0].toLowerCase();
			if (! Role.getRegistered().containsKey(guidebookRoleId)) return false;

			Role.Builder guidebookRole = Role.getRoleById(guidebookRoleId);
			if (guidebookRole == null) return false;

			ItemStack book = guidebookRole.createGuidebook();
			player.getInventory().addItem(book);
			return true;
		case help:
			sender.sendMessage(role.getRoleConfigUsage(sender, command, label));
			return true;
		default:
			return false;
		}
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Stream.concat(
				Role.getRegistered().keySet().stream(),
				Arrays.stream(RoleSubcommandOptions.values()).map(RoleSubcommandOptions::toString)
			)
				.collect(Collectors.toList());
		}

		String roleId = args[0].toLowerCase();

		Role.Builder role = Role.getRegistered().get(roleId);
		if (role == null) return null;

		return onRoleTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), role);
	}

	private List<String> onRoleTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull Role.Builder role) {
		if (args.length <= 1) {
			return Arrays.stream(RoleOptions.values()).map(RoleOptions::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0];
		RoleOptions option = Arrays.stream(RoleOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		switch (option) {
		case config:
			return role.roleConfigTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
		default:
			return null;
		}
	}


	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " role ");

		usage.append('<');
		usage.append(
			Stream.concat(
				Role.getRegistered().keySet().stream().sorted(),
				Arrays.stream(RoleSubcommandOptions.values()).sorted().map(RoleSubcommandOptions::toString)
			)
				.collect(Collectors.joining(" | ") )
		);
		usage.append('>');

		usage.append(' ');

		usage.append('[');
		usage.append(
			Arrays.stream(RoleOptions.values()).sorted().map(RoleOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		usage.append(']');

		usage.append(' ');

		usage.append("[player]");

		return usage.toString();
	}
}
