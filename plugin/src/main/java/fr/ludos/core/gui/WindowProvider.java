package fr.ludos.core.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import xyz.xenondevs.invui.window.Window;

/**
 * Interface for classes which provide a visual interface via the use of {@link Window}s.
 */
public interface WindowProvider extends Named {
	/**
	 * Creates a {@link Window} for the given {@link Player}, under the given {@link GuiContext}.
	 * @param player The player for which the window is created
	 * @param context The context in which the window is created
	 * @return The created window, or null if the window could not be created
	 * (for example, if the player does not have permission to view the window)
	 */
	public Window window(Player player, GuiContext context);
	/**
	 * Opens a {@link Window} for the given {@link Player}, under the given {@link GuiContext}.
	 * @param player The player for which the window is opened
	 * @param context The context in which the window is opened
	 * @return true if the window was opened successfully, false otherwise
	 */
	public default boolean openWindow(Player player, GuiContext context) {
		if (! context.checkAuthorizationNotify(player)) return false;

		Window window = window(player, context);
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
