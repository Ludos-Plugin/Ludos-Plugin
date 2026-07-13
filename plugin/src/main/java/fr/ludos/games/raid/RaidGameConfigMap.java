package fr.ludos.games.raid;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.TypedConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;

public final class RaidGameConfigMap extends ConfigHashMap {
	public static final TypedConfigEntry<Set<OfflinePlayer>> playersEntry = new TypedConfigEntry<>(
		"players",
		new MultipleGroupPlayerConfigOptions("Players", "all")
	);

	public static final TypedConfigEntry<Integer> wavesEntry = new TypedConfigEntry<>(
		"waves",
		new NumberConfigOption("Number of Waves", 0, true)
	);

	public static final RaidGameConfigMap instance = new RaidGameConfigMap(RaidGame.ID);

	private RaidGameConfigMap(String namespace) {
		super(namespace, Set.of(playersEntry, wavesEntry, WorldBorderArea.configEntry));
	}

	public Set<OfflinePlayer> getPlayers(ConfigurationSection container) {
		return getTypedOptionValue(playersEntry, container);
	}
	public int getWaves(ConfigurationSection container) {
		return getTypedOptionValue(wavesEntry, container);
	}
	public int getArea(ConfigurationSection container) {
		return getTypedOptionValue(WorldBorderArea.configEntry, container);
	}
}