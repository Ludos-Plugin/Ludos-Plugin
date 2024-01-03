package fr.ludos.listener.items.burrower;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import org.bukkit.Material;

public enum BurrowerPickLevels {
    WOODEN  (Material.WOODEN_PICKAXE, 25),
    STONE   (Material.STONE_PICKAXE, 140),
    IRON    (Material.IRON_PICKAXE, 370),
    GOLDEN  (Material.GOLDEN_PICKAXE, 480),
    DIAMOND (Material.DIAMOND_PICKAXE, 575);

    private Material material;
    private double xpThreshold;

    public static final int valueCount;

    public int index() {
        return ArrayUtils.indexOf(values(), this);
    }
    public Material getMaterial() {
        return material;
    }
    public double xpThreshold() {
        return xpThreshold;
    }
    public boolean isMax() {
        return (index() + 1) >= valueCount; 
    }


    private BurrowerPickLevels(Material material, double xpThreshold) {
        this.material = material;
        this.xpThreshold = xpThreshold;
    }

    static {
        valueCount = values().length;
    }


    @Nullable
    public static BurrowerPickLevels findByKey(int i) {
        if ( i >= valueCount ) {
            return null;
        }
        return values()[i];
    }

    @Nullable
    public static BurrowerPickLevels getNextLevel(BurrowerPickLevels current) {
        int currentIndex = current.index();
        if ( currentIndex + 1 >= valueCount ) {
            return null;
        }
        return values()[currentIndex + 1];
    }
}