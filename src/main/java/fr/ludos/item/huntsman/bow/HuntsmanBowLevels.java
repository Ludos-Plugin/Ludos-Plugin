package fr.ludos.item.huntsman.bow;

import javax.annotation.Nullable;
import org.apache.commons.lang3.ArrayUtils;
import fr.ludos.item.SpecialItemLevels;

public enum HuntsmanBowLevels implements SpecialItemLevels {
    BASE (200, LevelBranch.NONE, new HuntsmanBowLevels[]{}),
    FIRE1 (200, LevelBranch.FIRE, new HuntsmanBowLevels[]{}),
    FIRE2 (200, LevelBranch.FIRE, new HuntsmanBowLevels[]{}),
    EXPLOSION (200, LevelBranch.FIRE, new HuntsmanBowLevels[]{}),
    SLOW1 (200, LevelBranch.SLOWNESS, new HuntsmanBowLevels[]{}),
    SLOW2 (200, LevelBranch.SLOWNESS, new HuntsmanBowLevels[]{}), 
    SLOW2_COBWEB (200, LevelBranch.SLOWNESS, new HuntsmanBowLevels[]{}), 
    POISON1(200, LevelBranch.POISON, new HuntsmanBowLevels[]{}),
    POISON2(200, LevelBranch.POISON, new HuntsmanBowLevels[]{}),
    POISON2_NAUSEA(200, LevelBranch.POISON, new HuntsmanBowLevels[]{}); 

    private double xpThreshold;
    private LevelBranch type;
    private HuntsmanBowLevels[] evolutions;

    public final static HuntsmanBowLevels[] values = HuntsmanBowLevels.values();

    public int index() {
        return ArrayUtils.indexOf(values(), this);
    }
    public HuntsmanBowLevels[] getEvolutions() {
        return evolutions;
    }
    public LevelBranch getType() {
        return type;
    }
    public double getXpThreshold() {
        return xpThreshold;
    }
    public boolean isMax() {
        return evolutions.length == 0; 
    }


    private HuntsmanBowLevels(double xpThreshold, LevelBranch branch, HuntsmanBowLevels[] evolutions) {
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

    @Nullable
    public static HuntsmanBowLevels getNextLevel(HuntsmanBowLevels current) {
        int currentIndex = current.index();
        if ( currentIndex + 1 >= values.length ) {
            return null;
        }
        return values()[currentIndex + 1];
    }

    public static enum LevelBranch {
        NONE,
        FIRE,
        POISON,
        SLOWNESS;
    }
}