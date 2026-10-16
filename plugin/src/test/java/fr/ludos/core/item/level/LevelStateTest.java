package fr.ludos.core.item.level;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LevelStateTest {


	@Test
	@DisplayName("Should initialize with default values")
	void testInitializationDefault() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = new LevelState(value);


		assertEquals(1, state.level());
		assertEquals(5.0, state.xp(), 0.01);
		assertEquals(Integer.MAX_VALUE, state.maxLevel());
	}


	@Test
	@DisplayName("Should initialize with XP threshold function")
	void testInitializationWithThresholdFunction() {
		LevelValue value = new LevelValue(1, 5.0);
		Function<Integer, Double> threshold = level -> level * 10.0;
		LevelState state = LevelState.xpCapped(value, threshold);


		assertEquals(1, state.level());
		assertEquals(5.0, state.xp(), 0.01);
		assertEquals(10.0, state.xpThreshold(), 0.01);
	}


	@Test
	@DisplayName("Should initialize with XP threshold value")
	void testInitializationWithThresholdValue() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.xpCapped(value, 10.0);


		assertEquals(1, state.level());
		assertEquals(5.0, state.xp(), 0.01);
		assertEquals(10.0, state.xpThreshold(), 0.01);
	}


	@Test
	@DisplayName("Should initialize with max level")
	void testInitializationWithMaxLevel() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.levelCapped(value, 2);


		assertEquals(1, state.level());
		assertEquals(5.0, state.xp(), 0.01);
		assertEquals(2, state.maxLevel());
	}

	@Test
	@DisplayName("Should set value and notify listeners")
	void testSetValue() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = new LevelState(value);

		BiConsumer<LevelValue, Integer> mockLevelListener = mock(BiConsumer.class);
		state.addLevelUpListener(mockLevelListener);
		BiConsumer<LevelValue, Double> mockXpListener = mock(BiConsumer.class);
		state.addXpChangeListener(mockXpListener);


		state.setValue(new LevelValue(2, 10.0));


		assertEquals(2, state.level());
		assertEquals(10.0, state.xp(), 0.01);
		verify(mockLevelListener).accept(eq(new LevelValue(2, 10.0)), eq(1));
		verify(mockXpListener).accept(eq(new LevelValue(2, 10.0)), eq(5.0));
	}

	@Test
	@DisplayName("Should set level and notify listeners")
	void testSetLevel() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = new LevelState(value);
		BiConsumer<LevelValue, Integer> mockListener = mock(BiConsumer.class);
		state.addLevelUpListener(mockListener);


		state.setLevel(2);


		assertEquals(2, state.level());
		verify(mockListener).accept(eq(new LevelValue(2, 5.0)), eq(1));
	}


	@Test
	@DisplayName("Should add level and notify listeners")
	void testAddLvl() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = new LevelState(value);
		BiConsumer<LevelValue, Integer> mockListener = mock(BiConsumer.class);
		state.addLevelUpListener(mockListener);


		state.addLvl();

		assertEquals(2, state.level());
		verify(mockListener).accept(eq(new LevelValue(2, 5.0)), eq(1));
	}


	@Test
	@DisplayName("Should set XP and notify listeners")
	void testSetXp() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.xpCapped(value, 10.0);
		BiConsumer<LevelValue, Double> mockListener = mock(BiConsumer.class);
		state.addXpChangeListener(mockListener);


		state.setXp(8.0);


		assertEquals(8.0, state.xp(), 0.01);
		verify(mockListener).accept(eq(new LevelValue(1, 8.0)), eq(5.0));
	}


	@Test
	@DisplayName("Should add XP and level up if threshold reached")
	void testAddXpLevelUp() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.xpCapped(value, 10.0);
		BiConsumer<LevelValue, Integer> mockLevelListener = mock(BiConsumer.class);
		BiConsumer<LevelValue, Double> mockXpListener = mock(BiConsumer.class);
		state.addLevelUpListener(mockLevelListener);
		state.addXpChangeListener(mockXpListener);


		state.addXp(6.0); // 5 + 6 = 11 > 10, should level up


		assertEquals(2, state.level());
		assertEquals(1.0, state.xp(), 0.01);
		verify(mockLevelListener).accept(eq(new LevelValue(2, 1.0)), eq(1));
		verify(mockXpListener).accept(eq(new LevelValue(2, 1.0)), eq(5.0));
	}


	@Test
	@DisplayName("Should cap level at max level")
	void testCapLevel() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.levelCapped(value, 2);
		state.setLevel(3);


		assertEquals(2, state.level());
	}


	@Test
	@DisplayName("Should cap XP at max level")
	void testCapXp() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.capped(value, 10.0, 2);
		state.setXp(15.0);


		assertEquals(0.0, state.xp(), 0.01);
		assertEquals(2, state.level());
	}
	@Test
	@DisplayName("Should cap level at max level")
	void testCapLevelAdd() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelState state = LevelState.levelCapped(value, 2);
		state.addLvl();


		assertEquals(2, state.level());
	}
}