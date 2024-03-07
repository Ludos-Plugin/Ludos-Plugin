package fr.ludos.item.huntsman;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import fr.ludos.item.SpecialItemLevels;

import java.util.List;
import java.util.Arrays;

public enum HuntsmanBowLevels implements SpecialItemLevels<HuntsmanBowLevels> {
	SLOW2_COBWEB    (200, LevelBranch.SLOWNESS, Arrays.asList()),
	SLOW2           (200, LevelBranch.SLOWNESS, Arrays.asList(SLOW2_COBWEB)),
	SLOW1           (200, LevelBranch.SLOWNESS, Arrays.asList(SLOW2)),

	POISON2_NAUSEA  (200, LevelBranch.POISON,   Arrays.asList()),
	POISON2         (200, LevelBranch.POISON,   Arrays.asList(POISON2_NAUSEA)),
	POISON1         (200, LevelBranch.POISON,   Arrays.asList(POISON2)),

	FLAME_EXPLOSION (200, LevelBranch.FIRE,     Arrays.asList()),
	FLAME2          (200, LevelBranch.FIRE,     Arrays.asList(FLAME_EXPLOSION)),
	FLAME1          (200, LevelBranch.FIRE,     Arrays.asList(FLAME2)),

	BASE            (200, LevelBranch.NONE,     Arrays.asList(FLAME1, POISON1, SLOW1));

	private double xpThreshold;
	private LevelBranch type;
	private List<HuntsmanBowLevels> evolutions;

	public final static HuntsmanBowLevels[] values = HuntsmanBowLevels.values();

	public int index() {
		return ArrayUtils.indexOf(values(), this);
	}
	public List<HuntsmanBowLevels> getEvolutions() {
		return evolutions;
	}
	public LevelBranch getType() {
		return type;
	}
	public double getXpThreshold() {
		return xpThreshold;
	}
	public boolean isMax() {
		return evolutions.size() == 0;
	}


	private HuntsmanBowLevels(double xpThreshold, LevelBranch branch, List<HuntsmanBowLevels> evolutions) {
		this.type = branch;
		this.xpThreshold = xpThreshold;
	}


	@Nullable
	public static HuntsmanBowLevels findByKey(int i) {
		if ( i >= values.length ) {
			return null;
		}
		return values()[i];
	}


	public static enum LevelBranch {
		NONE,
		FIRE,
		POISON,
		SLOWNESS;
	}

	@Override
	public HuntsmanBowLevels getPrevious() {
		int index = index() - 1;
		index = Math.max(0, index);
		return values()[index];
	}

	@Override
	public HuntsmanBowLevels getNext() {
		int currentIndex = index();
		if ( currentIndex + 1 >= values.length ) {
			return this;
		}
		return values()[currentIndex + 1];
	}
}