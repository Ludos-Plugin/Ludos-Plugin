package fr.ludos.core.item.level;

import java.io.Serializable;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

/**
 * A simple pair record for holding a Level and XP value simultaneously.
 * @param level
 * @param xp
 */
public final record LevelValue(int level, double xp) implements Serializable {
	public LevelValue(int level) {
		this(level, 0.0);
	}
	public LevelValue() {
		this(0);
	}
	public LevelValue(LevelValue other) {
		this(other.level, other.xp);
	}

	public LevelValue copy(LevelValue other) {
		return new LevelValue(other);
	}

	public LevelValue withLevel(int level) {
		return new LevelValue(level, xp);
	}
	public LevelValue withLevel(int level, Integer maxLevel) {
		int maxed = maxLevel != null ? Math.min(level, maxLevel) : level;
		return withLevel(maxed);
	}

	public LevelValue withAddedLevel(int level) {
		return new LevelValue(this.level + level, xp);
	}
	public LevelValue withAddedLevel() {
		return withAddedLevel(1);
	}
	public LevelValue withAddedLevel(int level, Integer maxLevel) {
		return withLevel(this.level + level, maxLevel);
	}
	public LevelValue withAddLevel(Integer maxLevel) {
		return withAddedLevel(1, maxLevel);
	}

	public LevelValue withXp(double xp) {
		return new LevelValue(level, xp);
	}
	public LevelValue withXp(double xp, Function<@NotNull Integer, Double> thresholds, Integer maxLevel) {
		int level = this.level;

		while (level < maxLevel && xp >= thresholds.apply(level)) {
			xp -= thresholds.apply(level);
			level++;
		}
		if (maxLevel != null && level >= maxLevel) {
			return new LevelValue(maxLevel, 0.0);
		}

		return new LevelValue(level, xp);
	}
	public LevelValue withAddedXp(double xp) {
		return new LevelValue(level, this.xp + xp);
	}
	public LevelValue withAddedXp(double xp, Function<@NotNull Integer, Double> thresholds, Integer maxLevel) {
		return withXp(this.xp + xp, thresholds, maxLevel);
	}

	public LevelValue capped(Integer maxLevel) {
		if (maxLevel != null && level > maxLevel) {
			return new LevelValue(maxLevel, xp);
		}
		return this;
	}
	public LevelValue capped(double threshold, Integer maxLevel) {
		if (maxLevel != null && level >= maxLevel) {
			return new LevelValue(level, 0.0);
		}
		if (xp >= threshold) {
			return new LevelValue(level, 0);
		}
		return this;
	}
	public LevelValue capped(Function<@NotNull Integer, Double> thresholds, Integer maxLevel) {
		if (maxLevel != null && level >= maxLevel) {
			return new LevelValue(level, 0.0);
		}
		double threshold = thresholds.apply(level);
		if (xp >= threshold) {
			return new LevelValue(level, 0);
		}
		return this;
	}
	public LevelValue capped(double threshold) {
		if (xp >= threshold) {
			return new LevelValue(level, 0);
		}
		return this;
	}
	public LevelValue capped(Function<@NotNull Integer, Double> thresholds) {
		double threshold = thresholds.apply(level);
		if (xp >= threshold) {
			return new LevelValue(level, 0);
		}
		return this;
	}

	@Override
	public final String toString() {
		return "LevelValue[level=" + level + ", xp=" + xp + "]";
	}
}