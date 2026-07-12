package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.config.SingleGroupPlayerConfigOptions;

public final class ManhuntGameConfigMap extends ConfigHashMap {
	public static final ConfigEntry<Set<OfflinePlayer>> playersEntry = new ConfigEntry<>(
		"players",
		new MultipleGroupPlayerConfigOptions("Players", "all")
	);

	public static final ConfigEntry<OfflinePlayer> preyEntry = new ConfigEntry<>(
		"prey",
		new SingleGroupPlayerConfigOptions("Prey Player", "random")
	);

	public static final ConfigEntry<Integer> revealEntry = new ConfigEntry<>(
		"reveal",
		new NumberConfigOption("Reveal period duration seconds", Set.of(60, 120, 180, 240, 300, 360), 60, true)
	);

	public static final ManhuntGameConfigMap instance = new ManhuntGameConfigMap(ManhuntGame.ID);

	private ManhuntGameConfigMap(String namespace) {
		super(namespace, Set.of(playersEntry, preyEntry, WorldBorderArea.configEntry, revealEntry));
	}

	public Set<OfflinePlayer> getPlayers(ConfigurationSection container) {
		return getTypedOptionValue(playersEntry, container);
	}
	public OfflinePlayer getPrey(ConfigurationSection container) {
		return getTypedOptionValue(preyEntry, container);
	}
	public int getArea(ConfigurationSection container) {
		return getTypedOptionValue(WorldBorderArea.configEntry, container);
	}
	public int getRevealPeriod(ConfigurationSection container) {
		return getTypedOptionValue(revealEntry, container);
	}
}