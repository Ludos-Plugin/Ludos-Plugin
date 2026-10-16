package fr.ludos.core.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.persistence.pdc.LevelValuePersistentDataType;

class LevelValuePersistentDataTypeTest {
	private final PersistentDataAdapterContext context = mock(PersistentDataAdapterContext.class);

	@Test
	@DisplayName("Should serialize LevelValue to string correctly")
	void testSerialize() {
		LevelValue value = new LevelValue(5, 12.75);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);

		assertEquals("5|12.75", primitive);
	}

	@Test
	@DisplayName("Should deserialize string to LevelValue correctly")
	void testDeserialize() {
		String primitive = "3|45.5";
		LevelValue value = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(value);
		assertEquals(3, value.level());
		assertEquals(45.5, value.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle LevelValue with zero XP")
	void testZeroXp() {
		LevelValue value = new LevelValue(10, 0.0);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);
		LevelValue deserialized = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(10, deserialized.level());
		assertEquals(0.0, deserialized.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle LevelValue with max integer level")
	void testMaxLevel() {
		LevelValue value = new LevelValue(Integer.MAX_VALUE, 999.99);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);
		LevelValue deserialized = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(Integer.MAX_VALUE, deserialized.level());
		assertEquals(999.99, deserialized.xp(), 0.001);
	}

	@Test
	@DisplayName("Should throw exception on invalid format during deserialization")
	void testInvalidFormat() {
		String invalidPrimitive = "invalid|format";

		assertThrows(NumberFormatException.class, () -> {
			LevelValuePersistentDataType.INSTANCE.fromPrimitive(invalidPrimitive, context);
		});
	}

	@Test
	@DisplayName("Should handle negative XP values")
	void testNegativeXp() {
		LevelValue value = new LevelValue(1, -5.5);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);
		LevelValue deserialized = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(1, deserialized.level());
		assertEquals(-5.5, deserialized.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle negative levels")
	void testNegativeLevel() {
		LevelValue value = new LevelValue(-1, 0.0);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);
		LevelValue deserialized = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(-1, deserialized.level());
		assertEquals(0.0, deserialized.xp(), 0.001);
	}

	@Test
	@DisplayName("Should handle floating point precision in XP")
	void testPrecision() {
		LevelValue value = new LevelValue(1, 123.456789);
		String primitive = LevelValuePersistentDataType.INSTANCE.toPrimitive(value, context);
		LevelValue deserialized = LevelValuePersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(1, deserialized.level());
		assertEquals(123.456789, deserialized.xp(), 0.000001);
	}

	@Test
	@DisplayName("Should have correct primitive and complex types")
	void testTypeInformation() {
		assertEquals(String.class, LevelValuePersistentDataType.INSTANCE.getPrimitiveType());
		assertEquals(LevelValue.class, LevelValuePersistentDataType.INSTANCE.getComplexType());
	}
}