package fr.ludos.core.command.ludos;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.ludos.core.Ludos;
import fr.ludos.core.config.sectionProvider.ConfigSectionProvider;

public final class PlayerConfigProvider extends ConfigSectionProvider {
	private final Ludos ludos;
	public PlayerConfigProvider(Ludos ludos) {
		this.ludos = ludos;
	}

	@Override
	public ConfigurationSection getConfig(CommandSender sender) {
		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can set player configuration.");
			return null;
		}

		return ludos.getPlayerScopedConfig(player);
	}

	@Override
	public boolean saveConfig(ConfigurationSection config, CommandSender sender) {
		ludos.savePlayersConfig();
		return true;
	}

}
