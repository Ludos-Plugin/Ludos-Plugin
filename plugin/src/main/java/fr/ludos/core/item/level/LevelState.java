package fr.ludos.core.item.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class LevelState {
	@Nullable
	private Integer maxLevel;
	public Integer maxLevel() {
		if (maxLevel != null) {
			return maxLevel;
		}
		return Integer.MAX_VALUE;
	}

	@Nullable
	private Function<@NotNull Integer, Double> xpThresholdFunction;
	@Nullable
	private Double xpThreshold;
	public @NotNull double xpThreshold(@NotNull Integer level) {
		if (xpThresholdFunction != null) {
			return xpThresholdFunction.apply(level);
		}
		if (xpThreshold != null) {
			return xpThreshold;
		}
		return Double.POSITIVE_INFINITY;
	}
	public @NotNull double xpThreshold() {
		return xpThreshold(level());
	}

	private LevelValue value;
	public LevelValue value() {
		return value;
	}
	public int level() {
		return value.level();
	}
	public double xp() {
		return value.xp();
	}
	private final List<BiConsumer<LevelValue, Double>> xpChangeListeners = new ArrayList<>();

	public void addXpChangeListener(BiConsumer<LevelValue, Double> listener) {
		xpChangeListeners.add(listener);
	}

	public void removeXpChangeListener(BiConsumer<LevelValue, Double> listener) {
		xpChangeListeners.remove(listener);
	}

	private void notifyXpChange(double oldXp) {
		for (BiConsumer<LevelValue, Double> listener : xpChangeListeners) {
			listener.accept(value(), oldXp);
		}
	}

	private final List<BiConsumer<LevelValue, Integer>> levelUpListeners = new ArrayList<>();

	public void addLevelUpListener(BiConsumer<LevelValue, Integer> listener) {
		levelUpListeners.add(listener);
	}

	public void removeLevelUpListener(BiConsumer<LevelValue, Integer> listener) {
		levelUpListeners.remove(listener);
	}

	private void notifyLevelUp(int oldLevel) {
		for (BiConsumer<LevelValue, Integer> listener : levelUpListeners) {
			listener.accept(value(), oldLevel);
		}
	}

	public LevelState(LevelValue level) {
		this.value = level;
	}
	public LevelState(LevelValue level, Function<Integer, Double> thresholdFunction) {
		this.value = level.capped(thresholdFunction);
		this.xpThresholdFunction = thresholdFunction;
	}
	public LevelState(LevelValue level, double threshold) {
		this.value = level.capped(threshold);
		this.xpThreshold = threshold;
	}
	public LevelState(LevelValue level, Function<Integer, Double> thresholdFunction, int maxLevel) {
		this.value = level.capped(thresholdFunction, maxLevel);
		this.xpThresholdFunction = thresholdFunction;
		this.maxLevel = maxLevel;
	}
	public LevelState(LevelValue level, double threshold, int maxLevel) {
		this.value = level.capped(threshold, maxLevel);
		this.xpThreshold = threshold;
		this.maxLevel = maxLevel;
	}
	public LevelState(LevelValue level, int maxLevel) {
		this.value = level.capped(maxLevel);
		this.maxLevel = maxLevel;
	}

	public void setValue(LevelValue level) {
		LevelValue old = value;
		value = level.capped(this::xpThreshold, maxLevel());
		if (old.xp() != value.xp()) {
			notifyXpChange(old.xp());
		}
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}

	public void setLevel(int level) {
		LevelValue old = value;
		value = old.withLevel(level, maxLevel());
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
	public void addLvl(int toAdd) {
		LevelValue old = value;
		value = old.withAddedLevel(toAdd, maxLevel());
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
	public void addLvl() {
		LevelValue old = value;
		value = old.withAddedLevel(maxLevel());
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}

	public void setXp(double xp) {
		LevelValue old = value;
		value = old.withXp(xp, this::xpThreshold, maxLevel());
		if (old.xp() != value.xp()) {
			notifyXpChange(old.xp());
		}
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
	public void addXp(double xp) {
		LevelValue old = value;
		value = old.withAddedXp(xp, this::xpThreshold, maxLevel());
		if (old.xp() != value.xp()) {
			notifyXpChange(old.xp());
		}
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
}
