package fr.ludos.item.huntsman.trident;


import org.bukkit.entity.Player;
//import org.bukkit.entity.AbstractArrow.PickupStatus;

import fr.ludos.Main;
import fr.ludos.item.SpecialItem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
//import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
//import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;

import java.util.HashMap;
import java.util.Map;

public class HuntsmanTridentEvent implements Listener {

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

private final Map<Player, ItemStack> thrownTrident = new HashMap<>();
private final Map<Player, BukkitRunnable> retrievalTasks = new HashMap<>();

	//Retourne le trident si le joueur le jette après 60 secondes
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();
		Player player = event.getPlayer();

		if (new HuntsmanTrident(item) != null) {
		  thrownTrident.put(player, item);
		}
		BukkitRunnable task = new BukkitRunnable() {

			@Override
			public void run() {
				ItemStack trident = thrownTrident.remove(player);
					if (trident != null) {
						player.getInventory().addItem(trident);
						player.sendMessage("Your trident has returned to you.");
					}
					retrievalTasks.remove(player);
				}
		};
		task.runTaskLater(Main.getInstance(), 20 * 60);
		retrievalTasks.put(player, task);
	}


	@EventHandler
	public void onInventoryClickItem(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		try {
			new HuntsmanTrident(item);
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
	//     HuntsmanTrident bow = new HuntsmanTrident(item);

	//     if (bow != null && event.getDestination().getType() != InventoryType.PLAYER) {
	//         event.setCancelled(true);
	//     }
	// }

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();

		try {
			new HuntsmanTrident(item);
			event.setCancelled(true);
		} catch (IllegalArgumentException exception) {

		}
	}


	@EventHandler
	public void playerJoinTheGame(PlayerJoinEvent event) {
		actuateTridentInventory(event.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)  {
		actuateTridentInventory(event.getPlayer());
	}


	private void actuateTridentInventory(Player player) {
		if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
			return;
		}

		Inventory inventory = player.getInventory();
		if ( SpecialItem.containedIn(inventory, (stack) -> new HuntsmanTrident(stack)) ) {
			return;
		}

		inventory.addItem(
			new HuntsmanTrident(player).getStack()
		);
	}

}
