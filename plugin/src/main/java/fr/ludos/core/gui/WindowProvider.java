package fr.ludos.core.gui;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.security.AccessAuthorization;
import xyz.xenondevs.invui.window.Window;

/**
 * Interface for classes which provide a visual interface via the use of {@link Window}s.
 */
public interface WindowProvider extends AccessAuthorization, Named {
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
	 * @param plugin The plugin for which the window is created
	 * @return true if the window was opened successfully, false otherwise
	 */
	public default boolean openWindow(Player player, Plugin plugin) {
		return openWindow(player, GuiContext.of(plugin, this));
	}
	/**
	 * Opens a {@link Window} for the given {@link Player}, under the given {@link GuiContext}.
	 * @param player The player for which the window is opened
	 * @param context The context in which the window is opened
	 * @return true if the window was opened successfully, false otherwise
	 */
	public default boolean openWindow(Player player, GuiContext context) {
		AccessAuthorization authz = getAccessAuthorization();
		if (authz != null && ! authz.checkAuthorizationNotify(player)) return false;
		if (! context.checkAuthorizationNotify(player)) return false;

		Window window = window(player, context);
		if (window == null) return false;

		window.open();
		return true;
	}

	/**
	 * Optional {@link AccessAuthorization} instance, to intrinsically link the WindowProvider and its Authorization system.
	 * @return an {@link AccessAuthorization} instance, or null to pass-through.
	 */
	public default @Nullable AccessAuthorization getAccessAuthorization() {
		return null;
	}

	@Override
	default @Nullable String getAccessError(CommandSender sender) {
		AccessAuthorization authz = getAccessAuthorization();
		if (authz == null) {
			return null;
		}
		return authz.getAccessError(sender);
	}

	public static void playDenySound(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.1f, 0.8f);
	}
	public static void playClickSound(Player player) {
		player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, 0.1f, 0.7f);
	}
}
