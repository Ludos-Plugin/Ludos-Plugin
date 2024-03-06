package fr.ludos.item.huntsman.crossbow;

import fr.ludos.item.SpecialItem;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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


	@Override
	public NamespacedKey getOwnerKey() {
		return null;
	}

	@Override
	protected String getName(){
		return "Old Crossbow";
	}

	@Override
	public List<String> getLore(){
		return null;
	}
}