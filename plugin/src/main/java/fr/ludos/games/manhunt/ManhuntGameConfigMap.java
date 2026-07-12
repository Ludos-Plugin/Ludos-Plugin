package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.config.SingleGroupPlayerConfigOptions;

public final class ManhuntGameConfigMap extends ConfigMap {
	public static final String playersOptionsKey = "players";
	public static final MultipleGroupPlayerConfigOptions playersOptions = new MultipleGroupPlayerConfigOptions("Players", "all");

	public static final String preyOptionsKey = "prey";
	public static final SingleGroupPlayerConfigOptions preyOptions = new SingleGroupPlayerConfigOptions("Prey Player", "random");

	public static final String areaOptionsKey = "area";
	public static final NumberConfigOption areaOptions = WorldBorderArea.configOption;

	public static final String revealOptionsKey = "reveal";
	public static final NumberConfigOption revealOptions = new NumberConfigOption("Reveal period duration seconds", Set.of(60, 120, 180, 240, 300, 360), 60, true);

	public static final ManhuntGameConfigMap instance = new ManhuntGameConfigMap(ManhuntGame.ID);

	private ManhuntGameConfigMap(String namespace) {
		super(namespace);
	}

	@Override
	public @NotNull Set<@NotNull String> getValues() {
		return Set.of(playersOptionsKey, preyOptionsKey, areaOptionsKey, revealOptionsKey);
	}

	@Override
	public ConfigOptions<?> getOptions(String name) {
		switch (name) {
			case playersOptionsKey: return playersOptions;
			case preyOptionsKey: return preyOptions;
			case areaOptionsKey: return areaOptions;
			case revealOptionsKey: return revealOptions;
			default: return null;
		}
	}

	public Set<OfflinePlayer> getPlayers(ConfigurationSection container) {
		return getTypedOptionValue(playersOptionsKey, playersOptions, container);
	}
	public OfflinePlayer getPrey(ConfigurationSection container) {
		return getTypedOptionValue(preyOptionsKey, preyOptions, container);
	}
	public int getArea(ConfigurationSection container) {
		return getTypedOptionValue(areaOptionsKey, areaOptions, container);
	}
	public int getRevealPeriod(ConfigurationSection container) {
		return getTypedOptionValue(revealOptionsKey, revealOptions, container);
	}
}