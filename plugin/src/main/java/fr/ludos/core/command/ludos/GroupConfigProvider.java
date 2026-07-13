package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.sectionProvider.ConfigSectionProvider;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupConfigMap;

public final class GroupConfigProvider extends ConfigSectionProvider {
	private final Ludos plugin;
	public GroupConfigProvider(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can configure games through a group.");
			return null;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return null;
		}

		boolean membersCanConfig = GroupConfigMap.membersAuth.getGroupConfig(group).canConfig();
		if (! group.isLeader(player) && ! membersCanConfig) {
			sender.sendMessage("Only the group leader can configure the group.");
			return null;
		}

		return group.getConfig();
	}

	@Override
	public boolean saveConfig(ConfigurationSection config, CommandSender sender) {
		Group.saveConfigGroups(plugin);
		plugin.saveGroups();
		return true;
	}

}
