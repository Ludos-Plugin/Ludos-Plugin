package fr.ludos.core.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.persistence.PersistentEntry;
import fr.ludos.core.persistence.serializer.IntegerSerializer;
import fr.ludos.other.ExcludeFromJacocoGeneratedReport;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * A {@link SpecialItemInterface} wrapper for an Item ({@link ItemStack}).
 * @param <T> self type
 */
public abstract class SpecialItem<T extends SpecialItem<T>> implements SpecialItemInterface {
	public final static String NAMESPACE = "item";

	public static final PersistentEntry<Integer> ENTITY_KILLS = PersistentEntry.of(IntegerSerializer.UNSIGNED, "entity_kills", 0);
	public static final PersistentEntry<Integer> PLAYER_KILLS = PersistentEntry.of(IntegerSerializer.UNSIGNED, "player_kills", 0);

	public final static int USAGE_COOLDOWN = 4;

	private final Events<T> events;
	public Events<T> getEvents() { return events; }
	public Game game() { return events.game(); }

	@Override
	public final String typeId() {
		return events.getTypeId();
	}

	private final UUID itemId;
	public final UUID getItemId() {
		return itemId;
	}

	ItemStack stack;
	public ItemStack stack() {
		return stack;
	}

	private final Player owner;
	public Player owner() {
		return owner;
	}

	protected SpecialItem(ItemData info, Events<T> events) {
		this.itemId = Objects.requireNonNull(info.itemId);
		this.owner = Objects.requireNonNull(info.owner);
		this.stack = Objects.requireNonNull(info.stack);
		this.events = Objects.requireNonNull(events);
	}


	public void updateName() {
		ItemStack stack = stack();
		ItemMeta meta = stack.getItemMeta();
		meta.displayName(normalizedDisplayName());
		stack.setItemMeta(meta);
	}
	public void updateLore() {
		ItemStack stack = stack();
		ItemMeta meta = stack.getItemMeta();
		meta.lore(getLore());
		stack.setItemMeta(meta);
	}

	@Override
	public void update() {
		updateName();
		updateLore();
	}

	public void uncache() {
		events.uncache(this);
	}

	public void give(Player player) {
		events.give(this, player);
	}
	public void give(PlayerInventory inventory) {
		events.give(this, inventory);
	}

	@ExcludeFromJacocoGeneratedReport // Tested, but not picked up by Jacoco
	public final boolean refreshUseCooldown() {
		Player owner = owner();
		Material itemType = stack().getType();

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
			typeId().equals(item.typeId()) &&
			owner().getUniqueId().equals(item.owner().getUniqueId());
	}

	/**
	 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param player The player whose inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * @return Whether or not the provided player owns at least one instance of {@link SpecialItem} type T
	 */
	public static <T extends SpecialItem<T>> Boolean isOwnedBy(Player player, Function<ItemStack, T> constructor) {
		return isContainedIn(player.getInventory(), constructor);
	}

