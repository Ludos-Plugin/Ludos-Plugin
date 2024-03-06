package fr.ludos.item.huntsman.trident;


import org.bukkit.entity.Player;

import fr.ludos.Main;
import fr.ludos.item.SpecialItemEvents;
import fr.ludos.role.HuntsmanRole;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import javax.annotation.Nullable;

public class HuntsmanTridentEvent extends SpecialItemEvents<HuntsmanTrident> {

	private static final String OWNER_NAMESPACE_KEY = "ludos_archer_Trident_owner";
	private static final String XP_NAMESPACE_KEY = "ludos_archer_Trident_xp";
	private static final String LVL_NAMESPACE_KEY = "ludos_archer_Trident_lvl";

	private static NamespacedKey ownerKey = null;
	private static NamespacedKey xpKey = null;
	private static NamespacedKey lvlKey = null;



	static NamespacedKey getOwnerkey() {
		return ownerKey;
	}
	static NamespacedKey getXpKey() {
		return xpKey;
	}
	static NamespacedKey getLvlKey() {
		return lvlKey;
	}


	public HuntsmanTridentEvent() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
		lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
	}


	public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack trident) {
		player.getInventory().removeItem(trident);
	}


	@Override
	@Nullable
	protected HuntsmanTrident getItem(ItemStack stack) {
		try {
			HuntsmanTrident bow = new HuntsmanTrident(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	protected HuntsmanTrident createItem(Player owner) {
		return new HuntsmanTrident(owner);
	}

	@Override
	protected String getRoleId() {
		return HuntsmanRole.id;
	}

}