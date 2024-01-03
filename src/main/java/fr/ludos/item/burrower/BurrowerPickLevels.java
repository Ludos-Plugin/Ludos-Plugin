package fr.ludos.item.burrower;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public enum BurrowerPickLevels {
    WOODEN      (Material.WOODEN_PICKAXE, 25, Collections.emptyMap()),
    STONE       (Material.STONE_PICKAXE, 37, Collections.emptyMap()),
    STONE1      (Material.STONE_PICKAXE, 55, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 1); }} ),
    GOLDEN        (Material.GOLDEN_PICKAXE, 82, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 1); }} ),
    GOLDEN1       (Material.GOLDEN_PICKAXE, 123, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 2); }} ),
    IRON      (Material.IRON_PICKAXE, 184, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 2); }} ),
    IRON1     (Material.IRON_PICKAXE, 276, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); }} ),
    IRON2     (Material.IRON_PICKAXE, 414, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1); }} ),
    DIAMOND     (Material.DIAMOND_PICKAXE, 621, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}} ),
    DIAMOND1    (Material.DIAMOND_PICKAXE, 931, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}} ),
    DIAMOND2    (Material.DIAMOND_PICKAXE, 1396, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}} ),
    NETHERITE   (Material.NETHERITE_PICKAXE, 2094, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}} ),
    NETHERITE1  (Material.NETHERITE_PICKAXE, 6238, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}} ),
    NETHERITE2  (Material.NETHERITE_PICKAXE, 10000, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}} );
    // NETHERITE2  (Material.NETHERITE_PICKAXE, 370, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 2); }} ),
    // NETHERITE3  (Material.NETHERITE_PICKAXE, 370, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 3); }} ),
    // NETHERITE4  (Material.NETHERITE_PICKAXE, 370, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 4); }} ),
    // NETHERITE5  (Material.NETHERITE_PICKAXE, 370, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); }} );

    private Material material;
    private double xpThreshold;
    private Map<Enchantment, Integer> enchantments;

    public static final int valueCount;

    public int index() {
        return ArrayUtils.indexOf(values(), this);
    }
    public Material getMaterial() {
        return material;
    }
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    public double xpThreshold() {
        return xpThreshold;
    }
    public boolean isMax() {
        return (index() + 1) >= valueCount; 
    }


    private BurrowerPickLevels(Material material, double xpThreshold, Map<Enchantment, Integer> enchantments) {
        this.material = material;
        this.xpThreshold = xpThreshold;
        this.enchantments = enchantments; 
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