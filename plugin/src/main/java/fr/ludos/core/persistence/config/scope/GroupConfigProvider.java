package fr.ludos.core.persistence.config.scope;

import java.util.Objects;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;
import fr.ludos.core.security.AccessAuthorization;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigNode} within the Group's config ({@link Group#getConfig()}).
 */
public final class GroupConfigProvider implements ConfigSectionProvider {
	private final GroupManager manager;

	public GroupConfigProvider(GroupManager manager) {
		this.manager = Objects.requireNonNull(manager);
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
	public boolean saveConfig() {
		manager.saveData();
		return true;
	}

	@Override
	public AccessAuthorization getAccessAuthorization() {
		return manager.getConfigAuthz();
	}
}