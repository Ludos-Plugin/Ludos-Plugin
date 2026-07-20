package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.sectionProvider.ConfigSectionProvider;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigOptions} within the Global Ludos config ({@link Ludos#getConfig}).
 */
public final class GlobalConfigProvider extends ConfigSectionProvider {
	private final Ludos ludos;
	public GlobalConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (sender instanceof Player player && ! player.isOp()) {
			sender.sendMessage("Only Server Operators are allowed to globally configure Ludos.");
			return null;
		}

		return ludos.getConfig();
	}

	@Override
	public boolean saveConfig(ConfigurationSection config, CommandSender sender) {
		ludos.saveConfig();
		return true;
	}
}
