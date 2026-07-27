package fr.ludos.core.persistence.config.sectionProvider;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Utility;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.ConfigRoot;

/**
 * .
 */
public class ConfigSectionContext implements ConfigSectionProvider {
	private final @Nullable ConfigSectionContext previous;
	private final ConfigSectionProvider provider;
	private final Plugin plugin;
	private final String path;
	private ConfigRoot config = null;

	private ConfigSectionContext(@Nullable ConfigSectionContext previous, ConfigSectionProvider provider, Plugin plugin, String path) {
		this.previous = previous;
		this.provider = provider;
		this.plugin = plugin;
		this.path = path;
	}
	public ConfigSectionContext(ConfigSectionProvider provider, Plugin plugin, ConfigRoot currentConfig) {
		this(null, provider, plugin, null);
		this.config = currentConfig;
	}
	public ConfigSectionContext(ConfigSectionProvider provider, Plugin plugin) {
		this(null, provider, plugin, null);
	}

	public ConfigSectionContext getDeeper(@Nullable String path, ConfigRoot currentConfig) {
		this.config = currentConfig;

		String finalPath = path == null
			? this.path
			: this.path == null
				? path
				: this.path + '.' + path;
		return new ConfigSectionContext(this, provider, plugin, finalPath);
	}

	public void openWindow(Player player) {
		if (config == null) return;

		final ConfigSectionContext thiz = this;
		new BukkitRunnable() {
			public void run() {
				if (config instanceof ConfigNode node) {
					node.openConfigWindow(player, thiz);
				} else if (config instanceof ConfigSectionCollection root) {
					root.openConfigWindow(player, plugin);
				}
			}
		}.runTaskLater(plugin, 0);
	}
	public void openPreviousWindow(Player player) {
		if (previous == null) return;

		previous.openWindow(player);
	}

	public Plugin plugin() {
		return plugin;
	}


	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		return Utility.getOrCreateConfigSection(provider.getConfig(sender), path);
	}

	@Override
	public boolean isAuthorized(CommandSender sender, ConfigNodeOperation op) {
		return provider.isAuthorized(sender, op);
	}

	@Override
	public boolean saveConfig() {
		return provider.saveConfig();
	}
}