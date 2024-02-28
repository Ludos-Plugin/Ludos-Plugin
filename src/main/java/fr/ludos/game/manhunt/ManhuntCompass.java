package fr.ludos.game.manhunt;

import javax.annotation.Nullable;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.NamespacedKey;

import fr.ludos.Main;
import fr.ludos.item.SpecialItem;

public class ManhuntCompass extends SpecialItem {
    private static final String OWNER_NAMESPACE_KEY = "ludos_hunter_compass_owner";

    public ManhuntCompass(Player owner) {
		super(new ItemStack(Material.COMPASS), owner);
    }


    @Override 
    public NamespacedKey getOwnerKey() {
        return new NamespacedKey(Main.getInstance(), OWNER_NAMESPACE_KEY);
    }

    @Override 
    public String getLore() {
        return "Toute les 3 min donne la position de la proie";
    }

    @Override 
    public String getName() {
        return "Boussole de Chasseur";
    }

	// @Nullable
    // public static ManhuntCompass getHunterCompass(ItemStack item) {
	// 	if ()
        
    // }
}
