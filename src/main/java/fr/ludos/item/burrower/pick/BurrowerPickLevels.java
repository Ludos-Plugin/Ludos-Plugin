package fr.ludos.item.burrower.pick;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import fr.ludos.item.SpecialItemLevels;

public enum BurrowerPickLevels implements SpecialItemLevels {
    WOODEN      (Material.WOODEN_PICKAXE, 25, Collections.emptyMap(), 1),
    STONE       (Material.STONE_PICKAXE, 37, Collections.emptyMap(), 1),
    STONE1      (Material.STONE_PICKAXE, 55, new HashMap<Enchantment, Integer>(){{        put(Enchantment.DIG_SPEED, 1); }}, 1 ),
    GOLDEN      (Material.GOLDEN_PICKAXE, 82, new HashMap<Enchantment, Integer>(){{       put(Enchantment.DIG_SPEED, 1); }}, 2 ),
    GOLDEN1     (Material.GOLDEN_PICKAXE, 123, new HashMap<Enchantment, Integer>(){{      put(Enchantment.DIG_SPEED, 2); }}, 2 ),
    IRON        (Material.IRON_PICKAXE, 184, new HashMap<Enchantment, Integer>(){{        put(Enchantment.DIG_SPEED, 2); }}, 2 ),
    IRON1       (Material.IRON_PICKAXE, 276, new HashMap<Enchantment, Integer>(){{        put(Enchantment.DIG_SPEED, 3); }}, 2 ),
    IRON2       (Material.IRON_PICKAXE, 414, new HashMap<Enchantment, Integer>(){{        put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}, 2 ),
    DIAMOND     (Material.DIAMOND_PICKAXE, 621, new HashMap<Enchantment, Integer>(){{     put(Enchantment.DIG_SPEED, 3); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}, 3 ),
    DIAMOND1    (Material.DIAMOND_PICKAXE, 931, new HashMap<Enchantment, Integer>(){{     put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 1);}}, 3 ),
    DIAMOND2    (Material.DIAMOND_PICKAXE, 1396, new HashMap<Enchantment, Integer>(){{    put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}, 3 ),
    NETHERITE   (Material.NETHERITE_PICKAXE, 2094, new HashMap<Enchantment, Integer>(){{  put(Enchantment.DIG_SPEED, 4); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}, 4 ),
    NETHERITE1  (Material.NETHERITE_PICKAXE, 6238, new HashMap<Enchantment, Integer>(){{  put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 2);}}, 4 ),
    NETHERITE2  (Material.NETHERITE_PICKAXE, 10000, new HashMap<Enchantment, Integer>(){{ put(Enchantment.DIG_SPEED, 5); put(Enchantment.LOOT_BONUS_BLOCKS, 3);}}, 4 );

    private Material material;
    private double xpThreshold;
    private Map<Enchantment, Integer> enchantments;
    private int radius;

    private final static BurrowerPickLevels[] values = BurrowerPickLevels.values();

    public int index() {
        return ArrayUtils.indexOf(values, this);
    }
    public Material getMaterial() {
        return material;
    }
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }
    public double getXpThreshold() {
        return xpThreshold;
    }
    public boolean isMax() {
        return (index() + 1) >= values.length; 
    }
    public int radius(){
        return radius;
    }


    private BurrowerPickLevels(Material material, double xpThreshold, Map<Enchantment, Integer> enchantments, int radius) {
        this.material = material;
        this.xpThreshold = xpThreshold;
        this.enchantments = enchantments; 
        this.radius = radius;
    }


    @Nullable
    public static BurrowerPickLevels findByKey(int i) {
        if ( i >= values.length ) {
            return null;
        }
        return values()[i];
    }

    @Nullable
    public static BurrowerPickLevels getNextLevel(BurrowerPickLevels current) {
        int currentIndex = current.index();
        if ( currentIndex + 1 >= values.length ) {
            return null;
        }
        return values()[currentIndex + 1];
    }
}