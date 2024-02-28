package fr.ludos.item.huntsman.bow;

import java.util.Collections;
import java.util.Map;

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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class HuntsmanBowEvents implements Listener {

	private static final String OWNER_NAMESPACE_KEY = "ludos_archer_bow_owner";
	private static final String XP_NAMESPACE_KEY = "ludos_archer_bow_xp";
	private static final String LVL_NAMESPACE_KEY = "ludos_archer_bow_lvl";

	private static NamespacedKey ownerKey = null;
	private static NamespacedKey xpKey = null;
	private static NamespacedKey lvlKey = null;

	private static Map<String, HuntsmanBowLevels> deadPlayerLevels = Collections.emptyMap();



	static NamespacedKey getOwnerkey() {
		return ownerKey;
	}
	static NamespacedKey getXpKey() {
		return xpKey;
	}
	static NamespacedKey getLvlKey() {
		return lvlKey;
	}


	public HuntsmanBowEvents() {
		Main plugin = Main.getInstance();
		ownerKey = new NamespacedKey(plugin, OWNER_NAMESPACE_KEY);
		xpKey = new NamespacedKey(plugin, XP_NAMESPACE_KEY);
		lvlKey = new NamespacedKey(plugin, LVL_NAMESPACE_KEY);
	}


	public void playerEventListener(Player player, PlayerDeathEvent event, ItemStack CrossBow) {
		player.getInventory().removeItem(CrossBow);
	}


	@EventHandler
	public void poisonArrow1(Player player) {

		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();

		Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
		poisonArrow.setGravity(true);
		poisonArrow.setShooter(player);
		poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

		PotionEffect poisoneffect = new PotionEffect(PotionEffectType.POISON, 100, 1);
		poisonArrow.addCustomEffect(poisoneffect, true);
	}


// //Arrow arrowProjectile = (Arrow) event.getProjectile();
//         arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
//         if (arrowProjectile.isShotFromCrossbow()) {
//             arrowProjectile.setGravity(false);
//             arrowProjectile.setDamage(0.5);
//         }

	@EventHandler
	public void poisonArrow2(Player player) {

		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();
		Arrow poisonArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		poisonArrow.setPickupStatus(PickupStatus.DISALLOWED);
		poisonArrow.setShooter(player);
		poisonArrow.setVelocity(eyeLocation.getDirection().multiply(2));

		PotionEffect poisoneffect = new PotionEffect(PotionEffectType.POISON, 60, 2);
		poisonArrow.addCustomEffect(poisoneffect, true);

	}

	@EventHandler
	public void fireArrow(Player player, int level) {

		Location eyeLocation = player.getEyeLocation();
		World world = player.getWorld();

		Arrow fireArrow = (Arrow) world.spawnEntity(eyeLocation, EntityType.ARROW);
		fireArrow.setShooter(player);
		fireArrow.setVelocity(eyeLocation.getDirection().multiply(2));
	}

	@EventHandler
	public void onShootArrow(EntityShootBowEvent event) {
		if ( ! (event.getEntity() instanceof Player) ) {
			return;
		}
		Player player = (Player) event.getEntity();

		try {
			HuntsmanBow bow = new HuntsmanBow(event.getBow());

			Arrow arrowProjectile = (Arrow)event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			updateArrowCount(player);
			if (bow.getLevel().getType() == HuntsmanBowLevels.LevelBranch.POISON) {

			}
			if (bow.getLevel() == HuntsmanBowLevels.POISON1) {
				poisonArrow1(player);
			}
			if (bow.getLevel() == HuntsmanBowLevels.POISON2) {
				poisonArrow2(player);
			}
		} catch (Error e) {

		}
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

		HuntsmanBow bow = new HuntsmanBow(item);
		if ( bow != null ) {
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onInventoryClickItem(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		try {
			new HuntsmanBow(item);
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
	//     HuntsmanBow bow = new HuntsmanBow(item);

	//     if (bow != null && event.getDestination().getType() != InventoryType.PLAYER) {
	//         event.setCancelled(true);
	//     }
	// }

	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		ItemStack item = event.getEntity().getItemStack();

		try {
			new HuntsmanBow(item);
			event.setCancelled(true);
		} catch (IllegalArgumentException exception) {

		}
	}


	@EventHandler
	public void playerJoinTheGame(PlayerJoinEvent event) {
		actuateBowInventory(event.getPlayer());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
			return;
		}

		HuntsmanBow bow = SpecialItem.findIn(player.getInventory(), (item) -> new HuntsmanBow(item));
		if ( bow != null ) {
			Integer index = bow.getLevel().index() - 1;
			index = Math.max(0, index);
			deadPlayerLevels.put( player.getName(), HuntsmanBowLevels.values()[index] );
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)  {
		actuateBowInventory(event.getPlayer());
	}


	private void actuateBowInventory(Player player) {
		if ( ! Role.isPlayerRole(player, BurrowerRole.id) ) {
			return;
		}

		Inventory inventory = player.getInventory();
		if ( SpecialItem.containedIn(inventory, (stack) -> new HuntsmanBow(stack)) ) {
			return;
		}


		HuntsmanBowLevels level = HuntsmanBowLevels.BASE;
		if (player != null && deadPlayerLevels.containsKey(player.getName())) {
			level = deadPlayerLevels.get(player.getName());
		}
		inventory.addItem(
			new HuntsmanBow(player, level).getStack()
		);
	}

}
