package fr.ludos.core.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
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
import fr.ludos.core.item.level.LevelItemInterface;
import fr.ludos.core.item.level.LevelState;
import fr.ludos.core.item.level.LevelValue;
import net.kyori.adventure.text.Component;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiLevelBranchItemTest {
	private enum TestBranch implements MultiLevelBranchItem.Branch {
		MODE_A(3, level -> (level + 1) * 10.0),
		MODE_B(5, level -> (level + 1) * 20.0),
		MODE_C(0, level -> 0.0); // MAX_LEVEL

		private final int maxLevel;
		private final Function<Integer, Double> xpThresholdFunction;

		TestBranch(int maxLevel, Function<Integer, Double> xpThresholdFunction) {
			this.maxLevel = maxLevel;
			this.xpThresholdFunction = xpThresholdFunction;
		}

		@Override
		public String id() {
			return name().toLowerCase();
		}

		@Override
		public Component getName() {
			return Component.text(name());
		}

		@Override
		public Component getDescription() {
			return Component.text("Description for " + name());
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }

		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onDeselectBranch(SpecialItemInterface item) { }

		@Override
		public void onSelectBranch(SpecialItemInterface item) { }

		@Override
		public void onSetLevel(int level, SpecialItemInterface item) { }

		@Override
		public void onUnsetLevel(int level, SpecialItemInterface item) { }

		@Override
		public int maxLevel() {
			return maxLevel;
		}

		@Override
		public double xpThreshold(@NotNull Integer level) {
			return xpThresholdFunction.apply(level);
		}
	}

	private static class TestMultiLevelBranchItem extends MultiLevelBranchItem<TestMultiLevelBranchItem, TestBranch> {
		public final static String ID = "test_multi_level_branch_item";

		protected TestMultiLevelBranchItem(ItemData<TestBranch> info, Events<TestMultiLevelBranchItem, TestBranch> events) {
			super(info, events);
		}

		@Override
		public Component getName() {
			return Component.text("Test Multi Level Branch Item");
		}

		@Override
		public List<Component> getLore() {
			List<Component> lore = super.getLore();
			lore.add(getBranch().getDescription());
			return lore;
		}

		public static class TestEvents extends Events<TestMultiLevelBranchItem, TestBranch> {
			public TestEvents(Game game, Info info) {
				super(Arrays.asList(TestBranch.values()), game, info);
			}

			@Override
			public String getTypeId() {
				return ID;
			}

			@Override
			protected TestMultiLevelBranchItem getItemInternal(ItemData<TestBranch> info) {
				return new TestMultiLevelBranchItem(info, this);
			}

			@Override
			protected TestMultiLevelBranchItem createItemInternal(MultiLevelData levels, BranchData<TestBranch> data, Player owner) {
				return new TestMultiLevelBranchItem(
					new ItemData<TestBranch>(levels, data, new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_AXE), owner)),
					this
				);
			}
		}
	}

	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private Group mockGroup;
	private PlayerMock owner;
	private TestMultiLevelBranchItem.TestEvents testEvents;

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

		testEvents = new TestMultiLevelBranchItem.TestEvents(mockGame, new SpecialItem.Events.Info(ItemSlot.HOTBAR_1, false));
	}

	@Test
	@DisplayName("Should initialize with correct branch and level states")
	void testInitialization() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		assertNotNull(item);
		assertNotNull(item.getBranch());
		assertEquals(item.branches.values().iterator().next(), item.getBranch()); // Default first branch

		// Verify level states for all branches
		Map<String, LevelState> levelStates = item.getLevelStates();
		assertNotNull(levelStates);
		assertEquals(3, levelStates.size()); // 3 branches

		for (TestBranch branch : TestBranch.values()) {
			LevelState state = levelStates.get(branch.id());
			assertNotNull(state);
			assertEquals(0, state.level());
			assertEquals(0.0, state.xp(), 0.001);
		}
	}

	@Test
	@DisplayName("Should handle branch switching and preserve level states")
	void testBranchSwitching() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		assertTrue(item.switchBranch(TestBranch.MODE_A));
		item.addXp(15.0);
		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);


		assertTrue(item.switchBranch(TestBranch.MODE_B));
		assertEquals(TestBranch.MODE_B, item.getBranch());

		assertEquals(0, item.level());
		assertEquals(0.0, item.xp(), 0.001);

		item.addXp(25.0);
		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);


		assertTrue(item.switchBranch(TestBranch.MODE_A));
		assertEquals(TestBranch.MODE_A, item.getBranch());

		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle level progression independently per branch")
	void testLevelProgressionPerBranch() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		item.switchBranch(TestBranch.MODE_A);
		item.addXp(15.0);
		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);

		item.addXp(20.0);
		assertEquals(2, item.level());
		assertEquals(5.0, item.xp(), 0.001);


		item.switchBranch(TestBranch.MODE_B);

		item.addXp(70.0);
		assertEquals(2, item.level());
		assertEquals(10.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle max level per branch")
	void testMaxLevelPerBranch() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		assertTrue(item.switchBranch(TestBranch.MODE_A));
		item.addXp(10.0);
		item.addXp(20.0);
		item.addXp(30.0);

		assertEquals(3, item.level());
		assertEquals(0.0, item.xp(), 0.001);

		item.addXp(100.0);
		assertEquals(3, item.level());


		item.switchBranch(TestBranch.MODE_C);
		item.addXp(100.0);
		assertEquals(0, item.level());
		assertEquals(0.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should generate correct lore with branch, level and XP")
	void testGetLore() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		item.switchBranch(TestBranch.MODE_A);
		item.addXp(15.0);

		List<Component> lore = item.getLore();
		assertNotNull(lore);
		assertTrue(lore.size() >= 3);


		boolean hasBranch = false;
		boolean hasLevel = false;
		boolean hasXp = false;

		for (Component line : lore) {
			String str = line.toString();
			if (str.contains("Mode:")) hasBranch = true;
			if (str.contains("Level:")) hasLevel = true;
			if (str.contains("XP:")) hasXp = true;
		}

		assertTrue(hasBranch, "Lore should contain branch info");
		assertTrue(hasLevel, "Lore should contain level info");
		assertTrue(hasXp, "Lore should contain XP info");
	}

	@Test
	@DisplayName("Should save and load level values from item stack")
	void testSaveAndLoadLevelValues() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		// Add XP to MODE_A
		item.switchBranch(TestBranch.MODE_A);
		item.addXp(15.0);

		TestBranch currentBranch = item.getBranch();
		assertEquals(TestBranch.MODE_A, currentBranch);
		assertTrue(TestBranch.MODE_A == currentBranch);


		// Add XP to MODE_B
		item.switchBranch(TestBranch.MODE_B);
		item.addXp(25.0);

		currentBranch = item.getBranch();
		assertEquals(TestBranch.MODE_B, currentBranch);
		assertTrue(TestBranch.MODE_B == currentBranch);


		Map<String, LevelValue> loadedLevels = item.getLevelValues();
		assertNotNull(loadedLevels);
		assertEquals(3, loadedLevels.size());


		LevelValue modeA = loadedLevels.get(TestBranch.MODE_A.id());
		assertNotNull(modeA);
		assertEquals(1, modeA.level());
		assertEquals(5.0, modeA.xp(), 0.001);

		LevelValue modeB = loadedLevels.get(TestBranch.MODE_B.id());
		assertNotNull(modeB);
		assertEquals(1, modeB.level());
		assertEquals(5.0, modeB.xp(), 0.001);

		LevelValue modeC = loadedLevels.get(TestBranch.MODE_C.id());
		assertNotNull(modeC);
		assertEquals(0, modeC.level());
		assertEquals(0.0, modeC.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle item switching with level data")
	void testItemSwitchWithData() {
		PlayerMock player = server.addPlayer("Player2");
		PlayerInventory inventory = player.getInventory();

		TestMultiLevelBranchItem item1 = testEvents.createItem(player);
		item1.switchBranch(TestBranch.MODE_A);
		item1.addXp(15.0); // MODE_A, Level 1

		item1.switchBranch(TestBranch.MODE_B);
		item1.addXp(25.0); // MODE_B, Level 1

		TestMultiLevelBranchItem item2 = testEvents.createItem(player);
		item1.switchBranch(TestBranch.MODE_A);
		item2.addXp(30.0); // MODE_A, Level 3 (MAX)
		item2.switchBranch(TestBranch.MODE_C);
		item2.addXp(50.0); // MODE_C, Level 1

		inventory.setItem(0, item1.getStack());
		inventory.setItem(1, item2.getStack());

		// Create mock event for switching
		PlayerItemHeldEvent switchEvent = mock(PlayerItemHeldEvent.class);
		when(switchEvent.getPlayer()).thenReturn(player);
		when(switchEvent.getPreviousSlot()).thenReturn(0);
		when(switchEvent.getNewSlot()).thenReturn(1);

		// Trigger the event handler manually to ensure no exceptions
		testEvents.onSwitchItem(switchEvent);
	}

	@Test
	@DisplayName("Should handle branch removal and level state cleanup")
	void testBranchRemoval() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		item.switchBranch(TestBranch.MODE_A);
		// Add XP to MODE_A
		item.addXp(15.0);

		// Remove MODE_A
		assertTrue(item.removeBranch(TestBranch.MODE_A));
		assertFalse(item.branches.containsValue(TestBranch.MODE_A));

		// Level state for MODE_A should be removed
		Map<String, LevelState> levelStates = item.getLevelStates();
		assertNull(levelStates.get(TestBranch.MODE_A.id()));

		// Add MODE_A back
		item.addBranch(TestBranch.MODE_A);
		assertNotNull(levelStates.get(TestBranch.MODE_A.id()));
		assertEquals(0, levelStates.get(TestBranch.MODE_A.id()).level());
		assertEquals(0.0, levelStates.get(TestBranch.MODE_A.id()).xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle level state initialization with custom XP threshold")
	void testLevelStateInitialization() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		// Switch to MODE_B
		item.switchBranch(TestBranch.MODE_B);

		// Add XP
		item.addXp(25.0); // Should reach level 1 (threshold=20)

		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle level up message")
	void testLevelUpMessage() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);
		assertTrue(item.switchBranch(TestBranch.MODE_A));
		item.addXp(10.0); // Trigger level up

		Component message = LevelItemInterface.getLevelUpMessage(item);
		assertNotNull(message);

		String messageText = message.toString();
		assertTrue(messageText.contains("Your"));
		assertTrue(messageText.contains("Test Multi Level Branch Item"));
		assertTrue(messageText.contains("has leveled up!"));
	}

	@Test
	@DisplayName("Should handle XP lore field for max level")
	void testXpLoreFieldMaxLevel() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		// Set to MAX level for MODE_A
		assertTrue(item.switchBranch(TestBranch.MODE_A));
		item.addXp(10.0); // Level 1
		item.addXp(20.0); // Level 2
		item.addXp(30.0); // Level 3 (MAX)

		Component xpLore = MultiLevelBranchItem.getBranchXpLoreField(item, TestBranch.MODE_A);
		assertNotNull(xpLore);

		String loreText = xpLore.toString();
		assertTrue(loreText.contains("XP:"));
		assertTrue(loreText.contains("MAX"));
	}

	@Test
	@DisplayName("Should handle null item stack in branch and level retrieval")
	void testNullItemStack() {
		LevelValue level = LevelItemInterface.levelFromItemStack(null, mockGame);
		assertNull(level);

		Map<String, LevelValue> levelValues = MultiLevelBranchItem.levelsFromItemStack(null, TestMultiLevelBranchItem.ID);
		assertNull(levelValues);

		ItemStack stackWithoutMeta = mock(ItemStack.class);
		when(stackWithoutMeta.getItemMeta()).thenReturn(null);

		level = LevelItemInterface.levelFromItemStack(stackWithoutMeta, mockGame);
		assertNull(level);

		levelValues = MultiLevelBranchItem.levelsFromItemStack(stackWithoutMeta, TestMultiLevelBranchItem.ID);
		assertNull(levelValues);
	}

	// @Test
	// @DisplayName("Should return the next branch in alphabetical order of id()")
	// void testGetNextBranchAlphabeticalOrder() {
	// 	TestBranchItem item = testEvents.createItem(owner);

	// 	// Start with first branch (alphabetically: ACTIVEn)
	// 	assertEquals(TestBranch.ACTIVE, item.getBranch());

	// 	// Get next branch
	// 	TestBranch nextBranch = item.getNextBranch();
	// 	assertEquals(TestBranch.CHARGED, nextBranch); // "charged" comes after "active" alphabetically

	// 	// Switch to CHARGED
	// 	item.switchBranch(TestBranch.CHARGED);

	// 	// Get next branch
	// 	nextBranch = item.getNextBranch();
	// 	assertEquals(TestBranch.RELOADING, nextBranch); // "reloading" comes after "charged"

	// 	// Switch to RELOADING
	// 	item.switchBranch(TestBranch.RELOADING);

	// 	// Get next branch (should cycle back to first)
	// 	nextBranch = item.getNextBranch();
	// 	assertEquals(TestBranch.ACTIVE, nextBranch);
	// }

	// @Test
	// @DisplayName("Should handle branch switching with custom order via id()")
	// void testGetNextBranchCustomIdOrder() {
	// 	// Create a custom branch enum with different id()
	// 	enum CustomBranch implements BranchItemInterface.Branch {
	// 		// Reverse alphabetical order in id()
	// 		ONE("a", "Branch One"),
	// 		TWO("b", "Branch Two"),
	// 		THREE("c", "Branch Three");

	// 		private final String id;
	// 		private final String name;

	// 		CustomBranch(String id, String name) {
	// 			this.id = id;
	// 			this.name = name;
	// 		}

	// 		@Override
	// 		public String id() {
	// 			return id;
	// 		}

	// 		@Override
	// 		public Component getName() {
	// 			return Component.text(name);
	// 		}

	// 		@Override
	// 		public Component getDescription() {
	// 			return Component.text("Description for " + name);
	// 		}

	// 		@Override
	// 		public void onEquip(SpecialItemInterface item) { }

	// 		@Override
	// 		public void onUnequip(SpecialItemInterface item) { }

	// 		@Override
	// 		public void onDeselectBranch(SpecialItemInterface item) { }

	// 		@Override
	// 		public void onSelectBranch(SpecialItemInterface item) { }
	// 	}

	// 	// Create item with custom branches
	// 	BranchItem.TestEvents<?> customEvents = new BranchItem.Events<TestBranchItem, CustomBranch>(
	// 		List.of(CustomBranch.ONE, CustomBranch.TWO, CustomBranch.THREE),
	// 		null,
	// 		mockGame,
	// 		new SpecialItem.Events.Info(ItemSlot.HOTBAR_1, false)
	// 	) {
	// 		@Override
	// 		protected TestBranchItem getItemInternal(BranchItem.ItemData<CustomBranch> info) {
	// 			return new TestBranchItem(info, this) {
	// 				@Override
	// 				public Component getName() {
	// 					return Component.text("Custom Branch Item");
	// 				}
	// 			};
	// 		}

	// 		@Override
	// 		protected TestBranchItem createItemInternal(BranchData<CustomBranch> data, Player owner) {
	// 			return new TestBranchItem(
	// 				new ItemData<>(data, new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_AXE), owner)),
	// 				this
	// 			);
	// 		}
	// 	};

	// 	TestBranchItem item = customEvents.createItem(owner);

	// 	// Start with first branch (ONE, id="a")
	// 	assertEquals(CustomBranch.ONE, item.getBranch());

	// 	// Get next branch
	// 	CustomBranch nextBranch = item.getNextBranch();
	// 	assertEquals(CustomBranch.TWO, nextBranch); // "b" comes after "a"

	// 	// Switch to TWO
	// 	item.switchBranch(CustomBranch.TWO);

	// 	// Get next branch
	// 	nextBranch = item.getNextBranch();
	// 	assertEquals(CustomBranch.THREE, nextBranch); // "c" comes after "b"

	// 	// Switch to THREE
	// 	item.switchBranch(CustomBranch.THREE);

	// 	// Get next branch (should cycle back to ONE)
	// 	nextBranch = item.getNextBranch();
	// 	assertEquals(CustomBranch.ONE, nextBranch);
	// }

	@Test
	@DisplayName("Should handle single branch")
	void testGetNextBranchSingleBranch() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		// Remove all branches except one
		for (TestBranch branch : TestBranch.values()) {
			if (branch != TestBranch.MODE_A) {
				item.removeBranch(branch);
			}
		}

		// Only one branch left
		assertEquals(TestBranch.MODE_A, item.getBranch());

		// Get next branch should return the same branch (cycle)
		TestBranch nextBranch = item.getNextBranch();
		assertEquals(TestBranch.MODE_A, nextBranch);
	}

	@Test
	@DisplayName("Should return null if no branches")
	void testGetNextBranchNoBranches() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		// Remove all branches
		for (TestBranch branch : TestBranch.values()) {
			item.removeBranch(branch);
		}

		// Should have no branches
		assertTrue(item.branches.isEmpty());

		// Get next branch should return null
		TestBranch nextBranch = item.getNextBranch();
		assertNull(nextBranch);
	}


	@Test
	@DisplayName("Should set branch and update persistent data")
	void testSetItemBranch() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		BranchItem.setItemBranch(item, TestBranch.MODE_B);

		ItemStack stack = item.getStack();
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		String savedBranchId = container.get(BranchItemInterface.BRANCH_KEY, PersistentDataType.STRING);
		assertEquals(TestBranch.MODE_B.id(), savedBranchId);

		assertEquals(TestBranch.MODE_B, item.getBranch());
	}

	@Test
	@DisplayName("Should call onSetBranch and onSelectBranch")
	void testSetItemBranchEvents() {
		TestMultiLevelBranchItem item = spy(testEvents.createItem(owner));

		TestBranch spyBranch = spy(TestBranch.MODE_C);

		BranchItem.setItemBranch(item, spyBranch);


		verify(item).onSetBranch(spyBranch);

		verify(spyBranch).onSelectBranch(item);
	}

	@Test
	@DisplayName("Should call onDeselectBranch on old branch")
	void testSetItemBranchDeselectOldBranch() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		TestBranch oldBranch = spy(item.getBranch());
		item.branches.put(oldBranch.id(), oldBranch);
		item.switchBranch(oldBranch);

		TestBranch newBranch = spy(item.getNextBranch());
		BranchItem.setItemBranch(item, newBranch);

		verify(oldBranch).onDeselectBranch(item);
		verify(newBranch).onSelectBranch(item);
	}

	@Test
	@DisplayName("Should update item lore after branch change")
	void testSetItemBranchUpdateLore() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		List<Component> initialLore = item.getLore();
		assertNotNull(initialLore);


		BranchItem.setItemBranch(item, TestBranch.MODE_B);

		List<Component> updatedLore = item.getLore();
		assertNotNull(updatedLore);
		assertNotSame(initialLore, updatedLore);


		boolean hasNewBranch = false;
		for (Component line : updatedLore) {
			if (line.toString().contains("Description for MODE_B")) {
				hasNewBranch = true;
				break;
			}
		}
		assertTrue(hasNewBranch, "Lore should contain the new branch name");
	}

	@Test
	@DisplayName("Should handle null branch")
	void testSetItemBranchNull() {
		TestMultiLevelBranchItem item = testEvents.createItem(owner);

		BranchItem.setItemBranch(item, null);

		assertNotNull(item.getBranch());


		ItemStack stack = item.getStack();
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();
		String savedBranchId = container.get(BranchItemInterface.BRANCH_KEY, PersistentDataType.STRING);

		assertNotNull(savedBranchId);
		assertEquals(item.branches.values().iterator().next().id(), savedBranchId);
	}

	@Test
	@DisplayName("Should handle branch switch event with PlayerItemHeldEvent")
	void testSetItemBranchWithEvent() {
		TestMultiLevelBranchItem item = (testEvents.createItem(owner));
		owner.getInventory().setItem(0, item.getStack());

		TestBranch mockBranch = spy(item.getBranch());
		item.branches.put(mockBranch.id(), mockBranch);
		BranchItem.setItemBranch(item, mockBranch);

		PlayerItemHeldEvent event = mock(PlayerItemHeldEvent.class);
		when(event.getPlayer()).thenReturn(owner);
		when(event.getPreviousSlot()).thenReturn(0);
		when(event.getNewSlot()).thenReturn(1);

		BranchItem.onSwitchItem(event, testEvents::getItem);

		verify(mockBranch, times(1)).onUnequip(item);


		when(event.getPreviousSlot()).thenReturn(1);
		when(event.getNewSlot()).thenReturn(0);

		BranchItem.onSwitchItem(event, testEvents::getItem);

		verify(mockBranch, times(1)).onEquip(item);
	}
}