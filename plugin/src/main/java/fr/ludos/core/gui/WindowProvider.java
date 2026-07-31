package fr.ludos.core.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import xyz.xenondevs.invui.window.Window;

/**
 * Interface for classes which provide a visual interface via the use of {@link Window}s.
 */
public interface WindowProvider extends Named {
	public Window configWindow(Player player, GuiContext context);
	public default boolean openConfigWindow(Player player, GuiContext context) {
		if (! context.checkAuthorizationNotify(player)) return false;

		Window window = configWindow(player, context);
		if (window == null) return false;

		window.open();
		return true;
	}

	public static void playDenySound(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.1f, 0.8f);
	}
	public static void playClickSound(Player player) {
		player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, 0.1f, 0.7f);
	}
}
