package fr.ludos.item.huntsman.crossbow;

import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HuntsmanCrossbow extends SpecialItem {
    public HuntsmanCrossbow(ItemStack stack){
        super(stack);
    }
    public HuntsmanCrossbow(Player owner){
        super(new ItemStack(Material.CROSSBOW), owner);
    }
    public HuntsmanCrossbow(ItemStack item, Player owner) {
        super(item, owner);
    }


    /**
     * @param inventory
     * @return true if the provided inventory contains a Burrower's pick
     */
    public static HuntsmanCrossbow findIn(Inventory inventory) {
        for (ItemStack item : inventory) {
            try {
                HuntsmanCrossbow pick = new HuntsmanCrossbow(item);
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
        return "Old Crossbow";
    }

    @Override
    public String getLore(){
        return null;
    }
}