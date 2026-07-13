package fr.ludos.games.manhunt;

import java.util.Set;

import org.bukkit.OfflinePlayer;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.NumberConfigOptions;
import fr.ludos.core.config.SingleGroupPlayerConfigOptions;
import fr.ludos.core.config.ValueConfigOptions;

public final class ManhuntGameConfigMap extends ConfigOptionsMap {
	public static final ValueConfigOptions<Set<OfflinePlayer>> players =
		new MultipleGroupPlayerConfigOptions("Players", "players", "all");

	public static final ValueConfigOptions<OfflinePlayer> prey =
		new SingleGroupPlayerConfigOptions("Prey Player", "prey", "random");

	public static final ValueConfigOptions<Integer> revealPeriod =
		new NumberConfigOptions("Reveal period duration seconds", "reveal", null, 180, Set.of(60, 120, 180, 240, 300, 360), true);

	public static final ManhuntGameConfigMap instance = new ManhuntGameConfigMap(ManhuntGame.ID);

	private ManhuntGameConfigMap(String namespace) {
		super(namespace, Set.of(players, prey, WorldBorderArea.config, revealPeriod));
	}
}