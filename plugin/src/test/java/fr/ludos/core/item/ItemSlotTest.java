package fr.ludos.core.item;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemSlotTest {
	private ServerMock server;


	@BeforeAll
	void setUpAll() {
		server = MockBukkit.mock();
	}

	@AfterAll
	void tearDownAll() {
		MockBukkit.unmock();
	}

	@Test
	@DisplayName("Should set item in specific hotbar slot if empty")
	void testSetItemInHotbar() {
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack item = new ItemStack(org.bukkit.Material.STONE);

		when(inventory.getItem(ItemSlot.HOTBAR_1.ordinal())).thenReturn(null);


		ItemSlot.set(ItemSlot.HOTBAR_1, item, inventory);

		verify(inventory).setItem(ItemSlot.HOTBAR_1.ordinal(), item);
		verify(inventory, never()).addItem(any(ItemStack.class));
	}

	@Test
	@DisplayName("Should add item to inventory if target slot is occupied")
	void testSetItemInOccupiedSlot() {
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack item = new ItemStack(org.bukkit.Material.STONE);
		ItemStack occupied = new ItemStack(org.bukkit.Material.DIRT);

		when(inventory.getItem(ItemSlot.HOTBAR_1.ordinal())).thenReturn(occupied);


		ItemSlot.set(ItemSlot.HOTBAR_1, item, inventory);

		verify(inventory).addItem(item);
	}

	@Test
	@DisplayName("Should add item if slot is null")
	void testSetItemWithNullSlot() {
		PlayerInventory inventory = mock(PlayerInventory.class);
		ItemStack item = new ItemStack(org.bukkit.Material.STONE);


		ItemSlot.set(null, item, inventory);

		verify(inventory).addItem(item);
	}

	@Test
	@DisplayName("Should support all enum slots")
	void testEnumValues() {
		assertNotNull(ItemSlot.HOTBAR_1);
		assertNotNull(ItemSlot.CHESTPLATE);
		assertNotNull(ItemSlot.OFFHAND);
		assertEquals(42, ItemSlot.values().length);
	}
}