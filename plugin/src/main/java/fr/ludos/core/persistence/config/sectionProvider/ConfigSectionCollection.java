package fr.ludos.core.persistence.config.sectionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.persistence.config.ConfigEntry;

/**
 * Collection of {@link ConfigSectionProvider}s, with their corresponding {@link ConfigEntry}.
 */
public abstract class ConfigSectionCollection {
	public abstract @NotNull Set<String> getProviderKeys(CommandSender sender);
	public abstract @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender);
	public abstract @NotNull ConfigEntry getOptions(String key, CommandSender sender);

	public final boolean exec(@NotNull String[] args, CommandSender sender) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigSectionProvider provider = getProvider(key, sender);
		if (provider == null) return true;

		ConfigurationSection config = provider.getConfig(sender);
		if (config == null) return true;

		ConfigEntry options = getOptions(key, sender);

		boolean success = options.execute(Arrays.copyOfRange(args, 1, args.length), sender, config);
		if (success) {
			provider.saveConfig(config, sender);
		}
		return true;
	}

	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getProviderKeys(sender).stream().toList();
		}

		String key = args[0];

		ConfigEntry options = getOptions(key, sender);

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
}
