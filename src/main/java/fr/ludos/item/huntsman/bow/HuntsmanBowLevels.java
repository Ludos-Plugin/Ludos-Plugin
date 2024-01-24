package fr.ludos.item.huntsman.bow;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.SpecialItemLevels;

public enum HuntsmanBowLevels implements SpecialItemLevels {
    BASE (200, null, new HuntsmanBowLevels[]{});

    private double xpThreshold;
    private Map<Enchantment, Integer> enchantments;
    private HuntsmanBowLevels[] evolutions;

    public final static HuntsmanBowLevels[] values = HuntsmanBowLevels.values();

    public int index() {
        return ArrayUtils.indexOf(values(), this);
    }
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    public HuntsmanBowLevels[] getEvolutions() {
        return evolutions;
    }
    public double xpThreshold() {
        return xpThreshold;
    }
    public boolean isMax() {
        return (index() + 1) >= values.length; 
    }


    private HuntsmanBowLevels(double xpThreshold, Map<Enchantment, Integer> enchantments, HuntsmanBowLevels[] evolutions) {
        this.xpThreshold = xpThreshold;
        this.enchantments = enchantments;
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
}