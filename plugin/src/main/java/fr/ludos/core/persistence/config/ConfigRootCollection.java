package fr.ludos.core.persistence.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.sectionProvider.ConfigSectionContext;

/**
 * {@link ConfigRoot} implemented as a Collection of sub-{@link ConfigNode}s.
 */
public abstract class ConfigRootCollection implements ConfigRoot {
	public abstract @Nullable ConfigNode getEntry(String key);

	public final @NotNull Set<@NotNull String> getEntryOptions(String key, CommandSender sender) {
		ConfigNode node = getEntry(key);
		if (node == null) return Collections.emptySet();

		return node.options(sender);
	}

	@Override
	public boolean execute(@NotNull String[] args, CommandSender sender, ConfigSectionContext context, ConfigNodeOperation mode) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigNode node = getEntry(key);
		if (node == null) return false;

		return node.execute(Arrays.copyOfRange(args, 1, args.length), sender, context, mode);
	}

	@Override
	public @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return options(sender).stream().toList();
		}

		ConfigNode node = getEntry(args[0]);
		if (node == null) return null;

		return node.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender, mode);
	}
}
