package fr.ludos.games.raid;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOptions;
import fr.ludos.core.config.ValueConfigOptions;

public final class RaidGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> players =
		new MultipleGroupPlayerConfigOptions("Players", "players", "all");

	public static final ValueConfigOptions<Integer> waves =
		new NumberConfigOptions("Number of Waves", "waves", null, 0, true);

	public static final RaidGameConfigMap instance = new RaidGameConfigMap(RaidGame.ID);

	private RaidGameConfigMap(String namespace) {
		super(namespace, Set.of(players, waves, WorldBorderArea.config));
	}
}