package fr.ludos.core.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ludos.core.item.level.LevelValue;
import fr.ludos.core.persistence.pdc.LevelValueMapPersistentDataType;

class LevelValueMapPersistentDataTypeTest {
	private final PersistentDataAdapterContext context = mock(PersistentDataAdapterContext.class);

	@Test
	@DisplayName("Should serialize map of LevelValues to byte array correctly")
	void testSerialize() {
		Map<String, LevelValue> map = new HashMap<>();
		map.put("key1", new LevelValue(1, 10.0));
		map.put("key2", new LevelValue(2, 20.5));
		map.put("key3", new LevelValue(3, 0.0));

		byte[] primitive = LevelValueMapPersistentDataType.INSTANCE.toPrimitive(map, context);

		assertNotNull(primitive);
		assertTrue(primitive.length > 0);
	}

	@Test
	@DisplayName("Should deserialize byte array to map of LevelValues correctly")
	void testDeserialize() {
		Map<String, LevelValue> originalMap = new HashMap<>();
		originalMap.put("branch_a", new LevelValue(5, 15.5));
		originalMap.put("branch_b", new LevelValue(10, 0.0));
		originalMap.put("branch_c", new LevelValue(0, 5.0));

		byte[] primitive = LevelValueMapPersistentDataType.INSTANCE.toPrimitive(originalMap, context);
		Map<String, LevelValue> deserializedMap = LevelValueMapPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserializedMap);
		assertEquals(originalMap.size(), deserializedMap.size());

		for (Map.Entry<String, LevelValue> entry : originalMap.entrySet()) {
			LevelValue original = entry.getValue();
			LevelValue deserialized = deserializedMap.get(entry.getKey());

			assertNotNull(deserialized);
			assertEquals(original.level(), deserialized.level());
			assertEquals(original.xp(), deserialized.xp(), 0.001);
		}
	}

	@Test
	@DisplayName("Should handle empty map serialization and deserialization")
	void testEmptyMap() {
		Map<String, LevelValue> emptyMap = new HashMap<>();
		byte[] primitive = LevelValueMapPersistentDataType.INSTANCE.toPrimitive(emptyMap, context);
		Map<String, LevelValue> deserialized = LevelValueMapPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserialized);
		assertTrue(deserialized.isEmpty());
	}

	@Test
	@DisplayName("Should handle map with large number of entries")
	void testLargeMap() {
		Map<String, LevelValue> largeMap = new HashMap<>();
		for (int i = 0; i < 1000; i++) {
			largeMap.put("key_" + i, new LevelValue(i, i * 1.5));
		}

		byte[] primitive = LevelValueMapPersistentDataType.INSTANCE.toPrimitive(largeMap, context);
		Map<String, LevelValue> deserialized = LevelValueMapPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(largeMap.size(), deserialized.size());
		for (Map.Entry<String, LevelValue> entry : largeMap.entrySet()) {
			LevelValue original = entry.getValue();
			LevelValue deserializedValue = deserialized.get(entry.getKey());
			assertNotNull(deserializedValue);
			assertEquals(original.level(), deserializedValue.level());
			assertEquals(original.xp(), deserializedValue.xp(), 0.001);
		}
	}

	@Test
	@DisplayName("Should handle special characters in map keys")
	void testSpecialKeys() {
		Map<String, LevelValue> map = new HashMap<>();
		map.put("key-with-dash", new LevelValue(1, 1.0));
		map.put("key_with_underscore", new LevelValue(2, 2.0));
		map.put("key with space", new LevelValue(3, 3.0));
		map.put("key.with.dots", new LevelValue(4, 4.0));

		byte[] primitive = LevelValueMapPersistentDataType.INSTANCE.toPrimitive(map, context);
		Map<String, LevelValue> deserialized = LevelValueMapPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(map.size(), deserialized.size());
		for (Map.Entry<String, LevelValue> entry : map.entrySet()) {
			assertTrue(deserialized.containsKey(entry.getKey()));
			assertEquals(entry.getValue().level(), deserialized.get(entry.getKey()).level());
			assertEquals(entry.getValue().xp(), deserialized.get(entry.getKey()).xp(), 0.001);
		}
	}

	@Test
	@DisplayName("Should throw runtime exception on IO exception")
	void testParseThrow() {
		Map<String, LevelValue> map = mock(Map.class);
		when(map.get(anyString())).thenThrow(ClassCastException.class);

		assertThrows(RuntimeException.class, () -> {
			LevelValueMapPersistentDataType.INSTANCE.toPrimitive(map, context);
		});

		assertThrows(RuntimeException.class, () -> {
			LevelValueMapPersistentDataType.INSTANCE.fromPrimitive(null, context);
		});
	}
}