package fr.ludos.command;


import fr.ludos.Ludos;
import fr.ludos.role.Role;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.EnumUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RoleCommand implements TabExecutor {

	private static final String randomRole = "random";
	private final Ludos plugin;


	public RoleCommand(Ludos plugin) {
		this.plugin = plugin;
	}


	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) return false;

		String roleString = args[0].toLowerCase();
		if ( ! EnumUtils.isValidEnum(RoleCommandOptions.class, roleString) ) return false;

		RoleCommandOptions config = RoleCommandOptions.valueOf( roleString );

		return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(RoleCommandOptions.values()).map(RoleCommandOptions::toString)
				.sorted()
				.collect(Collectors.toList());
		}

		String optionString = args[0].toLowerCase();
		if (! EnumUtils.isValidEnum(RoleCommandOptions.class, optionString)) return null;

		RoleCommandOptions config = RoleCommandOptions.valueOf( optionString );

		return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
	}

	private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args, RoleCommandOptions config) {
		switch (config) {
			case get:
				Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
				if (getTarget == null) {
					sender.sendMessage(randomRole); // TODO: Translate
					return true;
				}

				Role.Builder role = Role.getRole(getTarget);
				sender.sendMessage(role == null ? randomRole : role.getId());

				return true;
			case reset:
				Player removeTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
				if (removeTarget == null) return false;

				Role.removeRole(removeTarget, plugin);
				if ( removeTarget != sender ) {
					sender.sendMessage("The role of Player " + removeTarget.getName() + " is now randomly chosen");
				}

				return true;
			case set:
				if (args.length == 0) return false;

				String roleString = args[0].toLowerCase();
				if (! Role.getRegistered().containsKey(roleString)) return false;

				Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
				if (setTarget == null) return false;

				Role.setRole(setTarget, roleString, plugin);
				if (setTarget != sender) {
					sender.sendMessage("The role of Player " + setTarget.getName() + " is now " + roleString);
				}

				return true;
			default:
				return false;
		}
	}

	private List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args, RoleCommandOptions config) {
		switch (config) {
			case get:
			case reset:
				return null;
			case set:
				switch (args.length) {
					case 0:
					case 1:
						return Role.getRegistered().keySet().stream()
							.sorted()
							.collect(Collectors.toList());
					default:
						return null;
				}
			default:
				return null;
		}
	}

	public String getUsage() {

		StringBuilder usage = new StringBuilder("/<command> ");

		usage.append('<');
		usage.append( Role.getRegistered().keySet().stream()
						.sorted()
						.collect(Collectors.joining(" | ") ) );
		usage.append('>');

		usage.append(' ');

		usage.append('[');
		usage.append( Arrays.stream(RoleCommandOptions.values()).map(RoleCommandOptions::toString)
						.sorted()
						.collect(Collectors.joining( " | ")) );
		usage.append(']');

		usage.append(' ');

		usage.append("[player]");

		return usage.toString();
	}
}