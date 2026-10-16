package fr.ludos.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.WindowProvider;

/**
 * A NullCommand that opens a GUI window when executed. This is used as a fallback for SubcommandManagers when no Subcommand is specified.
 */
public final class GuiCommand implements NullCommand {
	private final Plugin plugin;
	private final WindowProvider windowProvider;

	public GuiCommand(Plugin plugin, WindowProvider windowProvider) {
		this.plugin = plugin;
		this.windowProvider = windowProvider;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		if (sender instanceof Player player) {
			if (! windowProvider.openWindow(player, plugin)) {
				WindowProvider.playDenySound(player);
			}
			return true;
		}
		return false;
	}

}
