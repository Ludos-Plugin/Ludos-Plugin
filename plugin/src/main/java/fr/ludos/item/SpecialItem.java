package fr.ludos.item;

import fr.ludos.Main;
import fr.ludos.role.Role;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;



import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import java.util.UUID;


/**
 * Pickaxe is a class that represents a special item, "Miner Pickaxe," in Minecraft.
 * This item allows the miner player to improve their own pickaxe based on the ores they collect.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides methods to give a miner pickaxe to the player and level up the pickaxe based on XP.
 * <br><br>
 * - Automatically updates the pickaxe's material as it levels up.
 * <br><br>
 * - Defines an evolution path from wood to stone, iron, gold, and finally diamond pickaxe.
 * <br><br>
 * Usage:
 * <br><br>
 * - Call addPickaxeInventory(player) to give a miner pickaxe to the specified player.
 * <br><br>
 * - Call levelPickaxe(player, xp) with the XP gained from mining to level up the pickaxe.
 * <br><br>
 * Example:
 * <pre>{@code
 * Pickaxe pickaxe = new Pickaxe();
 * pickaxe.addPickaxeInventory(player);
 * pickaxe.levelPickaxe(player, xp);
 * }</pre>
 * <br><br>
 * @author feur25 & Ganon358
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.Material
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see java.util.Collections
 */

public abstract class SpecialItem {

	public static final String ID = "id";
	private NamespacedKey idKey = new NamespacedKey(Main.getInstance(), ID);

	public static final String OWNER = "owner";
	private NamespacedKey ownerKey = new NamespacedKey(Main.getInstance(), OWNER);

	private final ItemStack stack;

	private final Player owner;

	public ItemStack getStack() {
		return stack;
	}

	public Player getOwner() {
		return owner;
	}

	protected List<String> getLore() {
		return null;
	}

	protected abstract String getId();
	protected abstract String getName();


	public SpecialItem(ItemStack stack) throws IllegalArgumentException {
		if (stack == null) {
			throw new IllegalArgumentException("Item Stack is null");
		}
		this.stack = stack;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) {
			throw new IllegalArgumentException("ItemStack has no Meta");
		}
		PersistentDataContainer container = meta.getPersistentDataContainer();
		if (! container.has(ownerKey, PersistentDataType.STRING) ) {
			throw new IllegalArgumentException("Owner not found");
		}

		if (! container.has(idKey, PersistentDataType.STRING) ) {
			throw new IllegalArgumentException("ID not found");
		}
		String id = container.get(idKey, PersistentDataType.STRING);
		if (! id.equals(getId())) {
			throw new IllegalArgumentException("Invalid ID (" + id + " instead of " + getId() + ")");
		}

		this.owner = Bukkit.getPlayer(
			UUID.fromString(
				getPersistentData(stack, ownerKey, PersistentDataType.STRING)
			)
		);;
	}

	protected SpecialItem(ItemStack stack, Player owner) {
		this.stack = stack;
		this.owner = owner;

		updateName();

		ItemMeta meta = stack.getItemMeta();

		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
		container.set(idKey, PersistentDataType.STRING, getId());

		stack.setItemMeta(meta);
	}

	public void updateName() {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.RESET.toString() + ChatColor.WHITE + getName());
		stack.setItemMeta(meta);
	}
	public void updateLore() {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(getLore());
		stack.setItemMeta(meta);
	}

	/**
	 * @param inventory
	 * @return true if the provided inventory contains a SpecialItem of type T
	 */
	public static <T extends SpecialItem> Boolean containedIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) {
				continue;
			}
			T specialItem = constructor.apply(item);
			if (specialItem != null) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @param inventory
	 * @return The first Special Item of type T found in the inventory or null if there is none
	 */
	public static <T extends SpecialItem>T findIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) {
				continue;
			}
			T specialItem = constructor.apply(item);
			if (specialItem != null) {
				return specialItem;
			}
		}

		return null;
	}


	/**
	 * @param inventory
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem> List<T> findAllIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		ArrayList<T> results = new ArrayList<>();
		for (int i = 0; i < items.length; i++) {
			ItemStack item = items[i];
			if (item == null) {
				continue;
			}
			T specialItem = constructor.apply(item);
			if (specialItem != null) {
				results.add(specialItem);
			}
		}

		return (List<T>) Collections.unmodifiableList(results);
	}



	protected static <T, Z> Z getPersistentData(ItemStack item, NamespacedKey key, PersistentDataType<T, Z> type) {
		return item.getItemMeta().getPersistentDataContainer().get(key, type);
	}

	public static <T extends SpecialItem> T addSpecialItem(Player player, Function<Player, T> constructor) {
		return constructor.apply(player);
	}


	public static abstract class Events<T extends SpecialItem> implements Listener {
		@Nullable
		protected abstract T getItem(ItemStack stack);

		protected abstract T createItem(Player owner);

		protected final String roleId;

		public Events(@Nullable String roleId) {
			this.roleId = roleId;

			Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
		}

		public void stop() {
			HandlerList.unregisterAll(this);

			removeFromAllInventories();
		}

		protected void removeFromAllInventories() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Inventory inventory = player.getInventory();
				List<T> items = SpecialItem.findAllIn(inventory, this::getItem);
				for(T item : items) {
					inventory.remove(item.getStack());
				}
			}
		}

		protected void updateAllInventories() {
			// TODO: add proper Player selection
			if (roleId == null) {
				return;
			}
			for (Player player : Role.getPlayersOfRole(roleId)) {
				updateItemInInventory(player);
			}
		}

		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent event) {
			if (roleId != null && ! Role.isPlayerRole(event.getPlayer(), roleId)) {
				return;
			}

			ItemStack item = event.getItemDrop().getItemStack();

			if (getItem(item) != null) {
				event.setCancelled(true);
			}
		}

		@EventHandler
		public void onInventoryClickItem(InventoryClickEvent event) {
			if (roleId != null && ! Role.isPlayerRole(event.getWhoClicked(), roleId)) {
				return;
			}

			ItemStack item = event.getCursor();
			if (item.getType() == Material.AIR) {
				item = event.getCurrentItem();
			}

			if (getItem(item) == null) {
				return;
			}

			if ( event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getInventory().getType() != InventoryType.PLAYER/*  || isAnotherInventory */ ) {
				event.setResult(Result.DENY);
			}
		}

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			ItemStack item = event.getEntity().getItemStack();

			if (getItem(item) == null) {
				return;
			}

			event.setCancelled(true);
		}


		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			updateItemInInventory(event.getPlayer());
		}

		@EventHandler
		public void onPlayerRespawn(PlayerRespawnEvent event)  {
			updateItemInInventory(event.getPlayer());
		}


		public void updateItemInInventory(Player player) {
			if (roleId != null && ! Role.isPlayerRole(player, roleId)) {
				return;
			}

			Inventory inventory = player.getInventory();
			if (T.containedIn(inventory, this::getItem)) {
				return;
			}

			T item = createItem(player);
			if (item == null) {
				return;
			}
			player.getInventory().addItem(item.getStack());
		}

	}
}
