package fr.ludos.core.item;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.item.SpecialItem.Events.Info;
import fr.ludos.core.item.level.LevelItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpecialItemTest {
	// Helper class for testing
	private static class TestSpecialItem extends SpecialItem<TestSpecialItem> {
		public final static String ID = "test_item";


		protected TestSpecialItem(SpecialItem.ItemData info, Events events) {
			super(info, events);
		}

		@Override
		public Component getName() {
			return Component.text("Test Item");
		}
		@Override
		public List<Component> getLore() {
			return Collections.emptyList();
		}

		public static abstract class Events extends SpecialItem.Events<TestSpecialItem> {
			protected Events(Game game, Info info) {
				super(game, info);
			}
			@Override
			public String getTypeId() {
				return ID;
			}

			@Override
			protected TestSpecialItem getItemInternal(fr.ludos.core.item.SpecialItem.ItemData info) {
				return new TestSpecialItem(info, this);
			}
			@Override
			protected TestSpecialItem createItemInternal(Player owner) {
				return new TestSpecialItem(new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_SWORD), owner), this);
			}
		};
	}


	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private Group mockGroup;
	private PlayerMock owner;
	private TestSpecialItem.Events testEvents;


	@BeforeAll
	void setUpAll() {
		server = MockBukkit.mock();
	}

	@AfterAll
	void tearDownAll() {
		MockBukkit.unmock();
	}


	@BeforeEach
	void setUp() {
		mockLudos = mock(Ludos.class);
		mockGame = mock(Game.class);
		mockGroup = mock(Group.class);
		owner = server.addPlayer("Owner");


		when(mockLudos.getServer()).thenReturn(server);
		when(mockGame.getPlugin()).thenReturn(mockLudos);
		when(mockGame.ludos()).thenReturn(mockLudos);
		when(mockGame.getGroup()).thenReturn(mockGroup);
		when(mockGroup.isPlayer(any())).thenReturn(true);
		when(mockGroup.getOnlinePlayers()).thenReturn(Set.of(owner));


		// Setup ItemMeta mocks
		ItemStack stack = mock(ItemStack.class);
		ItemMeta meta = mock(ItemMeta.class);
		PersistentDataContainer container = mock(PersistentDataContainer.class);

		when(stack.getType()).thenReturn(Material.DIAMOND_SWORD);
		when(stack.getItemMeta()).thenReturn(meta);
		when(meta.getPersistentDataContainer()).thenReturn(container);
		when(container.has(any(), any())).thenReturn(false); // Initially no data


		testEvents = new TestSpecialItem.Events(mockGame, new SpecialItem.Events.Info(ItemSlot.HOTBAR_1, false)) {};
	}


	@Test
	@DisplayName("Should initialize item with correct metadata and PDC")
	void testInitializeItem() {
		TestSpecialItem testItem = testEvents.createItem(owner);

		ItemStack stack = testItem.getStack();
		assertNotNull(stack);

		assertTrue(stack.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE));


		ItemMeta meta = stack.getItemMeta();
		assertNotNull(meta);

		assertTrue(meta.isUnbreakable());


		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		assertNotNull(pdc);

		assertTrue(pdc.has(SpecialItem.OWNER_KEY, PersistentDataType.STRING));
		assertEquals(owner.getUniqueId().toString(), pdc.get(SpecialItem.OWNER_KEY, PersistentDataType.STRING));

		assertTrue(pdc.has(SpecialItem.TYPE_ID_KEY, PersistentDataType.STRING));
		assertEquals(testItem.getTypeId(), pdc.get(SpecialItem.TYPE_ID_KEY, PersistentDataType.STRING));

		assertTrue(pdc.has(SpecialItem.ITEM_ID_KEY, PersistentDataType.STRING));
		assertEquals(testItem.getItemId().toString(), pdc.get(SpecialItem.ITEM_ID_KEY, PersistentDataType.STRING));
	}

	@Test
	@DisplayName("Test Item id persistence")
	void testGetSpecialItemId() {
		TestSpecialItem item = testEvents.createItem(owner);
		assertEquals(item.getItemId(), SpecialItemInterface.getSpecialItemId(item.getStack(), mockGame));

		TestSpecialItem otherItem = testEvents.createItem(owner);
		assertNotEquals(item.getItemId(), SpecialItemInterface.getSpecialItemId(otherItem.getStack(), mockGame));

		ItemStack itemWithoutMeta = spy(item.getStack());
		when(itemWithoutMeta.getItemMeta()).thenReturn(null);
		assertNull(SpecialItemInterface.getSpecialItemId(itemWithoutMeta, mockGame));

		ItemMeta meta = item.getStack().getItemMeta();
		meta.getPersistentDataContainer().set(SpecialItem.ITEM_ID_KEY, PersistentDataType.STRING, null);
		item.getStack().setItemMeta(meta);
		assertNull(SpecialItemInterface.getSpecialItemId(item.getStack(), mockGame));

		ItemStack trashItem = spy(new ItemStack(Material.ACACIA_BOAT));
		assertNull(SpecialItemInterface.getSpecialItemId(trashItem, mockGame));

		assertNull(SpecialItemInterface.getSpecialItemId(null, mockGame));
	}

	@Test
	@DisplayName("Test owner persistence")
	void testGetSpecialItemOwner() {
		TestSpecialItem item = testEvents.createItem(owner);
		assertEquals(owner, SpecialItemInterface.getSpecialItemOwner(item.getStack()));

		ItemStack itemWithoutMeta = spy(item.getStack());
		when(itemWithoutMeta.getItemMeta()).thenReturn(null);
		assertNull(SpecialItemInterface.getSpecialItemOwner(itemWithoutMeta));

		ItemStack trashItem = spy(new ItemStack(Material.ACACIA_BOAT));
		assertNull(SpecialItemInterface.getSpecialItemOwner(trashItem));

		assertNull(SpecialItemInterface.getSpecialItemOwner(null));
	}


	@Test
	@DisplayName("Should check cooldown and prevent use if too soon")
	void testRefreshUseCooldown() {
		TestSpecialItem testItem = new TestSpecialItem(new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_SWORD), owner), testEvents);
		// Mock cooldown to return 2 (less than 4)
		when(owner.getCooldown(Material.DIAMOND_SWORD)).thenReturn(2);


		assertFalse(testItem.refreshUseCooldown());


		// Mock cooldown to return 0
		when(owner.getCooldown(Material.DIAMOND_SWORD)).thenReturn(0);


		assertTrue(testItem.refreshUseCooldown());
		verify(owner).setCooldown(Material.DIAMOND_SWORD, 4);
	}

	@Test
	@DisplayName("Equal operation should work")
	void testEquals() {
		TestSpecialItem item = testEvents.createItem(owner);
		TestSpecialItem copyItem = testEvents.getItem(item.getStack());

		assertEquals(item, copyItem);
		assertTrue(item == copyItem);
		assertFalse(item.equals(null));
		assertFalse(item.equals(mock(LevelItem.class)));

		TestSpecialItem otherItem = spy(copyItem);

		PlayerMock otherOwner = server.addPlayer("OtherOwner");
		when(otherItem.getOwner()).thenReturn(otherOwner);
		assertFalse(item.equals(otherItem));

		when(otherItem.getTypeId()).thenReturn("other_type_id");
		assertFalse(item.equals(otherItem));

		when(otherItem.getItemId()).thenReturn(UUID.randomUUID());
		assertFalse(item.equals(otherItem));
	}

	@Test
	@DisplayName("Should find same item instance in inventory")
	void testFindIn() {
		Inventory inventory = mock(Inventory.class);
		TestSpecialItem item = testEvents.createItem(owner);

		when(inventory.getContents()).thenReturn(new ItemStack[]{null, new ItemStack(Material.ACACIA_BOAT), item.getStack()});


		TestSpecialItem found = SpecialItem.findOne(inventory, testEvents::getItem);

		assertNotNull(found);
		assertEquals(item, found);
		assertTrue(item == found);
	}


	@Test
	@DisplayName("Should return null if item not in inventory")
	void testFindInNotPresent() {
		Inventory inventory = mock(Inventory.class);
		when(inventory.getContents()).thenReturn(new ItemStack[]{null, new ItemStack(Material.ACACIA_BOAT), new ItemStack(Material.STONE)});


		TestSpecialItem found = SpecialItem.findOne(inventory, testEvents::getItem);
		assertNull(found);
	}

	@Test
	@DisplayName("Should find item in inventory")
	void testContainedInPositive() {
		PlayerInventory inventory = owner.getInventory();
		inventory.clear();

		inventory.setItem(ItemSlot.MID_6.ordinal(), null);
		inventory.setItem(ItemSlot.MID_7.ordinal(), new ItemStack(Material.ACACIA_BOAT));

		assertFalse(TestSpecialItem.containedIn(inventory, testEvents::getItem));

		TestSpecialItem item = testEvents.createItem(owner);
		inventory.setItem(ItemSlot.BOOTS.ordinal(), item.getStack());

		assertTrue(TestSpecialItem.containedIn(inventory, testEvents::getItem));
	}

	@Test
	@DisplayName("Should refresh player inventory if item missing")
	void testRefreshPlayerInventory() {
		PlayerInventory inventory = owner.getInventory();
		inventory.clear();

		testEvents.refreshAllPlayerInventories();

		assertNotNull(inventory.getItem(ItemSlot.HOTBAR_1.ordinal()));
	}


	@Test
	@DisplayName("Should remove items from player inventory")
	void testRemoveFromPlayerInventory() {
		TestSpecialItem testItem = testEvents.createItem(owner);
		PlayerInventory inventory = owner.getInventory();

		ItemStack trashItem = new ItemStack(Material.ACACIA_BOAT);

		inventory.setItem(ItemSlot.HOTBAR_1.ordinal(), testItem.getStack());
		inventory.setItem(ItemSlot.OFFHAND.ordinal(), trashItem);
		inventory.setItem(ItemSlot.BOOTS.ordinal(), trashItem);
		inventory.setItem(ItemSlot.LEGGINGS.ordinal(), trashItem);
		inventory.setItem(ItemSlot.CHESTPLATE.ordinal(), trashItem);
		inventory.setItem(ItemSlot.HELMET.ordinal(), trashItem);
		testEvents.removeFromPlayerInventory(owner);

		assertNull(inventory.getItem(ItemSlot.HOTBAR_1.ordinal()));
		assertEquals(trashItem, inventory.getItem(ItemSlot.OFFHAND.ordinal()));
		assertEquals(trashItem, inventory.getItem(ItemSlot.BOOTS.ordinal()));
		assertEquals(trashItem, inventory.getItem(ItemSlot.LEGGINGS.ordinal()));
		assertEquals(trashItem, inventory.getItem(ItemSlot.CHESTPLATE.ordinal()));
		assertEquals(trashItem, inventory.getItem(ItemSlot.HELMET.ordinal()));


		inventory.setItem(ItemSlot.OFFHAND.ordinal(), testItem.getStack());
		inventory.setItem(ItemSlot.BOOTS.ordinal(), testItem.getStack());
		inventory.setItem(ItemSlot.LEGGINGS.ordinal(), testItem.getStack());
		inventory.setItem(ItemSlot.CHESTPLATE.ordinal(), testItem.getStack());
		inventory.setItem(ItemSlot.HELMET.ordinal(), testItem.getStack());
		testEvents.removeFromPlayerInventory(owner);

		assertNull(inventory.getItem(ItemSlot.OFFHAND.ordinal()));
		assertNull(inventory.getItem(ItemSlot.BOOTS.ordinal()));
		assertNull(inventory.getItem(ItemSlot.LEGGINGS.ordinal()));
		assertNull(inventory.getItem(ItemSlot.CHESTPLATE.ordinal()));
		assertNull(inventory.getItem(ItemSlot.HELMET.ordinal()));
	}


	@Test
	@DisplayName("Should build lore with correct color and formatting")
	void testBuildDataLore() {
		Component lore = SpecialItem.buildDataLore("Power", 5);


		assertNotNull(lore);
		assertTrue(lore.toString().contains("Power:"));
	}
	@Test
	@DisplayName("Should remove item from all player inventories")
	void testRemoveFromAllInventories() {
		PlayerInventory inventory1 = owner.getInventory();
		PlayerMock player2 = server.addPlayer("Player2");
		PlayerInventory inventory2 = player2.getInventory();
		TestSpecialItem item1 = testEvents.createItem(owner);
		inventory1.setItem(ItemSlot.HOTBAR_1.ordinal(), item1.getStack());
		TestSpecialItem item2 = testEvents.createItem(player2);
		inventory2.setItem(ItemSlot.HOTBAR_1.ordinal(), item2.getStack());

		when(mockGroup.getOnlinePlayers()).thenReturn(Set.of(owner, player2));


		testEvents.removeFromAllInventories();


		assertNull(inventory1.getItem(ItemSlot.HOTBAR_1.ordinal()));
		assertNull(inventory2.getItem(ItemSlot.HOTBAR_1.ordinal()));
	}


	@Test
	@DisplayName("Should refresh player inventory for all online players")
	void testRefreshAllPlayerInventories() {
		Inventory inventory1 = owner.getInventory();
		PlayerMock player2 = server.addPlayer("Player2");
		Inventory inventory2 = player2.getInventory();
		inventory1.clear();
		inventory2.clear();

		when(mockGroup.getOnlinePlayers()).thenReturn(Set.of(owner, player2));

		testEvents.refreshAllPlayerInventories();


		assertNotNull(inventory1.getItem(ItemSlot.HOTBAR_1.ordinal()));
		assertNotNull(inventory2.getItem(ItemSlot.HOTBAR_1.ordinal()));
	}


	@Test
	@DisplayName("Should refresh player inventory only once if item already present")
	void testRefreshPlayerInventoryAlreadyPresent() {
		PlayerMock holder = owner;
		PlayerInventory inventory = holder.getInventory();
		TestSpecialItem item = testEvents.createItem(holder);
		inventory.setItem(ItemSlot.HOTBAR_1.ordinal(), item.getStack());

		assertTrue(TestSpecialItem.containedIn(inventory, testEvents::getItem));


		inventory = spy(inventory);
		testEvents.refreshPlayerInventory(owner);

		assertEquals(1, TestSpecialItem.findAll(inventory, testEvents::getItem).size());

		verify(inventory, never()).addItem(any(ItemStack.class));
		verify(inventory, never()).setItem(anyInt(), any(ItemStack.class));
	}


	@Test
	@DisplayName("Should cancel drop event for special item if cannot drop")
	void testOnPlayerDropItemCancel() {
		PlayerDropItemEvent dropEvent = mock(PlayerDropItemEvent.class);
		Item mockItem = mock(Item.class);

		when(dropEvent.getPlayer()).thenReturn(owner);
		TestSpecialItem item = testEvents.createItem(owner);
		when(mockItem.getItemStack()).thenReturn(item.getStack());
		when(dropEvent.getItemDrop()).thenReturn(mockItem);


		testEvents.onPlayerDropItem(dropEvent);

		verify(dropEvent).setCancelled(true);
	}


	@Test
	@DisplayName("Should allow drop event if item can drop")
	void testOnPlayerDropItemAllow() {
		SpecialItem.Events<TestSpecialItem> dropableEvents = new TestSpecialItem.Events(mockGame, new Info(null, true)) {};

		PlayerDropItemEvent dropEvent = mock(PlayerDropItemEvent.class);
		Item mockItem = mock(Item.class);

		when(dropEvent.getPlayer()).thenReturn(owner);
		TestSpecialItem item = testEvents.createItem(owner);
		when(mockItem.getItemStack()).thenReturn(item.getStack());
		when(dropEvent.getItemDrop()).thenReturn(mockItem);


		dropableEvents.onPlayerDropItem(dropEvent);

		verify(dropEvent, never()).setCancelled(true);
	}

	@Test
	@DisplayName("Should allow click event if item can drop")
	void testOnPlayerClickItemAllow() {
		SpecialItem.Events<TestSpecialItem> dropableEvents = new TestSpecialItem.Events(mockGame, new Info(null, true)) {};
		InventoryClickEvent clickEvent = mock(InventoryClickEvent.class);


		dropableEvents.onInventoryClickItem(clickEvent);

		verify(clickEvent, never()).setResult(eq(Event.Result.DENY));
	}


	@Test
	@DisplayName("Should allow click event if moving in inventory")
	void testOnPlayerMoveItemAllow() {
		Inventory inventory = mock(Inventory.class);

		InventoryClickEvent clickEvent = mock(InventoryClickEvent.class);
		TestSpecialItem item = testEvents.createItem(owner);

		when(clickEvent.getWhoClicked()).thenReturn(owner);
		when(clickEvent.getCursor()).thenReturn(item.getStack());
		when(clickEvent.getInventory()).thenReturn(inventory);
		when(clickEvent.getAction()).thenReturn(InventoryAction.MOVE_TO_OTHER_INVENTORY);
		when(inventory.getType()).thenReturn(InventoryType.PLAYER);


		testEvents.onInventoryClickItem(clickEvent);

		when(inventory.getType()).thenReturn(InventoryType.CRAFTING);

		testEvents.onInventoryClickItem(clickEvent);

		verify(clickEvent, never()).setResult(eq(Event.Result.DENY));
	}


	@Test
	@DisplayName("Should deny moving special item to other inventory")
	void testOnInventoryClickItemDeny() {
		InventoryClickEvent clickEvent = mock(InventoryClickEvent.class);
		Inventory inventory = mock(Inventory.class);
		TestSpecialItem item = testEvents.createItem(owner);

		when(clickEvent.getWhoClicked()).thenReturn(owner);
		when(clickEvent.getCursor()).thenReturn(item.getStack());
		when(clickEvent.getAction()).thenReturn(InventoryAction.MOVE_TO_OTHER_INVENTORY);
		when(clickEvent.getInventory()).thenReturn(inventory);
		when(inventory.getType()).thenReturn(InventoryType.CHEST);


		testEvents.onInventoryClickItem(clickEvent);

		verify(clickEvent).setResult(eq(Event.Result.DENY));
	}


	@Test
	@DisplayName("Should allow click if item not in cursor")
	void testOnInventoryClickItemAllow() {
		InventoryClickEvent clickEvent = mock(InventoryClickEvent.class);
		HumanEntity whoClicked = owner;
		Inventory inventory = mock(Inventory.class);
		ItemStack empty = new ItemStack(Material.AIR);

		when(clickEvent.getWhoClicked()).thenReturn(whoClicked);
		when(clickEvent.getInventory()).thenReturn(inventory);
		when(clickEvent.getInventory().getType()).thenReturn(InventoryType.CHEST);
		when(clickEvent.getAction()).thenReturn(InventoryAction.MOVE_TO_OTHER_INVENTORY);
		when(clickEvent.getCursor()).thenReturn(empty);


		testEvents.onInventoryClickItem(clickEvent);

		verify(clickEvent, never()).setResult(eq(Event.Result.DENY));
	}


	@Test
	@DisplayName("Should refresh all player inventories overloads")
	void testRefreshAllPlayerInventoriesInGame() {
		when(mockGame.getActiveItems()).thenReturn(Set.of(testEvents));

		SpecialItem.Events.refreshPlayerInventory(mockGame, owner);
		assertNotNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));

		SpecialItem.Events.removeFromPlayerInventory(mockGame, owner);
		assertNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));

		SpecialItem.Events.refreshAllPlayerInventories(mockGame);
		assertNotNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));

		SpecialItem.Events.removeFromAllPlayerInventories(mockGame);
		assertNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));
	}

	@Test
	@DisplayName("Should cancel item spawn event for special item")
	void testOnItemSpawnCancel() {
		ItemSpawnEvent spawnEvent = mock(ItemSpawnEvent.class);
		Item itemEntity = mock(Item.class);

		TestSpecialItem item = testEvents.createItem(owner);
		when(spawnEvent.getEntity()).thenReturn(itemEntity);
		when(itemEntity.getItemStack()).thenReturn(item.getStack());


		testEvents.onItemSpawn(spawnEvent);

		verify(spawnEvent).setCancelled(true);
	}


	@Test
	@DisplayName("Should refresh inventory on player join")
	void testOnPlayerJoin() {
		PlayerJoinEvent joinEvent = mock(PlayerJoinEvent.class);
		when(joinEvent.getPlayer()).thenReturn(owner);


		testEvents.onPlayerJoin(joinEvent);

		assertNotNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));
	}


	@Test
	@DisplayName("Should refresh inventory on player respawn")
	void testOnPlayerRespawn() {
		PlayerRespawnEvent respawnEvent = mock(PlayerRespawnEvent.class);
		when(respawnEvent.getPlayer()).thenReturn(owner);


		testEvents.onPlayerRespawn(respawnEvent);

		assertNotNull(owner.getInventory().getItem(ItemSlot.HOTBAR_1.ordinal()));
	}


	@Test
	@DisplayName("Should allow drop if info.canDrop is true")
	void testInfoCanBeDropped() {
		Info info = new Info(true);
		assertTrue(info.canDrop());
	}


	@Test
	@DisplayName("Should disallow drop if info.canDrop is false")
	void testInfoCannotBeDropped() {
		Info info = new Info(ItemSlot.HOTBAR_1, false);
		assertFalse(info.canDrop());
	}


	@Test
	@DisplayName("Should have default slot if provided")
	void testInfoWithSlot() {
		Info info = new Info(ItemSlot.HOTBAR_1);
		assertEquals(ItemSlot.HOTBAR_1, info.slot());
		assertFalse(info.canDrop());
	}


	@Test
	@DisplayName("Should have null slot and false drop by default")
	void testInfoDefault() {
		Info info = new Info();
		assertNull(info.slot());
		assertFalse(info.canDrop());
	}


	@Test
	@DisplayName("Should not cancel drop if player not in group")
	void testOnPlayerDropItemNotInGroup() {
		PlayerDropItemEvent dropEvent = mock(PlayerDropItemEvent.class);
		OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
		when(offlinePlayer.getPlayer()).thenReturn(owner);
		when(dropEvent.getPlayer()).thenReturn(owner);


		when(mockGroup.isPlayer(owner)).thenReturn(false); // Not in group


		testEvents.onPlayerDropItem(dropEvent);


		verify(dropEvent, never()).setCancelled(true);
	}

	@Test
	@DisplayName("Should create correct action annotation with keybind and action")
	void testGetActionAnnotation() {
		String keybind = "key.keyboard.left.alt";
		Component action = Component.text("Attack").color(net.kyori.adventure.text.format.NamedTextColor.RED);

		Component result = SpecialItemInterface.getActionAnnotation(keybind, action);

		assertNotNull(result);

		String serialized = result.toString();


		assertTrue(serialized.contains("Press"), "Should contain 'Press'");
		assertTrue(serialized.contains(keybind), "Should contain the keybind");
		assertTrue(serialized.contains(" to "), "Should contain ' to '");
		assertTrue(serialized.contains("Attack"), "Should contain the action text");


		assertTrue(result.decoration(TextDecoration.ITALIC) == TextDecoration.State.FALSE, "Italic decoration should be false");

		assertEquals(action, result.children().get(2)); // "Press" (itself), keybind (0), " to " (1), action (2)
	}


	@Test
	@DisplayName("Should handle different keybinds correctly")
	void testGetActionAnnotationDifferentKeybind() {
		String keybind = "key.mouse.left";
		Component action = Component.text("Interact");


		Component result = SpecialItemInterface.getActionAnnotation(keybind, action);

		assertNotNull(result);
		assertTrue(result.toString().contains(keybind));
		assertTrue(result.decoration(TextDecoration.ITALIC) == TextDecoration.State.FALSE);
	}


	@Test
	@DisplayName("Should handle complex action components")
	void testGetActionAnnotationComplexAction() {
		String keybind = "key.keyboard.e";
		Component action = Component.text("Open Chest")
			.color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
			.decoration(TextDecoration.BOLD, TextDecoration.State.TRUE);


		Component result = SpecialItemInterface.getActionAnnotation(keybind, action);


		assertNotNull(result);
		assertTrue(result.toString().contains("Open Chest"));

		// The outer component should force italic false, but the inner action keeps its own decorations
		assertEquals(TextDecoration.State.FALSE, result.decoration(TextDecoration.ITALIC));
	}
}