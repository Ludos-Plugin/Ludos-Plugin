package fr.ludos.core.command.ludos.role;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * {@link Subcommand} for setting a Player's own role.
 */
public class RoleSet implements Subcommand {
	private final static String ID = "set";

	private final RoleManager manager;
	public RoleSet(RoleManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Set a player's role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		String roleId = args[0].toLowerCase();
		Role.Builder setRole = manager.getRoleById(roleId);
		if (setRole == null) {
			sender.sendMessage("Role not found: " + roleId);
			return true;
		}

		switch (args.length) {
			case 1:
				if (! (sender instanceof Player player)) {
					sender.sendMessage("Only players can have roles");
					return true;
				}

				manager.setRole(player, roleId);
				break;
			case 2:
				OfflinePlayer target = CommandUtility.getOfflinePlayerFromArg(args, 1);

				if (manager.isAuthorizedToEditRole(sender, target)) {
					manager.setRole(target, roleId);
					if (sender != target) {
						sender.sendMessage("The role of Player " + target.getName() + " is now " + roleId);
					}
				} else {
					sender.sendMessage("You are not authorized to reset this player's role");
				}
				return true;
			default:
				return false;
		}

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return manager.getRegistered().keySet().stream()
				.sorted()
				.collect(Collectors.toList());

		if (args.length == 2)
			return CommandUtility.getOnlinePlayerNames();

		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<role_id> [player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}