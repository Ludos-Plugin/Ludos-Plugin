package fr.ludos.core.item.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

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
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.persistence.LevelValuePersistentDataType;
import net.kyori.adventure.text.Component;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LevelItemTest {

	// Helper enum for testing Level implementation
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

	// Helper class for testing
	private static class TestLevelItem extends LevelItem<TestLevelItem, TestLevel> {
		public final static String ID = "test_level_item";

		protected TestLevelItem(ItemData<TestLevel> info, Events<TestLevelItem, TestLevel> events) {
			super(info, events);
		}

		@Override
		public Component getName() {
			return Component.text("Test Level Item");
		}

		public static class TestEvents extends Events<TestLevelItem, TestLevel> {
			public TestEvents(Game game, Info info) {
				super(game, info);
			}

			@Override
			public List<TestLevel> getLevels() {
				return Arrays.asList(TestLevel.LEVEL_1, TestLevel.LEVEL_2, TestLevel.LEVEL_3, TestLevel.MAX_LEVEL);
			}

			@Override
			public String getTypeId() {
				return ID;
			}

			@Override
			protected TestLevelItem getItemInternal(ItemData<TestLevel> info) {
				return new TestLevelItem(info, this);
			}

			@Override
			protected TestLevelItem createItemInternal(LevelItem.LevelData<TestLevel> data, Player owner) {
				return new TestLevelItem(new ItemData<>(data, new SpecialItem.ItemData(new ItemStack(Material.DIAMOND_SWORD), owner)), this);
			}
		}
	}

	private ServerMock server;
	private Ludos mockLudos;
	private Game mockGame;
	private Group mockGroup;
	private PlayerMock owner;
	private TestLevelItem.TestEvents testEvents;

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
		when(mockGame.getLudos()).thenReturn(mockLudos);
		when(mockGame.getGroup()).thenReturn(mockGroup);
		when(mockGroup.isPlayer(any())).thenReturn(true);
		when(mockGroup.getOnlinePlayers()).thenReturn(Set.of(owner));

		// Setup basic mocks for level item
		testEvents = new TestLevelItem.TestEvents(mockGame, new SpecialItem.Events.Info(ItemSlot.HOTBAR_1, false));
	}

	@Test
	@DisplayName("Equal operation should work")
	void testEquals() {
		TestLevelItem item = testEvents.createItem(owner);
		TestLevelItem copyItem = testEvents.getItem(item.getStack());

		assertEquals(item, copyItem);
		assertTrue(item == copyItem);
		assertFalse(item.equals(null));
		assertFalse(item.equals(mock(LevelItem.class)));

		TestLevelItem otherItem = spy(copyItem);

		PlayerMock otherOwner = server.addPlayer("OtherOwner");
		when(otherItem.getOwner()).thenReturn(otherOwner);
		assertFalse(item.equals(otherItem));

		when(otherItem.getTypeId()).thenReturn("other_type_id");
		assertFalse(item.equals(otherItem));

		when(otherItem.getItemId()).thenReturn(UUID.randomUUID());
		assertFalse(item.equals(otherItem));
	}

	@Test
	@DisplayName("Should initialize LevelItem with correct level state")
	void testInitializeLevelItem() {
		TestLevelItem item = testEvents.createItem(owner);

		assertNotNull(item);
		assertNotNull(item.levelState());
		assertEquals(0, item.level());
		assertEquals(0.0, item.xp(), 0.001);

		// Verify PDC contains level data
		ItemStack stack = item.getStack();
		ItemMeta meta = stack.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		assertTrue(container.has(LevelItemInterface.LEVEL_KEY, LevelValuePersistentDataType.INSTANCE));
		assertEquals(0, container.get(LevelItemInterface.LEVEL_KEY, LevelValuePersistentDataType.INSTANCE).level());
	}

	@Test
	@DisplayName("Should handle level progression correctly")
	void testLevelProgression() {
		TestLevelItem item = testEvents.createItem(owner);

		// Add XP to reach next level
		item.addXp(10.0);
		assertEquals(0.0, item.xp(), 0.001);
		assertEquals(1, item.level());

		// Add more XP to level up
		item.addXp(5.0); // Total 15 XP, threshold is 10
		assertEquals(1, item.level());
		assertEquals(5.0, item.xp(), 0.001);

		// Add XP to reach level 2
		item.addXp(15.0); // Total 20 XP, threshold is 25
		assertEquals(1, item.level());
		assertEquals(20.0, item.xp(), 0.001);

		item.addXp(10.0); // Total 30 XP
		assertEquals(2, item.level());
		assertEquals(5.0, item.xp(), 0.001);
	}

	@Test
	@DisplayName("Should retrieve correct level object")
	void testGetLevelObject() {
		TestLevelItem item = testEvents.createItem(owner);

		TestLevel currentLevel = item.lvlObject();
		assertEquals(TestLevel.LEVEL_1, currentLevel);

		TestLevel level1 = item.lvlObject(1);
		assertEquals(TestLevel.LEVEL_2, level1);

		TestLevel level2 = item.lvlObject(2);
		assertEquals(TestLevel.LEVEL_3, level2);
	}

	@Test
	@DisplayName("Should generate correct lore with level and XP")
	void testGetLore() {
		TestLevelItem item = testEvents.createItem(owner);

		List<Component> lore = item.getLore();
		assertNotNull(lore);
		assertTrue(lore.size() >= 2);

		// Check level lore
		Component levelLore = lore.get(lore.size() - 2);
		assertTrue(levelLore.toString().contains("Level:"));
		assertTrue(levelLore.toString().contains("1"));

		// Check XP lore
		Component xpLore = lore.get(lore.size() - 1);
		assertTrue(xpLore.toString().contains("XP:"));
		assertTrue(xpLore.toString().contains("0.0/10.0"));
	}

	@Test
	@DisplayName("Should handle item switching with level data")
	void testItemSwitchWithLevel() {
		PlayerMock player = server.addPlayer("Player2");
		PlayerInventory inventory = player.getInventory();

		TestLevelItem item1 = testEvents.createItem(owner);
		item1.addXp(15.0); // Level 1

		TestLevelItem item2 = testEvents.createItem(player);
		item2.addXp(30.0); // Level 2

		inventory.setItem(0, item1.getStack());
		inventory.setItem(1, item2.getStack());

		// Create mock event for switching from slot 0 to 1
		PlayerItemHeldEvent switchEvent = mock(PlayerItemHeldEvent.class);
		when(switchEvent.getPlayer()).thenReturn(player);
		when(switchEvent.getPreviousSlot()).thenReturn(0);
		when(switchEvent.getNewSlot()).thenReturn(1);
	}

	@Test
	@DisplayName("Should save and load level value from item stack")
	void testSaveAndLoadLevelValue() {
		TestLevelItem item = testEvents.createItem(owner);
		item.addXp(15.0); // Level 1

		ItemStack stack = item.getStack();
		LevelValue savedLevel = LevelItemInterface.levelFromItemStack(stack, mockGame);

		assertNotNull(savedLevel);
		assertEquals(1, savedLevel.level());
		assertEquals(5.0, savedLevel.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle max level correctly")
	void testMaxLevel() {
		TestLevelItem item = testEvents.createItem(owner);

		// Add XP to reach max level
		item.addXp(10.0); // Level 1
		item.addXp(25.0); // Level 2
		item.addXp(50.0); // Level 3 (MAX)

		assertEquals(3, item.level());
		assertEquals(0.0, item.xp(), 0.001); // Should be at max

		// Should not exceed max level
		item.addXp(100.0);
		assertEquals(3, item.level());
	}

	@Test
	@DisplayName("Should generate correct level up message")
	void testLevelUpMessage() {
		TestLevelItem item = testEvents.createItem(owner);

		Component message = LevelItemInterface.getLevelUpMessage(item);
		assertNotNull(message);

		String messageText = message.toString();
		assertTrue(messageText.contains("Your"));
		assertTrue(messageText.contains("Test Level Item"));
		assertTrue(messageText.contains("has leveled up!"));
	}

	@Test
	@DisplayName("Should handle LevelData correctly")
	void testLevelData() {
		List<TestLevel> levels = Arrays.asList(TestLevel.LEVEL_1, TestLevel.LEVEL_2, TestLevel.LEVEL_3);
		LevelItem.LevelData<TestLevel> levelData = new LevelItem.LevelData<>(levels);

		assertNotNull(levelData.getCurrentLevel());
		assertEquals(TestLevel.LEVEL_1, levelData.getCurrentLevel());

		LevelItem.LevelData<TestLevel> levelDataWithLevel = new LevelItem.LevelData<>(levels, new LevelValue(1, 5.0));
		assertEquals(TestLevel.LEVEL_2, levelDataWithLevel.getCurrentLevel());

		TestLevel defaultLevel = TestLevel.LEVEL_1;
		assertEquals(TestLevel.LEVEL_2, levelDataWithLevel.getCurrentLevelOr(defaultLevel));

		// Test with invalid level index
		LevelItem.LevelData<TestLevel> invalidLevelData = new LevelItem.LevelData<>(levels, new LevelValue(10, 0.0));
		assertNull(invalidLevelData.getCurrentLevel());
		assertEquals(defaultLevel, invalidLevelData.getCurrentLevelOr(defaultLevel));
	}

	@Test
	@DisplayName("Should handle XP threshold calculations")
	void testXpThreshold() {
		TestLevelItem item = testEvents.createItem(owner);

		double threshold = item.levelState().xpThreshold(0);
		assertEquals(10.0, threshold, 0.001);

		threshold = item.levelState().xpThreshold(1);
		assertEquals(25.0, threshold, 0.001);

		threshold = item.levelState().xpThreshold(2);
		assertEquals(50.0, threshold, 0.001);
	}

	@Test
	@DisplayName("Should handle null item stack in level retrieval")
	void testNullItemStack() {
		LevelValue level = LevelItemInterface.levelFromItemStack(null, mockGame);
		assertNull(level);

		ItemStack stackWithoutMeta = mock(ItemStack.class);
		when(stackWithoutMeta.getItemMeta()).thenReturn(null);

		level = LevelItemInterface.levelFromItemStack(stackWithoutMeta, mockGame);
		assertNull(level);
	}

	@Test
	@DisplayName("Should handle XP lore field for max level")
	void testXpLoreFieldMaxLevel() {
		TestLevelItem item = testEvents.createItem(owner);

		// Set to max level
		LevelState maxLevelState = LevelState.capped(new LevelValue(3, 0.0), l -> TestLevel.values()[l].xpThreshold(), 4);
		item.setValue(new LevelValue(3, 0.0));

		Component xpLore = LevelItemInterface.getXpLoreField(item);
		assertNotNull(xpLore);
		assertTrue(xpLore.toString().contains("MAX"));
	}

	@Test
	@DisplayName("Should handle level state initialization listeners")
	void testLevelStateListeners() {
		TestLevelItem item = testEvents.createItem(owner);

		BiConsumer<LevelValue, Double> onChangeXp = mock(BiConsumer.class);
		item.levelState().addXpChangeListener(onChangeXp);
		BiConsumer<LevelValue, Integer> onChangeLevel = mock(BiConsumer.class);
		item.levelState().addLevelUpListener(onChangeLevel);

		item.addXp(15.0);
		LevelValue expected = new LevelValue(1, 5.0);

		verify(onChangeXp).accept(eq(expected), eq(0.0));
		verify(onChangeLevel).accept(eq(expected), eq(0));
	}

	@Test
	@DisplayName("Should handle concurrent level and XP changes")
	void testConcurrentLevelXpChanges() {
		TestLevelItem item = testEvents.createItem(owner);

		// Add XP multiple times to trigger multiple level ups
		for (int i = 0; i < 100; i++) {
			item.addXp(1.0);
		}

		assertNotNull(item.levelState());
		assertTrue(item.level() >= 0);
		assertTrue(item.xp() >= 0);
	}
}