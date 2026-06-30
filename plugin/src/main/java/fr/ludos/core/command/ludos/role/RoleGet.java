package fr.ludos.core.command.ludos.role;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.role.Role;

public class RoleGet implements Subcommand {
	private final static String id = "get";

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
		OfflinePlayer getTarget = CommandUtility.getOfflinePlayerFromArgsOrSender(args, 0, sender);
		if (getTarget == null) {
			sender.sendMessage(Role.noneLabel); // TODO: Translate
			return true;
		}

		Role.Builder getRole = Role.getPlayerRole(getTarget);
		sender.sendMessage(getRole == null ? Role.noneLabel : getRole.getId());
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