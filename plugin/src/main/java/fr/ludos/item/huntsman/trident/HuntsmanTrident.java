package fr.ludos.item.huntsman.trident;

import fr.ludos.item.SpecialItem;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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