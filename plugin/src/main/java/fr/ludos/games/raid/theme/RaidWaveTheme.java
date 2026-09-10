package fr.ludos.games.raid.theme;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import fr.ludos.core.gui.Named;
import fr.ludos.games.raid.RaidGame;
import fr.ludos.games.raid.RaidWaveController;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import net.kyori.adventure.text.TextComponent;

/**
 * The Theme of a Wave, used in {@link RaidGame}.
 */
public abstract class RaidWaveTheme implements Named {
	private static final int MIN_MOB_COUNT = 11;
	private static final int MAX_MOB_COUNT = 100;

	private final RaidGame game;
	public RaidGame game() {
		return game;
	}

	public RaidWaveTheme(RaidGame game) {
		this.game = game;
	}

	@Override
	public abstract TextComponent displayName();

	public abstract WorldCreator getWorldCreator();
	public @Nullable Consumer<World> getWorldConfig() {
		return null;
	}

	public abstract List<WaveUnit> getPossibleUnits();

	public int getMinMobCount() {
		return MIN_MOB_COUNT;
	}
	public int getMaxMobCount() {
		return MAX_MOB_COUNT;
	}

	public void applyMobEffects(LivingEntity mob) { }

	public void applyPlayerEffects(Player player) { }
	public final void applyPlayerEffects(Iterable<Player> players) {
		for (Player player : players) {
			applyPlayerEffects(player);
		}
	}

	public void removePlayerEffects(Player player) { }
	public final void removePlayerEffects(Iterable<Player> players) {
		for (Player player : players) {
			removePlayerEffects(player);
		}
	}

	public Location getSpawnLocation(World world) {
		return world.getSpawnLocation();
	}

	public abstract RaidMonsterBoss<? extends Monster> createBoss(RaidGame game);

	public List<WaveUnit> composeWaveRoster(int pointsBudget, RaidWaveController waveController) {
		List<WaveUnit> roster = new ArrayList<>();
		int remaining = pointsBudget;
		int guard = 0;

		while (remaining >= 12 && guard++ < 2000 && roster.size() < getMaxMobCount()) {
			WaveUnit picked = pickWeightedUnit(remaining, waveController);
			if (picked == null) break;
			roster.add(picked);
			remaining -= picked.cost();
		}

		while (roster.size() < getMinMobCount() && roster.size() < getMaxMobCount()) {
			WaveUnit filler = pickWeightedUnit(20, waveController);
			if (filler == null) {
				filler = new WaveUnit(EntityType.ZOMBIE, 12, 1, false);
			}
			roster.add(filler);
		}

		return roster;
	}

	@Nullable
	public WaveUnit pickWeightedUnit(int remainingPoints, RaidWaveController waveController) {
		List<WaveUnit> eligible = getPossibleUnits().stream()
			.filter(unit -> unit.cost() <= remainingPoints)
			.filter(unit -> !unit.bossEcho() || waveController.bossesDefeated() > 0)
			.collect(Collectors.toList());

		if (eligible.isEmpty()) return null;

		int totalWeight = eligible.stream().mapToInt(WaveUnit::weight).sum();
		int roll = ThreadLocalRandom.current().nextInt(totalWeight);
		int current = 0;

		for (WaveUnit unit : eligible) {
			current += unit.weight();
			if (roll < current) return unit;
		}

		return eligible.get(eligible.size() - 1);
	}

	/**
	 * WaveUnit.
	 * @param type
	 * @param cost
	 * @param weight
	 * @param bossEcho
	 */
	public record WaveUnit(EntityType type, int cost, int weight, boolean bossEcho) { }
}
