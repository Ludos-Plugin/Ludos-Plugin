package fr.ludos.games.arena;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOptions;
import fr.ludos.core.config.ValueConfigOptions;

public final class ArenaGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> team1PlayersConfig =
		new MultipleGroupPlayerConfigOptions("Team 1 players", "team_1", "random");

	public static final ValueConfigOptions<Set<OfflinePlayer>> team2PlayersConfig =
		new MultipleGroupPlayerConfigOptions("Team 2 players", "team_2", "random");

	public static final ValueConfigOptions<Integer> roundsEntryConfig =
		new NumberConfigOptions("Number of Rounds", "rounds", null, 3, true);

	public static final ArenaGameConfigMap instance = new ArenaGameConfigMap(ArenaGame.ID);

	private ArenaGameConfigMap(String namespace) {
		super(namespace, Set.of(team1PlayersConfig, team2PlayersConfig, ArenaModeOption.config, roundsEntryConfig, WorldBorderArea.config));
	}
}
