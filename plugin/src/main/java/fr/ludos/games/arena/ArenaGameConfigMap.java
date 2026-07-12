package fr.ludos.games.arena;

import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.config.ConfigOptions;
import fr.ludos.core.config.ConfigMap;
import fr.ludos.core.config.EnumConfigOptions;
import fr.ludos.core.config.NumberConfigOption;
import fr.ludos.core.config.MultipleGroupPlayerConfigOptions;

public final class ArenaGameConfigMap extends ConfigMap {
	public static final String team1PlayersOptionsKey = "team_1";
	public static final MultipleGroupPlayerConfigOptions team1PlayersOptions = new MultipleGroupPlayerConfigOptions("Team 1 players", "random");

	public static final String team2PlayersOptionsKey = "team_2";
	public static final MultipleGroupPlayerConfigOptions team2PlayersOptions = new MultipleGroupPlayerConfigOptions("Team 2 players", "random");

	public static final String modeOptionsKey = "arena_mode";
	public static final EnumConfigOptions<ArenaModeOption> modeOptions = ArenaModeOption.configOptions;

	public static final String roundsOptionsKey = "rounds";
	public static final NumberConfigOption roundsOptions = new NumberConfigOption("Number of Rounds", true);

	public static final String areaSizeOptionsKey = "area";
	public static final NumberConfigOption areaSizeOptions = WorldBorderArea.configOption;

	public static final ArenaGameConfigMap instance = new ArenaGameConfigMap(ArenaGame.ID);

	private ArenaGameConfigMap(String namespace) {
		super(namespace);
	}

	@Override
	public @NotNull Set<@NotNull String> getValues() {
		return Set.of(team1PlayersOptionsKey, team2PlayersOptionsKey, modeOptionsKey, roundsOptionsKey, areaSizeOptionsKey);
	}

	@Override
	public ConfigOptions<?> getOptions(String name) {
		switch (name) {
			case team1PlayersOptionsKey: return team1PlayersOptions;
			case team2PlayersOptionsKey: return team2PlayersOptions;
			case modeOptionsKey: return modeOptions;
			case roundsOptionsKey: return roundsOptions;
			case areaSizeOptionsKey: return areaSizeOptions;
			default: return null;
		}
	}

	public Set<OfflinePlayer> getTeam1Players(ConfigurationSection container) {
		return team1PlayersOptions.getTypedValueOrDefault(namespacedPath(team1PlayersOptionsKey), container);
	}
	public Set<OfflinePlayer> getTeam2Players(ConfigurationSection container) {
		return team2PlayersOptions.getTypedValueOrDefault(namespacedPath(team2PlayersOptionsKey), container);
	}
	public ArenaModeOption getMode(ConfigurationSection container) {
		return modeOptions.getTypedValueOrDefault(namespacedPath(modeOptionsKey), container);
	}
	public int getRounds(ConfigurationSection container) {
		return roundsOptions.getTypedValueOrDefault(namespacedPath(roundsOptionsKey), container);
	}
	public int getAreaSize(ConfigurationSection container) {
		return areaSizeOptions.getTypedValueOrDefault(namespacedPath(areaSizeOptionsKey), container);
	}
}
