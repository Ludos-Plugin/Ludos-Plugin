package fr.ludos.core.item.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.persistence.pdc.LevelValuePersistentDataType;
import net.kyori.adventure.text.Component;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LevelBranchItemTest {

	// Helper enum for testing Branch implementation
	private enum TestBranch implements BranchItemInterface.Branch {
		MODE_A("Mode A", "First mode"),
		MODE_B("Mode B", "Second mode"),
		MODE_C("Mode C", "Third mode");

		private final String name;
		private final String description;

		TestBranch(String name, String description) {
			this.name = name;
			this.description = description;
		}

		@Override
		public String id() {
			return name().toLowerCase();
		}

		@Override
		public net.kyori.adventure.text.Component getName() {
			return net.kyori.adventure.text.Component.text(name);
		}

		@Override
		public net.kyori.adventure.text.Component getDescription() {
			return net.kyori.adventure.text.Component.text(description);
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }

		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onDeselectBranch(SpecialItemInterface item) { }

		@Override
		public void onSelectBranch(SpecialItemInterface item) { }
	}

	// Helper enum for testing Level implementation (same as LevelItemTest for consistency)
	private enum TestLevel implements LevelItemInterface.Level<TestLevel> {
		LEVEL_1(10.0),
		LEVEL_2(25.0),
		LEVEL_3(50.0),
		MAX_LEVEL(0.0);

		private final double xpThreshold;

		TestLevel(double xpThreshold) {
			this.xpThreshold = xpThreshold;
		}

		@Override
		public double xpThreshold() {
			return xpThreshold;
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }

		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onSwitchToLevel(SpecialItemInterface item) { }

		@Override
		public void onSwitchOffLevel(SpecialItemInterface item) { }
	}

	// Helper class for testing LevelBranchItem
	private static class TestLevelBranchItem extends LevelBranchItem<TestLevelBranchItem, TestBranch, TestLevel> {
		public final static String ID = "test_level_branch_item";

		protected TestLevelBranchItem(Info<TestBranch, TestLevel> info, Events<TestLevelBranchItem, TestBranch, TestLevel> events) {
			super(info, events);
		}

		@Override
		public Component getName() {
			return Component.text("Test Level Branch Item");
		}

		public static class TestEvents extends Events<TestLevelBranchItem, TestBranch, TestLevel> {
			public TestEvents(Game game, Info info) {
				super(Arrays.asList(TestBranch.values()), game, info);
			}

			@Override
			public String getTypeId() {
				return ID;
			}

			@Override
			public List<TestLevel> getLevels() {
				return Arrays.asList(TestLevel.LEVEL_1, TestLevel.LEVEL_2, TestLevel.LEVEL_3, TestLevel.MAX_LEVEL);
			}

			@Override
			protected TestLevelBranchItem getItemInternal(LevelBranchItem.Info<TestBranch, TestLevel> info) {
				return new TestLevelBranchItem(info, this);
			}

			@Override
			protected TestLevelBranchItem createItemInternal(BranchData<TestBranch> branch, LevelItem.LevelData<TestLevel> level, Player owner) {
				return new TestLevelBranchItem(
					new LevelBranchItem.Info<TestBranch, TestLevel>(
						branch,
						level,
						new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_AXE), owner)
					), this
				);
			}
		}
	}

	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private Group mockGroup;
	private PlayerMock owner;
	private TestLevelBranchItem.TestEvents testEvents;

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

		testEvents = new TestLevelBranchItem.TestEvents(mockGame, new SpecialItem.Events.Info(ItemSlot.HOTBAR_1, false));
	}

	@Test
	@DisplayName("Equal operation should work for LevelBranchItem")
	void testEquals() {
		TestLevelBranchItem item = testEvents.createItem(owner);
		TestLevelBranchItem copyItem = testEvents.getItem(item.getStack());

		assertEquals(item, copyItem);
		assertTrue(item == copyItem);
		assertFalse(item.equals(null));
		assertFalse(item.equals(mock(LevelBranchItem.class)));

		TestLevelBranchItem otherItem = spy(copyItem);
		PlayerMock otherOwner = server.addPlayer("OtherOwner");
		when(otherItem.getOwner()).thenReturn(otherOwner);
		assertFalse(item.equals(otherItem));

		when(otherItem.getTypeId()).thenReturn("other_type_id");
		assertFalse(item.equals(otherItem));

		when(otherItem.getItemId()).thenReturn(UUID.randomUUID());
		assertFalse(item.equals(otherItem));
	}

	@Test
	@DisplayName("Should initialize with correct branch and level state")
	void testInitialization() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		assertNotNull(item);
		assertNotNull(item.levelState());
		assertNotNull(item.getBranch());
		assertEquals(testEvents.getBranches().values().iterator().next(), item.getBranch()); // Default first branch
		assertEquals(0, item.level());
		assertEquals(0.0, item.xp(), 0.001);

		// Verify PDC contains both branch and level data
		ItemStack stack = item.getStack();
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		assertTrue(container.has(BranchItemInterface.BRANCH_KEY, org.bukkit.persistence.PersistentDataType.STRING));
		assertTrue(container.has(LevelItemInterface.LEVEL_KEY, LevelValuePersistentDataType.INSTANCE));
	}

	@Test
	@DisplayName("Should handle branch switching")
	void testBranchSwitching() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		assertTrue(item.switchBranch(TestBranch.MODE_B));
		assertEquals(TestBranch.MODE_B, item.getBranch());

		assertTrue(item.switchBranch(TestBranch.MODE_C));
		assertEquals(TestBranch.MODE_C, item.getBranch());

		// Try to switch to non-existent branch
		assertFalse(item.switchBranch(null));
	}

	@Test
	@DisplayName("Should handle level progression independently of branch")
	void testLevelProgression() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		// Add XP to reach next level
		item.addXp(10.0);
		assertEquals(0.0, item.xp(), 0.001);
		assertEquals(1, item.level());

		// Add more XP
		item.addXp(15.0); // Total 15 XP relative to threshold
		assertEquals(1, item.level());
		assertEquals(15.0, item.xp(), 0.001);

		// Switch branch, level should remain
		item.switchBranch(TestBranch.MODE_B);
		assertEquals(1, item.level());
		assertEquals(15.0, item.xp(), 0.001);

		// Reach level 2
		item.addXp(10.0); // Total 25 XP
		assertEquals(2, item.level());
		assertEquals(0.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should retrieve correct level object for current and specific levels")
	void testGetLevelObject() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		TestLevel currentLevel = item.lvlObject();
		assertEquals(TestLevel.LEVEL_1, currentLevel);

		TestLevel level1 = item.lvlObject(1);
		assertEquals(TestLevel.LEVEL_2, level1);

		TestLevel level2 = item.lvlObject(2);
		assertEquals(TestLevel.LEVEL_3, level2);
	}

	@Test
	@DisplayName("Should generate correct lore with branch, level and XP")
	void testGetLore() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		List<Component> lore = item.getLore();
		assertNotNull(lore);
		assertTrue(lore.size() >= 3); // Should have base lore + Branch + Level + XP

		// Check branch lore
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
	@DisplayName("Should handle item switching with branch and level data")
	void testItemSwitchWithData() {
		PlayerMock player = server.addPlayer("Player2");
		PlayerInventory inventory = player.getInventory();

		TestLevelBranchItem item1 = testEvents.createItem(player);
		item1.addXp(15.0); // Level 1
		item1.switchBranch(TestBranch.MODE_B);

		TestLevelBranchItem item2 = testEvents.createItem(player);
		item2.addXp(30.0); // Level 2
		item2.switchBranch(TestBranch.MODE_C);

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
	@DisplayName("Should save and load branch and level value from item stack")
	void testSaveAndLoad() {
		TestLevelBranchItem item = testEvents.createItem(owner);
		item.addXp(15.0); // Level 1
		item.switchBranch(TestBranch.MODE_B);

		ItemStack stack = item.getStack();

		// Load Branch
		String loadedBranchId = BranchItemInterface.branchFromItemStack(stack, mockGame);
		assertEquals("mode_b", loadedBranchId);

		// Load Level
		LevelValue loadedLevel = LevelItemInterface.levelFromItemStack(stack, mockGame);
		assertNotNull(loadedLevel);
		assertEquals(1, loadedLevel.level());
		assertEquals(5.0, loadedLevel.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle max level correctly")
	void testMaxLevel() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		// Add XP to reach max level
		item.addXp(10.0); // Level 1
		item.addXp(25.0); // Level 2
		item.addXp(50.0); // Level 3 (MAX)

		assertEquals(3, item.level());
		assertEquals(0.0, item.xp(), 0.001);

		// Should not exceed max level
		item.addXp(100.0);
		assertEquals(3, item.level());
		assertEquals(0.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should generate correct level up message")
	void testLevelUpMessage() {
		TestLevelBranchItem item = testEvents.createItem(owner);
		item.addXp(10.0); // Trigger level up

		Component message = LevelItemInterface.getLevelUpMessage(item);
		assertNotNull(message);

		String messageText = message.toString();
		assertTrue(messageText.contains("Your"));
		assertTrue(messageText.contains("Test Level Branch Item"));
		assertTrue(messageText.contains("has leveled up!"));
	}

	@Test
	@DisplayName("Should handle concurrent branch and level changes")
	void testConcurrentChanges() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		for (int i = 0; i < 5; i++) {
			item.switchBranch(TestBranch.values()[i % TestBranch.values().length]);
			item.addXp(1.0);
		}

		assertNotNull(item.levelState());
		assertNotNull(item.getBranch());
		assertTrue(item.level() >= 0);
		assertTrue(item.xp() >= 0);
	}

	@Test
	@DisplayName("Should handle LevelData correctly with branches")
	void testLevelData() {
		List<TestLevel> levels = Arrays.asList(TestLevel.LEVEL_1, TestLevel.LEVEL_2, TestLevel.LEVEL_3);
		LevelItem.LevelData<TestLevel> levelData = new LevelItem.LevelData<>(levels);

		assertNotNull(levelData.getCurrentLevel());
		assertEquals(TestLevel.LEVEL_1, levelData.getCurrentLevel());

		LevelItem.LevelData<TestLevel> levelDataWithLevel = new LevelItem.LevelData<>(levels, new LevelValue(1, 5.0));
		assertEquals(TestLevel.LEVEL_2, levelDataWithLevel.getCurrentLevel());

		TestLevel defaultLevel = TestLevel.LEVEL_1;
		assertEquals(TestLevel.LEVEL_2, levelDataWithLevel.getCurrentLevelOr(defaultLevel));
	}

	@Test
	@DisplayName("Should handle XP threshold calculations")
	void testXpThreshold() {
		TestLevelBranchItem item = testEvents.createItem(owner);

		double threshold = item.levelState().xpThreshold(0);
		assertEquals(10.0, threshold, 0.001);

		threshold = item.levelState().xpThreshold(1);
		assertEquals(25.0, threshold, 0.001);
	}

	@Test
	@DisplayName("Should handle null item stack in branch and level retrieval")
	void testNullItemStack() {
		LevelValue level = LevelItemInterface.levelFromItemStack(null, mockGame);
		assertNull(level);

		String branch = BranchItemInterface.branchFromItemStack(null, mockGame);
		assertNull(branch);

		ItemStack stackWithoutMeta = mock(ItemStack.class);
		when(stackWithoutMeta.getItemMeta()).thenReturn(null);

		level = LevelItemInterface.levelFromItemStack(stackWithoutMeta, mockGame);
		assertNull(level);

		branch = BranchItemInterface.branchFromItemStack(stackWithoutMeta, mockGame);
		assertNull(branch);
	}
}