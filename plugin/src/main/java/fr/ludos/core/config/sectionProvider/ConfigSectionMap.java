package fr.ludos.core.config.sectionProvider;

import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.config.ConfigOptions;

public class ConfigSectionMap extends ConfigSectionCollection {
	private final Map<String, ConfigSectionProvider> map;
	public ConfigSectionMap(ConfigOptions options, Map<String, ConfigSectionProvider> map) {
		super(options);
		this.map = map;
	}

	@Override
	public @NotNull Set<String> getProviderKeys(CommandSender sender) {
		return map.keySet();
	}

	@Override
	public @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender) {
		return map.get(key);
	}

}
