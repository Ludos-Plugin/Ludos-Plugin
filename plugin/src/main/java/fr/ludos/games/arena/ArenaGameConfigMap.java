package fr.ludos.games.arena;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.TypedConfigEntry;
import fr.ludos.core.config.ConfigHashMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOption;

public final class ArenaGameConfigMap extends ConfigHashMap {
	public static final TypedConfigEntry<Set<OfflinePlayer>> team1PlayersEntry = new TypedConfigEntry<>(
		"team_1",
		new MultipleGroupPlayerConfigOptions("Team 1 players", "random")
	);

	public static final TypedConfigEntry<Set<OfflinePlayer>> team2PlayersEntry = new TypedConfigEntry<>(
		"team_2",
		new MultipleGroupPlayerConfigOptions("Team 2 players", "random")
	);

	public static final TypedConfigEntry<ArenaModeOption> arenaModeEntry = new TypedConfigEntry<>(
		"arena_mode",
		ArenaModeOption.configOptions
	);

	public static final TypedConfigEntry<Integer> roundsEntry = new TypedConfigEntry<>(
		"rounds",
		new NumberConfigOption("Number of Rounds", 3, true)
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
