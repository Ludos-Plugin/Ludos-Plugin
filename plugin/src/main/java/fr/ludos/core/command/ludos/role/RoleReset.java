package fr.ludos.core.command.ludos.role;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.RoleManager;

/**
 * {@link Subcommand} for resetting a Player's own role.
 */
public class RoleReset implements Subcommand {
	private final static String ID = "reset";

	private final RoleManager manager;
	public RoleReset(RoleManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Reset a player's role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		OfflinePlayer target = CommandUtility.getOfflinePlayerFromArgsOrSender(args, 0, sender);
		if (target == null) {
			sender.sendMessage("Could not find Player");
			return true;
		}

		if (manager.isAuthorizedToEditRole(sender, target)) {
			if (manager.getPlayerRole(target) == null) return true;

			sender.sendMessage(
				sender == target ?
				"Your role was reset" :
				"The role of player " + target.getName() + " was reset"
			);
			manager.unsetRole(target);
		} else {
			sender.sendMessage("You are not authorized to reset this player's role");
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
	public String getUsage(@NotNull CommandSender sender) {
		return "[player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}