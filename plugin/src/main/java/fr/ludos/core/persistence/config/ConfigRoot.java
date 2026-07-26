package fr.ludos.core.persistence.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.invui.window.Window;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigRoot {
	public @NotNull Set<@NotNull String> options(CommandSender sender);

	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode);

	public default @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		return Collections.emptyList();
	}

	public Component name();
	public default Component displayName() {
		return name()
			.decoration(TextDecoration.ITALIC, false);
	}
	public Window configWindow(Player player, ConfigSectionContext context);
	public default void openConfigWindow(Player player, ConfigSectionContext context) {
		Window window = configWindow(player, context);
		if (window == null) {
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.1f, 0.8f);
			return;
		}
		window.open();
	}
}
