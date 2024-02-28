package fr.ludos.item.huntsman.crossbow;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;

import fr.ludos.Main;
import fr.ludos.item.SpecialItem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;


public class HuntsmanCrossbowEvents implements Listener {

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


	public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack CrossBow) {
		player.getInventory().removeItem(CrossBow);
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

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();

		HuntsmanCrossbow bow = new HuntsmanCrossbow(item);
		if ( bow != null ) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onInventoryClickItem(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		try {
			new HuntsmanCrossbow(item);
			if ( event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY || event.getInventory() != event.getClickedInventory() ) {
				event.setResult(Result.DENY);
			}
		} catch (IllegalArgumentException exception) {

		}

	}
	// @EventHandler
	// public void onInventoryMoveItem(InventoryMoveItemEvent event) {
	//     Bukkit.broadcastMessage(event.getDestination().toString());
	//     ItemStack item = event.getItem();

	//     Bukkit.broadcastMessage(item.toString());
	//     HuntsmanCrossbow bow = new HuntsmanCrossbow(item);

	//     if (bow != null && event.getDestination().getType() != InventoryType.PLAYER) {
	//         event.setCancelled(true);
	//     }
	// }

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();

		try {
			new HuntsmanCrossbow(item);
			event.setCancelled(true);
		} catch (IllegalArgumentException exception) {

		}
	}


	@EventHandler
	public void playerJoinTheGame(PlayerJoinEvent event) {
		actuateCrossbowInventory(event.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)  {
		actuateCrossbowInventory(event.getPlayer());
	}


	private void actuateCrossbowInventory(Player player) {
		if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
			return;
		}

		Inventory inventory = player.getInventory();
		if ( SpecialItem.containedIn(inventory, (stack) -> new HuntsmanCrossbow(stack)) ) {
			return;
		}

		inventory.addItem(
			new HuntsmanCrossbow(player).getStack()
		);
	}

}
