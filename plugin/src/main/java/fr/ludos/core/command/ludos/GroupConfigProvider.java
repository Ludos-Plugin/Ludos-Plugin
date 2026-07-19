package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.config.sectionProvider.ConfigSectionProvider;
import fr.ludos.core.group.Group;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigOptions} within the Group's config ({@link Group#getConfigSection()}).
 */
public final class GroupConfigProvider extends ConfigSectionProvider {
	private final Ludos ludos;
	public GroupConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can configure through a group.");
			return null;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return null;
		}

		boolean membersCanConfig = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return null;
		}

		return group.getConfig();
	}

	@Override
	public boolean saveConfig(ConfigurationSection config, CommandSender sender) {
		ludos.saveGroupsConfig();
		return true;
	}

}
