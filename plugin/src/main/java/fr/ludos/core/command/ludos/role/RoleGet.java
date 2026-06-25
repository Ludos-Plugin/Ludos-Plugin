package fr.ludos.core.command.ludos.role;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;

public class RoleGet implements Subcommand {
	private final static String id = "get";
	private static final String noRoleLabel = "none";

	@Override
	public String id() {
		return id;
	}

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
}