package fr.ludos.core.security.group;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.security.AccessAuthorization;

/**
 * {@link AccessAuthorization} implementation to authorize access to a Group's invite functionality.
 */
public class GroupInviteAuthz implements AccessAuthorization {
	private final GroupManager manager;

	public GroupInviteAuthz(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public @Nullable String getAccessError(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			return "Only players can invite others to a group.";
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return "You are not in a group.";
		}

		boolean membersCanInvite = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canInvite();
		if (! group.isLeader(player) && ! membersCanInvite) {
			return "Only the group leader can invite players to the group.";
		}

		return null;
	}
}