package fr.ludos.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class SpecialItem {

	public static final String ID = "id";
	private static final NamespacedKey idKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ID);

	public static final String OWNER = "owner";
	private static final NamespacedKey ownerKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), OWNER);

	public static final int USAGE_COOLDOWN = 3;

	private final Game game;
	public Game getGame() { return game; }

	private final ItemStack stack;
	public ItemStack getStack() {
		return stack;
	}

	private final Player owner;
	public Player getOwner() {
		return owner;
	}

	public static @Nullable Player getSpecialItemOwner(ItemStack stack, String id, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(idKey, PersistentDataType.STRING) ) return null;
		String itemId = container.get(idKey, PersistentDataType.STRING);
		if (! itemId.equals(id)) return null;

		if (! container.has(ownerKey, PersistentDataType.STRING) ) return null;


		Player owner = Bukkit.getPlayer(
			UUID.fromString(
				getPersistentData(stack, ownerKey, PersistentDataType.STRING)
			)
		);

		return owner;
	}
	public static Component getActionAnnotation(final @NotNull String keybind, Component action) {
		return Component.text("Press ")
				.color(NamedTextColor.GRAY)
			.append(Component.keybind(keybind)
				.color(NamedTextColor.YELLOW))
			.append(Component.text(" to ")
				.color(NamedTextColor.GRAY))
			.append(action)
			.decoration(TextDecoration.ITALIC, false);
	}

	protected SpecialItem(ItemStack stack, Player owner, Game game) {
		this.game = game;
		this.stack = stack;
		this.owner = owner;
	}
	protected final void initializeItem() {
		ItemMeta meta = stack.getItemMeta();

		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
		container.set(idKey, PersistentDataType.STRING, getId());

		stack.setItemMeta(meta);

		updateName();
		updateLore();

		onInitialize();
	}
	protected void onInitialize() { }


	public List<Component> getLore() {
		return new ArrayList<>();
	}

	protected abstract String getId();
	protected abstract Component getName();


	public void updateName() {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		meta.displayName(getName());
		stack.setItemMeta(meta);
	}
	public void updateLore() {
		ItemStack stack = getStack();
		ItemMeta meta = stack.getItemMeta();
		meta.lore(getLore());
		stack.setItemMeta(meta);
	}

	protected final boolean refreshUseCooldown() {
		Player owner = getOwner();
		Material itemType = getStack().getType();

		int cooldown = owner.getCooldown(itemType);
		if (cooldown > 0 && cooldown <= USAGE_COOLDOWN) {
			return false;
		}
		if (cooldown == 0) {
			owner.setCooldown(itemType, USAGE_COOLDOWN);
		}
		return true;

	}

	/**
	 * @param inventory
	 * @return true if the provided inventory contains a SpecialItem of type T
	 */
	public static <T extends SpecialItem> Boolean containedIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
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
	@Nullable
	public static <T extends SpecialItem> T findIn(Inventory inventory, Function<ItemStack, T> constructor) {
		return findIn(Arrays.asList(inventory.getContents()), constructor);
	}
	/**
	 * @param inventory
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem> List<T> findAllIn(Inventory inventory, Function<ItemStack, T> constructor) {
		return findAllIn(Arrays.asList(inventory.getContents()), constructor);
	}

	public static <T extends SpecialItem> T findIn(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) return specialItem;
		}

		return null;
	}
	public static <T extends SpecialItem> List<T> findAllIn(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		ArrayList<T> results = new ArrayList<>();
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) results.add(specialItem);
		}

		return Collections.unmodifiableList(results);
	}



	protected static <T, Z> Z getPersistentData(ItemStack item, NamespacedKey key, PersistentDataType<T, Z> type) {
		return item.getItemMeta().getPersistentDataContainer().get(key, type);
	}

	public static <T extends SpecialItem> T addSpecialItem(Player player, Function<Player, T> constructor) {
		return constructor.apply(player);
	}


	public static abstract class Events<T extends SpecialItem> extends GameProcessBase {
		private boolean isStarted = false;

		private final boolean canDrop;
		@Nullable
		private final Integer slot;

		public final Game game;

		@Override
		protected JavaPlugin getPlugin() {
			return game.getPlugin();
		}

		protected Events(Game game, @Nullable Integer slot, boolean canDrop) {
			this.game = game;

			this.canDrop = canDrop;
			this.slot = slot;
		}
		protected Events(Game game, @Nullable Integer slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}


		@Override
		protected final void onInit() {
			onItemInit();
		}
		@Override
		protected final void onStart() {
			updateAllInventories();

			onItemStart();
		}

		protected void onItemInit() { }
		protected void onItemStart() { }


		@Override
		protected final void onDeinit() {
			onItemDeinit();
		}
		@Override
		protected final void onStop() {
			removeFromAllInventories();

			onItemStop();
		}

		protected void onItemDeinit() { }
		protected void onItemStop() { }


		@Nullable
		protected abstract T getItem(ItemStack stack, Game game);
		protected abstract T createItem(Player owner, Game game);

		protected abstract Boolean canPlayerHaveItem(HumanEntity owner);


		protected void removeFromAllInventories() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerInventory inventory = player.getInventory();
				List<T> items = SpecialItem.findAllIn(inventory, (ItemStack stack) -> getItem(stack, game));
				for(T item : items) {
					inventory.remove(item.getStack());
					if (inventory.getItemInOffHand().equals(item.getStack())) {
						inventory.setItemInOffHand(null);
					}
				}
			}
		}

		protected void updateAllInventories() {
			for (Player player : game.getGameTeamController().getPlayers()) {
				if (canPlayerHaveItem(player)) {
					updateItemInInventory(player);
				}
			}
		}

		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent event) {
			if (canDrop) return;
			if (! canPlayerHaveItem(event.getPlayer())) return;

			ItemStack item = event.getItemDrop().getItemStack();

			if (getItem(item, game) != null) {
				event.setCancelled(true);
			}
		}

		@EventHandler
		public void onInventoryClickItem(InventoryClickEvent event) {
			if (canDrop) return;
			if (! canPlayerHaveItem(event.getWhoClicked())) return;

			ItemStack item = event.getCursor();
			if (item.getType().isAir()) {
				item = event.getCurrentItem();
			}

			if (getItem(item, game) == null) return;

			InventoryType invType = event.getInventory().getType();

			if (
				event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
				invType != InventoryType.PLAYER &&
				invType != InventoryType.CRAFTING
			) {
				event.setResult(Result.DENY);
			}
		}

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			if (canDrop) return;
			ItemStack item = event.getEntity().getItemStack();

			if (getItem(item, game) == null) return;

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
			if (! canPlayerHaveItem(player)) return;

			PlayerInventory inventory = player.getInventory();
			if (T.containedIn(inventory, (ItemStack stack) -> getItem(stack, game))) return;

			T item = createItem(player, game);
			if (item == null) return;

			int index = (slot == null) ? -1 : slot.intValue();

			if (index == -1 || inventory.getItem(index) != null) {
				inventory.addItem(item.getStack());
			} else {
				inventory.setItem(index, item.getStack());
			}
		}
	}
}
