package fr.ludos.item.huntsman.trident;

import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HuntsmanTrident extends SpecialItem {
    public HuntsmanTrident(ItemStack stack){
        super(stack);
    }
    public HuntsmanTrident(Player owner){
        super(new ItemStack(Material.TRIDENT), owner);
    }
    public HuntsmanTrident(ItemStack item, Player owner) {
        super(item, owner);
    }


    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrower's pick
     */
    public static HuntsmanTrident findIn(Inventory inventory) {
        for (ItemStack item : inventory) {
            try {
                HuntsmanTrident pick = new HuntsmanTrident(item);
                return pick;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return null;
    }
    

    @Override
    public NamespacedKey getOwnerKey() { 
        return null;
    }

    @Override
    protected String getName(){
        return "Old Trident";
    }

    @Override
    public String getLore(){
        return null;
    }
}