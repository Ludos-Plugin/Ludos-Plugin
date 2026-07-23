package fr.ludos.core.persistence.config.sectionProvider;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.persistence.config.ConfigEntry;

/**
 * {@link ConfigSectionCollection} implemented as a Map-like structure of sub-{@link ConfigSectionProvider}s and {@link ConfigEntry}.
 */
public class ConfigSectionMap extends ConfigSectionCollection {
	private final static Pair<ConfigSectionProvider, ConfigEntry> EMPTY_PAIR = Pair.of(null, null);
	private final Map<String, Pair<ConfigSectionProvider, ConfigEntry>> map;

	public ConfigSectionMap(Map<String, Pair<ConfigSectionProvider, ConfigEntry>> map) {
		this.map = map;
	}

	@Override
	public @NotNull Set<String> getProviderKeys(CommandSender sender) {
		return map.keySet();
	}

	@Override
	public @NotNull ConfigSectionProvider getProvider(String key, CommandSender sender) {
		return map.getOrDefault(key, EMPTY_PAIR).getLeft();
	}

	@Override
	public @NotNull ConfigEntry getOptions(String key, CommandSender sender) {
		return map.getOrDefault(key, EMPTY_PAIR).getRight();
	}

}
