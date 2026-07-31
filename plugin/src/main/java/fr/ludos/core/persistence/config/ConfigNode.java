package fr.ludos.core.persistence.config;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.gui.GuiContext;
import fr.ludos.core.gui.GuiObject;

/**
 * A structure to represent a Configurable value (ex: The number of waves in a Raid), and its valid values (options).
 */
public interface ConfigNode extends ConfigRoot, GuiObject {
	public @Nullable String key();

	public boolean execute(@NotNull String[] args, CommandSender sender, GuiContext context);

	public default @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		return Collections.emptyList();
	}
}
