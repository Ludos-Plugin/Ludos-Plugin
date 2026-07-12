package fr.ludos.games.arena;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;

public final class ArenaGameConfigMap extends ConfigHashMap {
	public static final ConfigEntry<Set<OfflinePlayer>> team1PlayersEntry = new ConfigEntry<>(
		"team_1",
		new MultipleGroupPlayerConfigOptions("Team 1 players", "random")
	);

	public static final ConfigEntry<Set<OfflinePlayer>> team2PlayersEntry = new ConfigEntry<>(
		"team_2",
		new MultipleGroupPlayerConfigOptions("Team 2 players", "random")
	);

	public static final ConfigEntry<ArenaModeOption> arenaModeEntry = new ConfigEntry<>(
		"arena_mode",
		ArenaModeOption.configOptions
	);

	public static final ConfigEntry<Integer> roundsEntry = new ConfigEntry<>(
		"rounds",
		new NumberConfigOption("Number of Rounds", true)
	);

	public static final ArenaGameConfigMap instance = new ArenaGameConfigMap(ArenaGame.ID);

	private ArenaGameConfigMap(String namespace) {
		super(namespace, Set.of(team1PlayersEntry, team2PlayersEntry, arenaModeEntry, roundsEntry, WorldBorderArea.configEntry));
	}

	public Set<OfflinePlayer> getTeam1Players(ConfigurationSection container) {
		return getTypedOptionValue(team1PlayersEntry, container);
	}
	public Set<OfflinePlayer> getTeam2Players(ConfigurationSection container) {
		return getTypedOptionValue(team2PlayersEntry, container);
	}
	public ArenaModeOption getMode(ConfigurationSection container) {
		return getTypedOptionValue(arenaModeEntry, container);
	}
	public int getRounds(ConfigurationSection container) {
		return getTypedOptionValue(roundsEntry, container);
	}
	public int getAreaSize(ConfigurationSection container) {
		return getTypedOptionValue(WorldBorderArea.configEntry, container);
	}
}
