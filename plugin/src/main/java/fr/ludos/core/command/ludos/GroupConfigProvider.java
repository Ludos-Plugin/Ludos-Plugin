package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.config.ConfigNode;
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
	public String getValidationError(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			return "Only players can configure through a group.";
		}

		Group group = manager.getGroupOfPlayer(player);
		if (group == null) {
			return "You are not in a group.";
		}

		boolean membersCanConfig = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			return "Only the group leader can configure the group.";
		}

		return null;
	}

	@Override
	public boolean saveConfig() {
		manager.saveConfig();
		return true;
	}

}
