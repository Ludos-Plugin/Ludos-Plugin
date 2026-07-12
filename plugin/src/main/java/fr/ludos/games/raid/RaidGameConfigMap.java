package fr.ludos.games.raid;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;

public final class RaidGameConfigMap extends ConfigMap {
	public static final String playersOptionsKey = "players";
	public static final MultipleGroupPlayerConfigOptions playersOptions = new MultipleGroupPlayerConfigOptions("Players", "all");

	public static final String wavesOptionsKey = "waves";
	public static final NumberConfigOption wavesOptions = new NumberConfigOption("Number of Waves", true);

	public static final String areaOptionsKey = "area";
	public static final NumberConfigOption areaOptions = WorldBorderArea.configOption;

	public static final RaidGameConfigMap instance = new RaidGameConfigMap(RaidGame.ID);

	private RaidGameConfigMap(String namespace) {
		super(namespace);
	}

	@Override
	public @NotNull Set<@NotNull String> getValues() {
		return Set.of(playersOptionsKey, wavesOptionsKey, areaOptionsKey);
	}

	@Override
	public ConfigOptions<?> getOptions(String name) {
		switch (name) {
			case playersOptionsKey: return playersOptions;
			case wavesOptionsKey: return wavesOptions;
			case areaOptionsKey: return areaOptions;
			default: return null;
		}
	}

	public Set<OfflinePlayer> getPlayers(ConfigurationSection container) {
		return getTypedOptionValue(playersOptionsKey, playersOptions, container);
	}
	public int getWaves(ConfigurationSection container) {
		return getTypedOptionValue(wavesOptionsKey, wavesOptions, container);
	}
	public int getArea(ConfigurationSection container) {
		return getTypedOptionValue(areaOptionsKey, areaOptions, container);
	}
}