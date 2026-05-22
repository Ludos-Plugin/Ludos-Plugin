package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.command.CommandUtility;
import fr.ludos.command.Subcommand;
import fr.ludos.role.Role;

public enum RoleSubcommand implements Subcommand {
	get() {
		@Override
		public String getDescription() {
			return "Get a Player's role.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			Player getTarget = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
			if (getTarget == null) {
				sender.sendMessage(noRoleLabel); // TODO: Translate
				return true;
			}

			Role.Builder getRole = Role.getPlayerRole(getTarget);
			sender.sendMessage(getRole == null ? noRoleLabel : getRole.getId());
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
	},
	set() {
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
				return false;
			}

			Player setTarget = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (setTarget == null) {
				sender.sendMessage("Player not found.");
				return false;
			}

			Role.setRole(setTarget, roleId);
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
	},
	config() {
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
				return false;
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
	},
	guidebook() {
		@Override
		public String getDescription() {
			return "Give the guidebook for a role to a player.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) return false;

			String guidebookRoleId = args[0].toLowerCase();
			Role.Builder guidebookRole = Role.getRoleById(guidebookRoleId);
			if (guidebookRole == null) return false;

			Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
			if (player == null) return false;

			ItemStack book = guidebookRole.createGuidebook();
			player.getInventory().addItem(book);
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
			return "<" +
				Role.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
				+ "> [player]";
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	},
	reset() {
		@Override
		public String getDescription() {
			return "Reset a player's role.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			Player target = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
			Role.removeRole(target);

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
	},
	help() {
		@Override
		public String getDescription() {
			return "Show help for role commands.";
		}
		@Override
		public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length < 1) {
				sender.sendMessage(getUsage());
				return true;
			}

			String arg = args[0].toLowerCase();
			RoleSubcommand option = Arrays.stream(RoleSubcommand.values())
				.filter(o -> o != help)
				.filter(o -> o.name().equals(arg))
				.findFirst().orElse(null);
			if (option == null) return false;

			sender.sendMessage(option.getUsage());
			return true;
		}
		@Override
		public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			if (args.length == 1) {
				return Arrays.stream(RoleSubcommand.values())
					.filter(o -> o != help)
					.map(RoleSubcommand::name)
					.collect(Collectors.toList());
			}
			return null;
		}
		@Override
		public String getUsage() {
			StringBuilder usage = new StringBuilder();
			usage.append('<');
			usage.append(
				Arrays.stream(RoleSubcommand.values())
					.filter(o -> o != help)
					.map(RoleSubcommand::name)
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');

			usage.append(' ');

			usage.append('<');
			usage.append(
				Role.getRegistered().keySet().stream().sorted()
					.collect(Collectors.joining(" | "))
			);
			usage.append('>');

			usage.append(' ');

			usage.append("[option]");

			return usage.toString();
		}
		@Override
		public boolean requireOp() {
			return false;
		}
	};

	public static final String arg = "role";
	private static final String noRoleLabel = "none";
}