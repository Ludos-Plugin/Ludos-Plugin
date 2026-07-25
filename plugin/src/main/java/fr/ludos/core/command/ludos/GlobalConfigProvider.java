package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionProvider;

/**
 * {@link ConfigSectionProvider} to scope subsequent {@link ConfigNode} within the Global Ludos config ({@link Ludos#getConfig}).
 */
public final class GlobalConfigProvider implements ConfigSectionProvider {
	private final Ludos ludos;
	public GlobalConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		return ludos.getConfig();
	}

	@Override
	public boolean isAuthorized(CommandSender sender, ConfigNodeOperation op) {
		if (op == ConfigNodeOperation.get) return true;

		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only Server Operators are allowed to globally configure Ludos.");
			return false;
		}
		if (! player.isOp()) {
			sender.sendMessage("Only Server Operators are allowed to globally configure Ludos.");
			return false;
		}

		return true;
	}

	@Override
	public boolean saveConfig() {
		ludos.saveConfig();
		return true;
	}
}
