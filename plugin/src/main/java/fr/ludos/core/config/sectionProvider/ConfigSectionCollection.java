package fr.ludos.core.config.sectionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.config.ConfigOptions;

public abstract class ConfigSectionCollection {
	private final @NotNull ConfigOptions options;
	public ConfigSectionCollection(@NotNull ConfigOptions options) {
		this.options = Objects.requireNonNull(options);
	}
	public abstract @NotNull Set<String> getProviderKeys(CommandSender sender);
	public abstract @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender);

	public final boolean exec(@NotNull String[] args, CommandSender sender) {
		if (args.length == 0) return false;

		String key = args[0];
		ConfigSectionProvider provider = getProvider(key, sender);
		if (provider == null) return true;

		ConfigurationSection config = provider.getConfig(sender);
		if (config == null) return true;

		boolean success = options.set(Arrays.copyOfRange(args, 1, args.length), sender, config);
		if (success) {
			provider.saveConfig(config, sender);
		}
		return true;
	}

	public final @Nullable List<@NotNull String> tabComplete(@NotNull String[] args, CommandSender sender) {
		if (args.length <= 1) {
			return getProviderKeys(sender).stream().toList();
		}

		return options.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
	}
}