	/**
	 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * @return Whether or not the provided inventory contains at least one instance of {@link SpecialItem} type T
	 */
	public static <T extends SpecialItem<T>> Boolean isContainedIn(PlayerInventory inventory, Function<ItemStack, T> constructor) {
		if (isContainedIn((Inventory) inventory, constructor)) {
			return true;
		}
		else if (constructor.apply(inventory.getHelmet()) != null) {
			return true;
		}
		else if (constructor.apply(inventory.getChestplate()) != null) {
			return true;
		}
		else if (constructor.apply(inventory.getLeggings()) != null) {
			return true;
		}
		else if (constructor.apply(inventory.getBoots()) != null) {
			return true;
		}
		else if (constructor.apply(inventory.getItemInOffHand()) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * @return Whether or not the provided inventory contains at least one instance of {@link SpecialItem} type T
	 */
	public static <T extends SpecialItem<T>> Boolean isContainedIn(Inventory inventory, Function<ItemStack, T> constructor) {
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
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return The first Special Item of type T found in the inventory or null if there is none
	 */
	@Nullable
	public static <T extends SpecialItem<T>> T findOne(Inventory inventory, Function<ItemStack, T> constructor) {
		return findOne(Arrays.asList(inventory.getContents()), constructor);
	}
	/**
	 * Find a single instance of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param items The items to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return The first Special Item of type T found in the inventory or null if there is none
	 */
	public static <T extends SpecialItem<T>> T findOne(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) return specialItem;
		}

		return null;
	}

	/**
	 * Find all instances of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem<T>> List<T> findAll(Inventory inventory, Function<ItemStack, T> constructor) {
		return findAll(Arrays.asList(inventory.getContents()), constructor);
	}
	/**
	 * Find all instances of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param items The items to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem<T>> List<T> findAll(Iterable<ItemStack> items, Function<ItemStack, T> constructor) {
		ArrayList<T> results = new ArrayList<>();
		for (ItemStack item : items) {
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) results.add(specialItem);
		}

		return results;
	}
	/**
	 * Find all instances of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
	 * @param <T> The specific type of {@link SpecialItem} that will be searched for
	 * @param inventory The inventory to search the {@link SpecialItem} in
	 * @param constructor A function that parses an ItemStack as an instance of that {@link SpecialItem}
	 * @param order The order of the slots to search in the inventory
	 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
	 * @return All the Special Items of type T found in the inventory or an empty list if there is none
	 */
	public static <T extends SpecialItem<T>> List<@NotNull T> findAll(PlayerInventory inventory, Function<ItemStack, T> constructor, ItemSlot[] order) {
		ArrayList<T> results = new ArrayList<>();
		for (ItemSlot slot : order) {
			ItemStack item = slot.get(inventory);
			if (item == null) continue;

			T specialItem = constructor.apply(item);
			if (specialItem != null) results.add(specialItem);
		}

		return results;
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
			String typeId = SpecialItemInterface.getSpecialItemTypeId(stack, game);
			if (! getTypeId().equals(typeId)) return null;

			UUID itemId = SpecialItemInterface.getSpecialItemId(stack, game);
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

			ItemMeta meta = item.stack().getItemMeta();

			if (! visibleDurability()) {
				meta.setUnbreakable(true);
				meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			}

			PersistentDataContainer container = meta.getPersistentDataContainer();
			container.set(OWNER_KEY, PersistentDataType.STRING, owner.getUniqueId().toString());
			container.set(TYPE_ID_KEY, PersistentDataType.STRING, getTypeId());
			container.set(ITEM_ID_KEY, PersistentDataType.STRING, item.getItemId().toString());

			item.stack().setItemMeta(meta);

			item.updateName();
			item.updateLore();

			CACHED.put(item.getItemId(), item);

			return item;
		}

		protected <TItem extends SpecialItem<T>> void uncache(TItem item) {
			if (item == null) return;
			CACHED.remove(item.getItemId());
		}

		public boolean visibleDurability() {
			return false;
		}
		public boolean isRanged() {
			return false;
		}
		public boolean isWeapon() {
			return true;
		}

		public final Boolean isPlayerValid(OfflinePlayer player) {
			if (! game.group().isPlayer(player)) return false;
			return isPlayerValidInternal(player);
		}
		protected Boolean isPlayerValidInternal(OfflinePlayer player) {
			return true;
		}

		public void createAndGiveTo(Player player) {
			T item = createItem(player);
			give(item, player);
		}

		public <TItem extends SpecialItem<T>> void give(TItem item, PlayerInventory inventory) {
			if (item == null) return;
			ItemSlot.set(info.slot, item.stack(), inventory);
		}
		public <TItem extends SpecialItem<T>> void give(TItem item, Player player) {
			give(item, player.getInventory());
		}

		public void refreshPlayerInventory(Player player) {
			if (! game.group().isPlayer(player)) return;
			if (! isPlayerValid(player)) return;

			if (isOwnedBy(player)) return;

			createAndGiveTo(player);
		}

		public void refreshAllPlayerInventories() {
			for (Player player : game().group().getOnlinePlayers()) {
				refreshPlayerInventory(player);
			}
		}

		public void removeFromPlayerInventory(Player player) {
			PlayerInventory inventory = player.getInventory();
			for(T item : SpecialItem.findAll(inventory, this::getItem)) {
				inventory.remove(item.stack());

				if (item.stack().equals(inventory.getHelmet())) {
					inventory.setHelmet(null);
				}
				else if (item.stack().equals(inventory.getChestplate())) {
					inventory.setChestplate(null);
				}
				else if (item.stack().equals(inventory.getLeggings())) {
					inventory.setLeggings(null);
				}
				else if (item.stack().equals(inventory.getBoots())) {
					inventory.setBoots(null);
				}
				else if (item.stack().equals(inventory.getItemInOffHand())) {
					inventory.setItemInOffHand(null);
				}
			}
		}

		public void removeFromAllInventories() {
			for (Player player : game().group().getOnlinePlayers()) {
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

		/**
		 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
		 * @param player The player whose inventory to search the {@link SpecialItem} in
		 * @return Whether or not the provided player owns at least one instance of {@link SpecialItem} type T
		 */
		public Boolean isOwnedBy(Player player) {
			return SpecialItem.isOwnedBy(player, this::getItem);
		}

		/**
		 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
		 * @param inventory The inventory to search the {@link SpecialItem} in
		 * @return Whether or not the provided inventory contains at least one instance of {@link SpecialItem} type T
		 */
		public Boolean isContainedIn(PlayerInventory inventory) {
			return SpecialItem.isContainedIn(inventory, this::getItem);
		}

		/**
		 * Check for the presence of any number of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
		 * @param inventory The inventory to search the {@link SpecialItem} in
		 * @return Whether or not the provided inventory contains at least one instance of {@link SpecialItem} type T
		 */
		public Boolean isContainedIn(Inventory inventory) {
			return SpecialItem.isContainedIn(inventory, this::getItem);
		}

		/**
		 * Find a single instance of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
		 * @param inventory The inventory to search the {@link SpecialItem} in
		 * @return The first Special Item of type T found in the inventory or null if there is none
		 */
		@Nullable
		public T findOne(Inventory inventory) {
			return SpecialItem.findOne(inventory, this::getItem);
		}
		/**
		 * Find a single instance of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
		 * @param items The items to search the {@link SpecialItem} in
		 * @return The first Special Item of type T found in the inventory or null if there is none
		 */
		public T findOne(Iterable<ItemStack> items) {
			return SpecialItem.findOne(items, this::getItem);
		}

		/**
		 * Find all instances of the {@link SpecialItem} type T in the given inventory, using the given constructor to parse ItemStacks.
		 * @param inventory The inventory to search the {@link SpecialItem} in
		 * @return All the Special Items of type T found in the inventory or an empty list if there is none
		 */
		public List<T> findAll(Inventory inventory) {
			return SpecialItem.findAll(inventory, this::getItem);
		}
		/**
		 * Find all instances of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
		 * @param items The items to search the {@link SpecialItem} in
		 * @return All the Special Items of type T found in the inventory or an empty list if there is none
		 */
		public List<T> findAll(Iterable<ItemStack> items) {
			return SpecialItem.findAll(items, this::getItem);
		}
		/**
		 * Find all instances of the {@link SpecialItem} type T in the given items iterable, using the given constructor to parse ItemStacks.
		 * @param inventory The inventory to search the {@link SpecialItem} in
		 * @param order The order of the slots to search in the inventory
		 * Note: it does not CREATE a {@link SpecialItem}, it only converts it into one if possible.
		 * @return All the Special Items of type T found in the inventory or an empty list if there is none
		 */
		public List<@NotNull T> findAll(PlayerInventory inventory, ItemSlot[] order) {
			return SpecialItem.findAll(inventory, this::getItem, order);
		}


		public void recordKill(T item, PersistentEntry<Integer> entry) {
			ConfigurationSection killerData = game().ludos().getItemData(item.owner(), this);

			int currentKills = entry.getOrDefault(killerData);
			entry.set(currentKills + 1, killerData);

			game().ludos().savePlayersConfig();
		}

		@EventHandler
		public void onKill(EntityDeathEvent event) {
			if (isWeapon()) return;

			LivingEntity victim = event.getEntity();
			EntityDamageEvent damageEvent = victim.getLastDamageCause();

			if (! (damageEvent instanceof EntityDamageByEntityEvent entityDamage)) return;

			ItemStack weapon;
			Entity damageCause = entityDamage.getDamager();
			if (! isRanged() && damageCause instanceof Player attacker) {
				weapon = attacker.getInventory().getItemInMainHand();
				if (weapon.getType() == Material.AIR) {
					weapon = attacker.getInventory().getItemInOffHand();
				}
				if (weapon == null) return;
			} else if (isRanged() && damageCause instanceof AbstractArrow arrow) {
				if (! (arrow.getShooter() instanceof Player shooter)) return;

				weapon = shooter.getInventory().getItemInMainHand();
				if (weapon.getType() == Material.AIR) {
					weapon = shooter.getInventory().getItemInOffHand();
				}
				if (weapon == null) return;
			} else return;


			T item = getItem(weapon);
			if (item == null) return;

			PersistentEntry<Integer> killData = (victim instanceof Player)
				? PLAYER_KILLS
				: ENTITY_KILLS;
			recordKill(item, killData);
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
			InventoryAction action = event.getAction();

			boolean removeFromInventory = action == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
				invType != InventoryType.PLAYER &&
				invType != InventoryType.CRAFTING;
			boolean splitItem = action == InventoryAction.PICKUP_HALF ||
				action == InventoryAction.PICKUP_SOME ||
				action == InventoryAction.PICKUP_ONE;

			if (removeFromInventory || splitItem) {
				event.setResult(Result.DENY);
			}
		}

		@EventHandler
		public void onItemSpawn(ItemSpawnEvent event) {
			if (info.canDrop()) return;
			ItemStack item = event.getEntity().getItemStack();

			T found = getItem(item);
			if (found == null) return;

			event.setCancelled(true);
			found.uncache();
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
