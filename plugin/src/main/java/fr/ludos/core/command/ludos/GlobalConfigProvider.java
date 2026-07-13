package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.sectionProvider.ConfigSectionProvider;

public final class GlobalConfigProvider extends ConfigSectionProvider {
	private final Ludos plugin;
	public GlobalConfigProvider(Ludos plugin) {
		this.plugin = plugin;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (sender instanceof Player player && ! player.isOp()) {
			sender.sendMessage("Only Server Operators are allowed to globally configure Ludos.");
			return null;
		}

		return plugin.getConfig();
	}

	@Override
	public boolean saveConfig(ConfigurationSection config, CommandSender sender) {
		plugin.saveConfig();
		return true;
	}
}
