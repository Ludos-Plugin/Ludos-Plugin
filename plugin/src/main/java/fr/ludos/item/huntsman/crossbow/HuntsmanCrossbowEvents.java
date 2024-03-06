package fr.ludos.item.huntsman.crossbow;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;

import fr.ludos.Main;
import fr.ludos.item.SpecialItemEvents;
import fr.ludos.role.HuntsmanRole;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;


public class HuntsmanCrossbowEvents extends SpecialItemEvents<HuntsmanCrossbow> {

	private static final String OWNER_NAMESPACE_KEY = "ludos_archer_crossbow_owner";
	private static final String XP_NAMESPACE_KEY = "ludos_archer_crossbow_xp";
	private static final String LVL_NAMESPACE_KEY = "ludos_archer_crossbow_lvl";

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


	public HuntsmanCrossbowEvents() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
		lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
	}

	@EventHandler
	public void onShootArrow(EntityShootBowEvent event) {
		if ( ! (event.getEntity() instanceof Player) ) {
			return;
		}
		Player player = (Player) event.getEntity();

		Arrow arrowProjectile = (Arrow) event.getProjectile();
		arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
		if (arrowProjectile.isShotFromCrossbow()) {
			arrowProjectile.setGravity(false);
			arrowProjectile.setDamage(0.5);
		}

		updateArrowCount(player);
	}

	private void updateArrowCount(Player player) {
		Inventory inventory = player.getInventory();

		ItemStack arrowItem = new ItemStack(Material.ARROW);
		inventory.remove(Material.ARROW);
		inventory.addItem(arrowItem);
	}


	@Override
	@Nullable
	protected HuntsmanCrossbow getItem(ItemStack stack) {
		try {
			HuntsmanCrossbow bow = new HuntsmanCrossbow(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	@Override
	protected HuntsmanCrossbow createItem(Player owner) {
		return new HuntsmanCrossbow(owner);
	}

	@Override
	protected String getRoleId() {
		return HuntsmanRole.id;
	}
}
