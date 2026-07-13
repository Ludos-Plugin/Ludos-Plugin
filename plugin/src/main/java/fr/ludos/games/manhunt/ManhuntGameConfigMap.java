package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.config.SingleGroupPlayerConfigOptions;
import fr.ludos.core.config.TypedConfigEntry;

public final class ManhuntGameConfigMap extends ConfigHashMap {
	public static final TypedConfigEntry<Set<OfflinePlayer>> playersEntry = new TypedConfigEntry<>(
		"players",
		new MultipleGroupPlayerConfigOptions("Players", "all")
	);

	public static final TypedConfigEntry<OfflinePlayer> preyEntry = new TypedConfigEntry<>(
		"prey",
		new SingleGroupPlayerConfigOptions("Prey Player", "random")
	);

	public static final TypedConfigEntry<Integer> revealEntry = new TypedConfigEntry<>(
		"reveal",
		new NumberConfigOption("Reveal period duration seconds", 180, Set.of(60, 120, 180, 240, 300, 360), true)
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