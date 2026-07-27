package fr.ludos.core.persistence.config;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigRoot {
	public Component name();
	public default Component displayName() {
		return name()
			.decoration(TextDecoration.ITALIC, false);
	}

	public @NotNull Set<@NotNull String> options(CommandSender sender);
}
