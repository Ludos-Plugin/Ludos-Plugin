package fr.ludos.core.command.ludos.group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.Group.AddPlayerMethod;

/**
 * {@link Subcommand} to invite players to the current {@link Group}, as the Group Leader, or an explicitly allowed member.
 */
public class GroupInvite implements Subcommand {
	private final static String ID = "invite";

	private final Ludos ludos;
	public GroupInvite(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Invite a player to your group.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can invite others to groups.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		List<Player> targets = CommandUtility.getPlayersFromArgs(args, sender).stream()
			.filter(p -> ! group.isPlayer(p))
			.collect(Collectors.toCollection(ArrayList::new));

		if (targets.isEmpty()) {
			sender.sendMessage("No valid player names provided.");
			return true;
		}

		boolean membersCanInvite = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canInvite();
		if (! group.isLeader(player) && ! membersCanInvite) {
			sender.sendMessage("Only the group leader can invite new members.");
			return true;
		}

		boolean hasJoined = false;
		for (Player target : targets.stream().toList()) {
			switch (group.requestAddPlayer(target, AddPlayerMethod.Invite)) {
				case Succeeded:
					hasJoined = true;
				case Failed:
					targets.remove(target);
					break;
				default:
					break;
			}
		}
		if (hasJoined) {
			ludos.saveGroupsConfig();
		}

		if (targets.size() > 0) {
			player.sendMessage("Invited " + targets.stream().map(Player::getName).collect(Collectors.joining(", ")) + " to the group.");
		}

		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (! (sender instanceof Player player)) return null;

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) return null;

		HashSet<Player> onlines = ludos.getServer().getOnlinePlayers()
			.stream()
			.collect(Collectors.toCollection(HashSet::new));
		onlines.removeAll(group.getPlayers());

		return onlines.stream()
			.map(Player::getName)
			.toList();
	}
	@Override
	public String getUsage() {
		return "[player1] [player2] ...";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}