package fr.ludos.item;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;

import fr.ludos.role.Role;


public abstract class SpecialItemEvents<T extends SpecialItem> implements Listener {
	@Nullable
	protected abstract T getItem(ItemStack stack);

	protected abstract T createItem(Player owner);

	@Nullable
	protected abstract String getRoleId();


	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();

		if (getItem(item) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClickItem(InventoryClickEvent event) {
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
        String roleId = getRoleId();
		if ( roleId != null && ! Role.isPlayerRole(player, roleId) ) {
			return;
		}

		Inventory inventory = player.getInventory();
		if ( SpecialItem.containedIn(inventory, this::getItem) ) {
			return;
		}

		player.getInventory().addItem(createItem(player).getStack());
	}

}
