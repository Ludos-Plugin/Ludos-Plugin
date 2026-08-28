package fr.ludos.core.security.group;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.security.AccessAuthorization;

/**
 * {@link AccessAuthorization} implementation to authorize access to a Group's management functionality.
 */
public class GroupManageAuthz implements AccessAuthorization {
	private final GroupManager manager;

	public GroupManageAuthz(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public @Nullable String getAccessError(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			return "Only players can configure through a group.";
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return "You are not in a group.";
		}

		boolean membersCanManage = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canManage();
		if (! group.isLeader(player) && ! membersCanManage) {
			return "Only the group leader can manage the group.";
		}

		return null;
	}
}