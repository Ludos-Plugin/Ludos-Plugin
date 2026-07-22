package fr.ludos.core.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Material;
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

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.other.ExcludeFromJacocoGeneratedReport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A {@link SpecialItemInterface} wrapper for an Item ({@link ItemStack}).
 * @param <T> self type
 */
public abstract class SpecialItem<T extends SpecialItem<T>> implements SpecialItemInterface {
	public static final int USAGE_COOLDOWN = 4;

	private final Events<T> events;
	public Events<T> getEvents() { return events; }
	public Game getGame() { return events.getGame(); }

	@Override
	public final String getTypeId() {
		return events.getTypeId();
	}

	private final UUID itemId;
	public final UUID getItemId() {
		return itemId;
	}

	ItemStack stack;
	public ItemStack getStack() {
		return stack;
	}

	private final Player owner;
	public Player getOwner() {
		return owner;
	}

	protected SpecialItem(ItemData info, Events<T> events) {
		this.itemId = Objects.requireNonNull(info.itemId);
		this.owner = Objects.requireNonNull(info.owner);
		this.stack = Objects.requireNonNull(info.stack);
		this.events = Objects.requireNonNull(events);
	}


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

	@ExcludeFromJacocoGeneratedReport // Tested, but not picked up by Jacoco
	public final boolean refreshUseCooldown() {
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

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof SpecialItem item)) return false;
		if (getClass() != obj.getClass()) return false;

		return
			getItemId().equals(item.getItemId()) &&
			getTypeId().equals(item.getTypeId()) &&
			getOwner().getUniqueId().equals(item.getOwner().getUniqueId());
	}

	/**
	 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}.<br>
	 * @return Whether or not the provided inventory contains at least one instance of {@link SpecialItem} type T
	 */
	public static <T extends SpecialItem<T>> Boolean containedIn(Inventory inventory, Function<ItemStack, T> constructor) {
		ItemStack[] items = inventory.getContents();
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Find a single instance of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}.<br>
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return The first Special Item of type T found in the inventory or null if there is none
	 */
	@Nullable
	public static <T extends SpecialItem<T>> T findIn(Inventory inventory, Function<ItemStack, T> constructor) {
		return findIn(Arrays.asList(inventory.getContents()), constructor);
	}
	/**
	 * Find all instances of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}.<br>
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem<T>> List<T> findAllIn(Inventory inventory, Function<ItemStack, T> constructor) {
		return findAllIn(Arrays.asList(inventory.getContents()), constructor);
	}

	/**
	 * Find a single instance of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param items The items to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}.<br>
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return The first Special Item of type T found in the inventory or null if there is none
	 */
	public static <T extends SpecialItem<T>> T findIn(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) return specialItem;
		}

		return null;
	}
	/**
	 * Find all instances of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param items The items to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}.<br>
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem<T>> List<T> findAllIn(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		ArrayList<T> results = new ArrayList<>();
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) results.add(specialItem);
		}

		return Collections.unmodifiableList(results);
	}

	public static <T extends SpecialItem<T>, TData> Component buildDataLore(String label, TData data) {
		return
			Component.text(label + ": ")
				.color(NamedTextColor.GRAY)
				.append(
					Component.text(data.toString())
						.color(NamedTextColor.YELLOW)
				)
			.decoration(TextDecoration.ITALIC, false);
	}

	/**
	 * Information about a {@link SpecialItem}, such as its specific ID and owner.
	 * @param stack
	 * @param itemId
	 * @param owner
	 */
	public static record ItemData(
		UUID itemId,
		ItemStack stack,
		Player owner
	) {
		public ItemData(ItemStack stack, Player owner) {
			this(UUID.randomUUID(), stack, owner);
		}
	}

	/**
	 * Events for the {@link T} {@link SpecialItem} type.
	 * @param <T> The {@link SpecialItem} Type to work on.
	 */
	public static abstract class Events<T extends SpecialItem<T>> extends GameEvents {
		protected final Map<UUID, T> CACHED = new HashMap<>();
		private final Info info;
		public final Info getInfo() {
			return this.info;
		}

		protected Events(Game game, Info info) {
			super(game);

			this.info = info;
		}

		public abstract String getTypeId();

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


		protected abstract @Nullable T getItemInternal(SpecialItem.ItemData info);
		public final @Nullable T getItem(ItemStack stack) {
			UUID itemId = SpecialItemInterface.getSpecialItemId(stack, getTypeId(), game);
			if (itemId == null) return null;

			Player owner = SpecialItemInterface.getSpecialItemOwner(stack);
			if (owner == null) return null;

			T cached = CACHED.get(itemId);
			if (cached != null) {
				cached.stack = stack; // Stack is a different instance, every time
				return cached;
			}

			T got = getItemInternal(new SpecialItem.ItemData(itemId, stack, owner));
			CACHED.put(itemId, got);

			return got;
		}

		protected abstract T createItemInternal(Player owner);
		public final T createItem(Player owner) {
			T item = createItemInternal(owner);

			ItemMeta meta = item.getStack().getItemMeta();

			if (! visibleDurability()) {
				meta.setUnbreakable(true);
				meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			}

			PersistentDataContainer container = meta.getPersistentDataContainer();
			container.set(OWNER_KEY, PersistentDataType.STRING, owner.getUniqueId().toString());
			container.set(TYPE_ID_KEY, PersistentDataType.STRING, getTypeId());
			container.set(ITEM_ID_KEY, PersistentDataType.STRING, item.getItemId().toString());

			item.getStack().setItemMeta(meta);

			item.updateName();
			item.updateLore();

			CACHED.put(item.getItemId(), item);

			return item;
		}

		public boolean visibleDurability() {
			return false;
		}

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

			ItemSlot.setItemInInventory(info.slot(), item.getStack(), inventory);
		}

		public void refreshAllPlayerInventories() {
			for (Player player : getGame().getGroup().getOnlinePlayers()) {
				refreshPlayerInventory(player);
			}
		}

		public void removeFromPlayerInventory(Player player) {
			PlayerInventory inventory = player.getInventory();
			for(T item : SpecialItem.findAllIn(inventory, this::getItem)) {
				inventory.remove(item.getStack());

				if (item.getStack().equals(inventory.getHelmet())) {
					inventory.setHelmet(null);
				}
				else if (item.getStack().equals(inventory.getChestplate())) {
					inventory.setChestplate(null);
				}
				else if (item.getStack().equals(inventory.getLeggings())) {
					inventory.setLeggings(null);
				}
				else if (item.getStack().equals(inventory.getBoots())) {
					inventory.setBoots(null);
				}
				else if (item.getStack().equals(inventory.getItemInOffHand())) {
					inventory.setItemInOffHand(null);
				}
			}
		}

		public void removeFromAllInventories() {
			for (Player player : getGame().getGroup().getOnlinePlayers()) {
				removeFromPlayerInventory(player);
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
				itemEvents.removeFromPlayerInventory(player);
			}
		}

		public static void removeFromAllPlayerInventories(Game game) {
			for (SpecialItem.Events<?> itemEvents : game.getActiveItems()) {
				itemEvents.removeFromAllInventories();
			}
		}


		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent event) {
			if (info.canDrop()) return;
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			ItemStack item = event.getItemDrop().getItemStack();

			if (getItem(item) != null) {
				event.setCancelled(true);
			}
		}

		@EventHandler
		public void onInventoryClickItem(InventoryClickEvent event) {
			if (info.canDrop()) return;
			HumanEntity entity = event.getWhoClicked();
			if (! (entity instanceof Player player)) return;

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
			if (info.canDrop()) return;
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

		/**
		 * Configuration for {@link SpecialItem.Events}, such as its default slot and whether or not it can be dropped.
		 * @param slot The default {@link ItemSlot} that the Item will attempt to place itself in
		 * @param canDrop Whether or not the {@link SpecialItem} instance can be dropped by its {@link SpecialItem#owner}
		 */
		public static final record Info(
			@Nullable ItemSlot slot,
			boolean canDrop
		) {
			public Info(@Nullable ItemSlot slot) {
				this(slot, false);
			}
			public Info(boolean canDrop) {
				this(null, canDrop);
			}
			public Info() {
				this(null, false);
			}
		}
	}
}
