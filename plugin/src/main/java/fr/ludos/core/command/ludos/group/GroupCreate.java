package fr.ludos.core.command.ludos.group;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.AddPlayerMethod;

/**
 * {@link Subcommand} to create a new {@link Group}, and join it as leader.
 */
public class GroupCreate implements Subcommand {
	private final static String ID = "create";

	private final Ludos ludos;
	public GroupCreate(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Create a new group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can create groups.");
			return true;
		}

		Group group = Group.createGroup(player, null, ludos);

		List<Player> members = CommandUtility.getPlayersFromArgs(args, sender);
		for (Player member : members) {
			group.requestAddPlayer(member, AddPlayerMethod.Invite);
		}

		ludos.saveGroupsConfig();

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return CommandUtility.getOnlinePlayerNames();
	}
	@Override
	public String getUsage() {
		return "[member1] [member2] ...";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}