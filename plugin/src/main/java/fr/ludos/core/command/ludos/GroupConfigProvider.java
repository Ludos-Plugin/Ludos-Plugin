package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigNode} within the Group's config ({@link Group#getConfig()}).
 */
public final class GroupConfigProvider implements ConfigSectionProvider {
	private final GroupManager manager;
	public GroupConfigProvider(GroupManager manager) {
		this.manager = manager;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can configure through a group.");
			return null;
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return null;
		}

		return group.getScopedConfig();
	}

	@Override
	public boolean isAuthorized(CommandSender sender, ConfigNodeOperation op) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can configure through a group.");
			return false;
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return false;
		}

		if (op == ConfigNodeOperation.get) return true;

		boolean membersCanConfig = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return false;
		}

		return true;
	}

	@Override
	public boolean saveConfig() {
		manager.saveConfig();
		return true;
	}

}
