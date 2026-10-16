package fr.ludos.core.item.level;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LevelValueTest {


	@Test
	@DisplayName("Should initialize with default values")
	void testInitializationDefault() {
		LevelValue value = new LevelValue();


		assertEquals(0, value.level());
		assertEquals(0.0, value.xp(), 0.01);
	}


	@Test
	@DisplayName("Should initialize with level only")
	void testInitializationWithLevel() {
		LevelValue value = new LevelValue(5);


		assertEquals(5, value.level());
		assertEquals(0.0, value.xp(), 0.01);
	}


	@Test
	@DisplayName("Should initialize with level and XP")
	void testInitializationWithLevelAndXp() {
		LevelValue value = new LevelValue(5, 10.0);


		assertEquals(5, value.level());
		assertEquals(10.0, value.xp(), 0.01);
	}


	@Test
	@DisplayName("Should create copy with new level")
	void testWithLevel() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.withLevel(3);


		assertEquals(3, copy.level());
		assertEquals(10.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should create copy with level capped at max")
	void testWithLevelCapped() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.withLevel(3, 4);


		assertEquals(3, copy.level());
		assertEquals(10.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should add level")
	void testWithAddedLevel() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.withAddedLevel(2);


		assertEquals(7, copy.level());
		assertEquals(10.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should add level with cap")
	void testWithAddedLevelCapped() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.withAddedLevel(2, 6);


		assertEquals(6, copy.level());
		assertEquals(10.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should add XP")
	void testWithAddedXp() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.withAddedXp(5.0);


		assertEquals(5, copy.level());
		assertEquals(15.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should add XP and level up")
	void testWithAddedXpLevelUp() {
		LevelValue value = new LevelValue(5, 10.0);
		Function<Integer, Double> threshold = level -> level * 10.0;
		LevelValue copy = value.withAddedXp(45.0, threshold, 10);


		assertEquals(6, copy.level());
		assertEquals(5.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should cap at XP threshold")
	void testCappedWithThreshold() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.xpCapped(10.0);


		assertEquals(5, copy.level());
		assertEquals(0.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should cap at max level")
	void testCappedWithMaxLevel() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.capped(100.0, 3);


		assertEquals(3, copy.level());
		assertEquals(0.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should cap level and xp if exceeds max level")
	void testCappedLevelExceeds() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.capped(5, 3);


		assertEquals(3, copy.level());
		assertEquals(0.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should cap XP at threshold if exceeds")
	void testCappedXpExceeds() {
		LevelValue value = new LevelValue(5, 10.0);
		LevelValue copy = value.xpCapped(5.0);


		assertEquals(5, copy.level());
		assertEquals(0.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should return correct string representation")
	void testToString() {
		LevelValue value = new LevelValue(2, 15.5);
		String str = value.toString();


		assertTrue(str.contains("level=2"));
		assertTrue(str.contains("xp=15.5"));
	}


	@Test
	@DisplayName("Should copy constructor work correctly")
	void testCopyConstructor() {
		LevelValue original = new LevelValue(3, 7.5);
		LevelValue copy = new LevelValue(original);


		assertEquals(original.level(), copy.level());
		assertEquals(original.xp(), copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should handle negative XP cap")
	void testCappedNegativeXpThreshold() {
		LevelValue value = new LevelValue(1, 5.0);
		LevelValue copy = value.xpCapped(-5.0);


		assertEquals(1, copy.level());
		assertEquals(0.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should handle custom threshold function correctly")
	void testWithAddedXpCustomThreshold() {
		LevelValue value = new LevelValue(1, 5.0);
		Function<Integer, Double> threshold = level -> level * 15.0;
		LevelValue copy = value.withAddedXp(20.0, threshold, 5);


		assertEquals(2, copy.level());
		assertEquals(10.0, copy.xp(), 0.01);
	}


	@Test
	@DisplayName("Should return true if level is within valid range")
	void testIsValidLevel() {
		LevelValue value = new LevelValue(1, 5.0);


		assertTrue(value.level() >= 0);
	}


	@Test
	@DisplayName("Should return true if XP is non-negative")
	void testIsValidXp() {
		LevelValue value = new LevelValue(1, 5.0);


		assertTrue(value.xp() >= 0.0);
	}
}