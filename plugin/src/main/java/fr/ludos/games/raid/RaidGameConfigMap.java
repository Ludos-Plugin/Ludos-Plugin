package fr.ludos.games.raid;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.NumberConfigOptions;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;

/**
 * {@link ConfigOptionsMap} for the {@link RaidGame} game.
 */
public final class RaidGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> PLAYERS =
		new MultipleGroupPlayerConfigOptions("Players", "players", "all");

	public static final ValueConfigOptions<Integer> WAVES =
		new NumberConfigOptions("Number of Waves", "waves", null, 0, true);

	public static final RaidGameConfigMap INSTANCE = new RaidGameConfigMap(RaidGame.ID);

	private RaidGameConfigMap(String namespace) {
		super(namespace, Set.of(PLAYERS, WAVES, WorldBorderArea.CONFIG));
	}
}