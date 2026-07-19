package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.NumberConfigOptions;
import fr.ludos.core.config.valueOptions.SingleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;

/**
 * {@link ConfigOptionsMap} for the {@link ManhuntGame}.
 */
public final class ManhuntGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> PLAYERS =
		new MultipleGroupPlayerConfigOptions("Players", "players", "all");

	public static final ValueConfigOptions<OfflinePlayer> PREY =
		new SingleGroupPlayerConfigOptions("Prey Player", "prey", "random");

	public static final ValueConfigOptions<Integer> REVEAL_PERIOD =
		new NumberConfigOptions("Reveal period duration seconds", "reveal", null, 180, Set.of(60, 120, 180, 240, 300, 360), true);

	public static final ManhuntGameConfigMap INSTANCE = new ManhuntGameConfigMap(ManhuntGame.ID);

	private ManhuntGameConfigMap(String namespace) {
		super(namespace, Set.of(PLAYERS, PREY, WorldBorderArea.CONFIG, REVEAL_PERIOD));
	}
}