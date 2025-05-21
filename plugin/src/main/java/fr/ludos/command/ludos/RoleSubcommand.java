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

		String arg = args[0];
		RoleSubcommandOptions option = Arrays.stream(RoleSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return false;

		return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}

	private boolean handleConfigsCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull RoleSubcommandOptions option) {
		switch (option) {
			case get:
				Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
				if (getTarget == null) {
					sender.sendMessage(noRoleLabel); // TODO: Translate
					return true;
				}

				Role.Builder role = Role.getRole(getTarget);
				sender.sendMessage(role == null ? noRoleLabel : role.getId());

				return true;
			case reset:
				Player removeTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
				if (removeTarget == null) return false;

				Role.removeRole(removeTarget, plugin);
				if ( removeTarget != sender ) {
					sender.sendMessage("The Player " + removeTarget.getName() + " now has no role");
				}

				return true;
			case set:
				if (args.length == 0) return false;

				String roleId = args[0].toLowerCase();
				if (! Role.getRegistered().containsKey(roleId)) return false;

				Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
				if (setTarget == null) return false;

				Role.setRole(setTarget, roleId, plugin);
				if (setTarget != sender) {
					sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + roleId);
				}

				return true;
			case help:
				sender.sendMessage(getUsage(sender, command, label));
				return true;
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
			default:
				return false;
		}
	}


	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(RoleSubcommandOptions.values()).map(RoleSubcommandOptions::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0];
		RoleSubcommandOptions option = Arrays.stream(RoleSubcommandOptions.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
	}

	private List<String> handleConfigsTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull RoleSubcommandOptions option) {
		switch (option) {
			case set:
			case guidebook:
				if (args.length <= 1) {
					return Role.getRegistered().keySet().stream()
						.collect(Collectors.toList());
				}

				return null;
			default:
				return null;
		}
	}


	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " role ");

		usage.append('<');
		usage.append(
			Role.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | ") )
		);
		usage.append('>');

		usage.append(' ');

		usage.append('[');
		usage.append(
			Arrays.stream(RoleSubcommandOptions.values()).map(RoleSubcommandOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		usage.append(']');

		usage.append(' ');

		usage.append("[player]");

		return usage.toString();
	}
}
