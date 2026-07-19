package fr.ludos.games.arena;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.NumberConfigOptions;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;

/**
 * {@link ConfigOptionsMap} for the {@link ArenaGame}.
 */
public final class ArenaGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> TEAM_1_PLAYERS =
		new MultipleGroupPlayerConfigOptions("Team 1 players", "team_1", "random");

	public static final ValueConfigOptions<Set<OfflinePlayer>> TEAM_2_PLAYERS =
		new MultipleGroupPlayerConfigOptions("Team 2 players", "team_2", "random");

	public static final ValueConfigOptions<Integer> ROUNDS =
		new NumberConfigOptions("Number of Rounds", "rounds", null, 3, true);

	public static final ArenaGameConfigMap INSTANCE = new ArenaGameConfigMap(ArenaGame.ID);

	private ArenaGameConfigMap(String namespace) {
		super(namespace, Set.of(TEAM_1_PLAYERS, TEAM_2_PLAYERS, ArenaModeOption.CONFIG, ROUNDS, WorldBorderArea.CONFIG));
	}
}
