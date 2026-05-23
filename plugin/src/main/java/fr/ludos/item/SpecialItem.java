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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
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
import fr.ludos.game.GameEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class SpecialItem implements SpecialItemInterface {

	public static final String TYPE_ID = "type_id";
	private static final NamespacedKey typeIdKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), TYPE_ID);

	public static final String ITEM_ID_KEY = "item_id";
	private static final NamespacedKey itemIdKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ITEM_ID_KEY);

	public static final String OWNER_KEY = "owner";
	private static final NamespacedKey ownerKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), OWNER_KEY);

	public static final int USAGE_COOLDOWN = 4;

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

	public static @Nullable UUID getSpecialItemId(ItemStack stack, String typeId, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;

		PersistentDataContainer container = meta.getPersistentDataContainer();

		if (! container.has(typeIdKey, PersistentDataType.STRING) ) return null;
		String found = container.get(typeIdKey, PersistentDataType.STRING);

		if (! found.equals(typeId)) return null;
		if (! container.has(itemIdKey, PersistentDataType.STRING) ) return null;

		return UUID.fromString(
			getPersistentData(stack, itemIdKey, PersistentDataType.STRING)
		);
	}

	public static @Nullable Player getSpecialItemOwner(ItemStack stack, Game game) {
		if (stack == null) return null;

		ItemMeta meta = stack.getItemMeta();
		if (meta == null) return null;


		PersistentDataContainer container = meta.getPersistentDataContainer();

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
			.append(
				Component.keybind(keybind)
					.color(NamedTextColor.YELLOW)
			)
			.append(
				Component.text(" to ")
					.color(NamedTextColor.GRAY)
			)
			.append(action)
			.decoration(TextDecoration.ITALIC, false);
	}

	protected SpecialItem(ItemStack stack, Player owner, Game game) {
		this.game = game;
		this.stack = stack;
		this.owner = owner;
	}
	protected final UUID initializeItem() {
		ItemMeta meta = stack.getItemMeta();

		meta.setUnbreakable(true);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

		UUID itemId = UUID.randomUUID();

		PersistentDataContainer container = meta.getPersistentDataContainer();
		container.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
		container.set(typeIdKey, PersistentDataType.STRING, getTypeId());
		container.set(itemIdKey, PersistentDataType.STRING, itemId.toString());

		stack.setItemMeta(meta);

		updateName();
		updateLore();

		onInitialize();

		return itemId;
	}

	protected void onInitialize() { }


	public List<Component> getLore() {
		return new ArrayList<>();
	}

	protected abstract String getTypeId();
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

	@Override
	public void update() {
		updateName();
		updateLore();
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

	public static <T extends SpecialItem, TData> Component buildDataLore(String label, TData data) {
		return
			Component.text(label + ": ")
				.color(NamedTextColor.GRAY)
				.append(
					Component.text(data.toString())
						.color(NamedTextColor.YELLOW)
				)
			.decoration(TextDecoration.ITALIC, false);
	}


	public static abstract class Events<T extends SpecialItem> extends GameEvents {
		private final boolean canDrop;
		@Nullable
		private final ItemSlot slot;

		protected Events(Game game, @Nullable ItemSlot slot, boolean canDrop) {
			super(game);

			this.canDrop = canDrop;
			this.slot = slot;
		}
		protected Events(Game game, @Nullable ItemSlot slot) {
			this(game, slot, false);
		}
		protected Events(Game game) {
			this(game, null, false);
		}


		@Override
		protected final void onInit() {
			super.onInit();
			onItemInit();
		}
		@Override
		protected final void onStart() {
			super.onStart();
			refreshAllPlayerInventories();

			game.getActiveItems().add(this);

			onItemStart();
		}

		protected void onItemInit() { }
		protected void onItemStart() { }


		@Override
		protected final void onDeinit() {
			super.onDeinit();
			onItemDeinit();
		}
		@Override
		protected final void onStop() {
			super.onStop();
			removeFromAllInventories();

			game.getActiveItems().remove(this);

			onItemStop();
		}

		protected void onItemDeinit() { }
		protected void onItemStop() { }


		@Nullable
		public abstract T getItem(ItemStack stack);
		public abstract T createItem(Player owner);

		public final Boolean isPlayerValid(OfflinePlayer player) {
			if (! game.getGroup().isPlayer(player)) return false;
			return isPlayerValidInternal(player);
		}
		protected Boolean isPlayerValidInternal(OfflinePlayer player) {
			return true;
		}


		public void refreshPlayerInventory(Player player) {
			if (! game.getGroup().isPlayer(player)) return;
			if (! isPlayerValid(player)) return;

			PlayerInventory inventory = player.getInventory();
			if (T.containedIn(inventory, this::getItem)) return;

			T item = createItem(player);
			if (item == null) return;

			ItemSlot.setItemInInventory(slot, item.getStack(), inventory);
		}

		public void refreshAllPlayerInventories() {
			for (Player player : getGame().getGroup().getOnlinePlayers()) {
				refreshPlayerInventory(player);
			}
		}

		public void removeFromPLayerInventory(Player player) {
			PlayerInventory inventory = player.getInventory();
			for(T item : SpecialItem.findAllIn(inventory, this::getItem)) {
				inventory.remove(item.getStack());

				if (inventory.getItemInOffHand().equals(item.getStack())) {
					inventory.setItemInOffHand(null);
				}
				else if (inventory.getHelmet() != null && inventory.getHelmet().equals(item.getStack())) {
					inventory.setHelmet(null);
				}
				else if (inventory.getChestplate() != null && inventory.getChestplate().equals(item.getStack())) {
					inventory.setChestplate(null);
				}
				else if (inventory.getLeggings() != null && inventory.getLeggings().equals(item.getStack())) {
					inventory.setLeggings(null);
				}
				else if (inventory.getBoots() != null && inventory.getBoots().equals(item.getStack())) {
					inventory.setBoots(null);
				}
			}
		}

		public void removeFromAllInventories() {
			for (Player player : getGame().getGroup().getOnlinePlayers()) {
				removeFromPLayerInventory(player);
			}
		}

		public static void refreshPlayerInventory(Game game, Player player) {
			for (SpecialItem.Events<?> itemEvents : game.getActiveItems()) {
				itemEvents.refreshPlayerInventory(player);
			}
		}
		public static void refreshAllPlayerInventories(Game game) {
			for (SpecialItem.Events<?> itemEvents : game.getActiveItems()) {
				itemEvents.refreshAllPlayerInventories();
			}
		}

		public static void removeFromPlayerInventory(Game game, Player player) {
			for (SpecialItem.Events<?> itemEvents : game.getActiveItems()) {
				itemEvents.removeFromPLayerInventory(player);
			}
		}

		public static void removeFromAllPlayerInventories(Game game) {
			for (SpecialItem.Events<?> itemEvents : game.getActiveItems()) {
				itemEvents.removeFromAllInventories();
			}
		}


		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent event) {
			if (canDrop) return;
			Player player = event.getPlayer();
			if (! game.getGroup().isPlayer(player)) return;
			if (! isPlayerValid(player)) return;

			ItemStack item = event.getItemDrop().getItemStack();

			if (getItem(item) != null) {
				event.setCancelled(true);
			}
		}

		@EventHandler
		public void onInventoryClickItem(InventoryClickEvent event) {
			if (canDrop) return;
			HumanEntity entity = event.getWhoClicked();
			Player player = Bukkit.getPlayer(entity.getUniqueId());
			if (player == null) return;
			if (! game.getGroup().isPlayer(player)) return;
			if (! isPlayerValid(player)) return;

			ItemStack item = event.getCursor();
			if (item.getType().isAir()) {
				item = event.getCurrentItem();
			}

			if (getItem(item) == null) return;

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

			if (getItem(item) == null) return;

			event.setCancelled(true);
		}


		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event) {
			refreshPlayerInventory(event.getPlayer());
		}

		@EventHandler
		public void onPlayerRespawn(PlayerRespawnEvent event)  {
			refreshPlayerInventory(event.getPlayer());
		}
	}
}
