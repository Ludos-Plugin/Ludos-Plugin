package fr.ludos.core.item.level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
	public @NotNull double getXpThreshold(@NotNull Integer level) {
		if (xpThresholdFunction != null) {
			return xpThresholdFunction.apply(level);
		}
		if (xpThreshold != null) {
			return xpThreshold;
		}
		return Double.POSITIVE_INFINITY;
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
	private final List<Consumer<LevelState>> xpChangeListeners = new ArrayList<>();

	public void addXpChangeListener(Consumer<LevelState> listener) {
		xpChangeListeners.add(listener);
	}

	public void removeXpChangeListener(Consumer<LevelState> listener) {
		xpChangeListeners.remove(listener);
	}

	private void notifyXpChange() {
		for (Consumer<LevelState> listener : xpChangeListeners) {
			listener.accept(this);
		}
	}

	private final List<BiConsumer<LevelState, Integer>> levelUpListeners = new ArrayList<>();

	public void addLevelUpListener(BiConsumer<LevelState, Integer> listener) {
		levelUpListeners.add(listener);
	}

	public void removeLevelUpListener(BiConsumer<LevelState, Integer> listener) {
		levelUpListeners.remove(listener);
	}

	private void notifyLevelUp(int oldLevel) {
		for (BiConsumer<LevelState, Integer> listener : levelUpListeners) {
			listener.accept(this, oldLevel);
		}
	}

	public LevelState(LevelValue level) {
		this.value = level;
	}
	public LevelState(LevelValue level, Function<Integer, Double> thresholdFunction) {
		this.value = level;
		this.xpThresholdFunction = thresholdFunction;
	}
	public LevelState(LevelValue level, double threshold) {
		this.value = level;
		this.xpThreshold = threshold;
	}
	public LevelState(LevelValue level, Function<Integer, Double> thresholdFunction, int maxLevel) {
		this.value = level;
		this.xpThresholdFunction = thresholdFunction;
		this.maxLevel = maxLevel;
	}
	public LevelState(LevelValue level, double threshold, int maxLevel) {
		this.value = level;
		this.xpThreshold = threshold;
		this.maxLevel = maxLevel;
	}
	public LevelState(LevelValue level, int maxLevel) {
		this.value = level;
		this.maxLevel = maxLevel;
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
		value = old.withXp(xp, this::getXpThreshold, maxLevel());
		if (old.xp() != value.xp()) {
			notifyXpChange();
		}
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
	public void addXp(double xp) {
		LevelValue old = value;
		value = old.withAddedXp(xp, this::getXpThreshold, maxLevel());
		if (old.xp() != value.xp()) {
			notifyXpChange();
		}
		if (old.level() != value.level()) {
			notifyLevelUp(old.level());
		}
	}
}
