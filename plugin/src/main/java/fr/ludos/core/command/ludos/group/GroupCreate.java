package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.JoinMethod;

public class GroupCreate implements Subcommand {
	private final static String id = "create";

	private final Ludos plugin;
	public GroupCreate(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public String id() {
		return id;
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

		Group group = Group.createGroup(player, null, plugin);

		Set<Player> members = CommandUtility.getPlayersFromArgs(args, 0, sender);
		for (Player member : members) {
			group.requestPlayerJoin(member, JoinMethod.Invite);
		}

		plugin.saveGroups();

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