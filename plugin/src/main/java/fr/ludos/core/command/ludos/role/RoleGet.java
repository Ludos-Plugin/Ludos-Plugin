package fr.ludos.core.command.ludos.role;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * {@link Subcommand} to get a Player's current {@link Role} if it was set, or "none".
 */
public class RoleGet implements Subcommand {
	private final static String ID = "get";

	private final RoleManager manager;

	public RoleGet(RoleManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Get a Player's role.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		OfflinePlayer getTarget = CommandUtility.getOfflinePlayerFromArgsOrSender(args, 0, sender);

		Role.Builder getRole = manager.getPlayerRole(getTarget);
		sender.sendMessage(getRole == null ? Role.NONE_LABEL : getRole.getId());
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