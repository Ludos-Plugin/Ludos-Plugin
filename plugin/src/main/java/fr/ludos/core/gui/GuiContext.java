package fr.ludos.core.gui;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import fr.ludos.core.security.AccessAuthorization;

/**
 * Context for GUI operations, used to manage the state and configuration of nested GUI interactions.<br>
 * This provides the ability to maintain a stack of GUI contexts, allowing for modal-like navigation between windows.
 */
public class GuiContext implements AccessAuthorization {
	private final @Nonnull Plugin plugin;
	private final @Nullable GuiContext previous;
	private @Nullable WindowProvider window;
	private @Nullable AccessAuthorization auth;
	private @Nullable ConfigSectionContext configContext;

	private GuiContext(
		@Nonnull Plugin plugin,
		@Nullable GuiContext previous,
		@Nullable WindowProvider window,
		@Nullable AccessAuthorization auth,
		@Nullable ConfigSectionContext configContext
	) {
		this.plugin = Objects.requireNonNull(plugin);
		this.previous = previous;
		this.window = window;
		this.auth = auth;
		this.configContext = configContext;
	}

	public static final GuiContext of(Plugin plugin, WindowProvider window, @Nullable AccessAuthorization auth) {
		return new GuiContext(plugin, null, window, auth, null);
	}
	public static final GuiContext of(Plugin plugin, WindowProvider window) {
		return of(plugin, window, null);
	}
	public static final GuiContext ofConfig(Plugin plugin, WindowProvider window, ConfigSectionContext config) {
		return new GuiContext(plugin, null, window, null, config);
	}

	public GuiContext deeper() {
		return new GuiContext(plugin, this, null, auth, configContext);
	}
	public GuiContext deeper(@Nullable WindowProvider window) {
		return new GuiContext(plugin, this, window, auth, configContext);
	}
	public GuiContext deeper(@Nullable AccessAuthorization auth) {
		return new GuiContext(plugin, this, null, auth, configContext);
	}
	public GuiContext deeper(@Nullable String path) {
		return new GuiContext(plugin, this, null, auth, configContext != null ? configContext.getDeeper(path) : configContext);
	}
	public GuiContext deeper(ConfigSectionContext configContext) {
		return new GuiContext(plugin, this, null, auth, Objects.requireNonNull(configContext));
	}

	public GuiContext withWindow(WindowProvider window) {
		return new GuiContext(plugin, previous, window, auth, configContext);
	}

	public GuiContext setWindow(WindowProvider window) {
		this.window = window;
		return this;
	}
	public final GuiContext setConfig(ConfigSectionContext configContext) {
		this.configContext = configContext;
		return this;
	}
	public final GuiContext setAccessAuth(AccessAuthorization auth) {
		this.auth = auth;
		return this;
	}


	public final Plugin plugin() {
		return this.plugin;
	}
	public final @Nullable ConfigSectionContext configContext() {
		return this.configContext;
	}

	public boolean openWindow(Player player) {
		if (window == null) return false;

		final GuiContext thiz = this;
		new BukkitRunnable() {
			public void run() {
				if (! window.openWindow(player, thiz)) {
					WindowProvider.playDenySound(player);
				}
			}
		}.runTaskLater(plugin, 0);
		return true;
	}

	public boolean openPreviousWindow(Player player) {
		if (previous == null) return false;

		return previous.openWindow(player);
	}

	@Override
	public @Nullable String getAccessError(CommandSender sender) {
		if (auth != null) {
			String authError = auth.getAccessError(sender);
			if (authError != null) return authError;
		}
		if (configContext != null) {
			String configError = configContext.getAccessError(sender);
			if (configError != null) return configError;
		}
		return null;
	}
}
