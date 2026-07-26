package fr.ludos.core.persistence.config.sectionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeOperation;
import fr.ludos.core.persistence.config.ConfigRoot;

/**
 * Collection of {@link ConfigSectionProvider}s, with their corresponding {@link ConfigNode}.
 */
public abstract class ConfigSectionCollection {
	public abstract @NotNull Set<String> getProviderKeys(CommandSender sender);
	public abstract @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender);
	public abstract @NotNull ConfigRoot getOptions(String key, CommandSender sender);

	public final boolean execute(Plugin plugin, @NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigSectionProvider provider = getProvider(key, sender);
		if (provider == null) return true;

		ConfigSectionContext context = new ConfigSectionContext(provider, plugin);

		ConfigRoot root = getOptions(key, sender);

		if (root.execute(Arrays.copyOfRange(args, 1, args.length), sender, context, mode) && mode != ConfigNodeOperation.get) {
			provider.saveConfig();
		}
		return true;
	}

	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender, ConfigNodeOperation mode) {
		if (args.length <= 1) {
			return getProviderKeys(sender).stream().toList();
		}

		String key = args[0];

		ConfigRoot root = getOptions(key, sender);

		return root.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender, mode);
	}
}
