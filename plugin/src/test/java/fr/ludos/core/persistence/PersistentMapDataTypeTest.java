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

import fr.ludos.core.persistence.pdc.PersistentMapDataType;

class PersistentMapDataTypeTest {
	private final PersistentDataAdapterContext context = mock(PersistentDataAdapterContext.class);


	private static class TestMapDataType extends PersistentMapDataType<String, String> {
		public static final TestMapDataType INSTANCE = new TestMapDataType();
	}

	@Test
	@DisplayName("Should serialize map to byte array")
	void testSerializeMap() {
		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		map.put("key2", "value2");
		map.put("key3", "value3");

		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(map, context);

		assertNotNull(primitive);
		assertTrue(primitive.length > 0);
	}

	@Test
	@DisplayName("Should deserialize byte array to map")
	void testDeserializeMap() {
		Map<String, String> originalMap = new HashMap<>();
		originalMap.put("branch_a", "mode_a");
		originalMap.put("branch_b", "mode_b");
		originalMap.put("branch_c", "mode_c");

		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(originalMap, context);
		Map<String, String> deserializedMap = TestMapDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserializedMap);
		assertEquals(originalMap.size(), deserializedMap.size());

		for (Map.Entry<String, String> entry : originalMap.entrySet()) {
			String originalValue = entry.getValue();
			String deserializedValue = deserializedMap.get(entry.getKey());

			assertNotNull(deserializedValue);
			assertEquals(originalValue, deserializedValue);
		}
	}

	@Test
	@DisplayName("Should handle empty map")
	void testEmptyMap() {
		Map<String, String> emptyMap = new HashMap<>();
		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(emptyMap, context);
		Map<String, String> deserialized = TestMapDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserialized);
		assertTrue(deserialized.isEmpty());
	}

	@Test
	@DisplayName("Should handle map with special characters in keys and values")
	void testSpecialCharacters() {
		Map<String, String> map = new HashMap<>();
		map.put("key-with-dash", "value-with-dash");
		map.put("key_with_underscore", "value_with_underscore");
		map.put("key.with.dots", "value.with.dots");
		map.put("key with space", "value with space");
		map.put("clé_avec_espèces", "valeur_avec_espèces"); // Non-ASCII characters

		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(map, context);
		Map<String, String> deserialized = TestMapDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(map.size(), deserialized.size());
		for (Map.Entry<String, String> entry : map.entrySet()) {
			assertEquals(entry.getValue(), deserialized.get(entry.getKey()));
		}
	}

	@Test
	@DisplayName("Should handle large map with many entries")
	void testLargeMap() {
		Map<String, String> largeMap = new HashMap<>();
		for (int i = 0; i < 5000; i++) {
			largeMap.put("key_" + i, "value_" + i);
		}

		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(largeMap, context);
		Map<String, String> deserialized = TestMapDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(largeMap.size(), deserialized.size());
		for (int i = 0; i < largeMap.size(); i++) {
			String originalValue = largeMap.get("key_" + i);
			String deserializedValue = deserialized.get("key_" + i);
			assertNotNull(deserializedValue);
			assertEquals(originalValue, deserializedValue);
		}
	}

	@Test
	@DisplayName("Should handle map with null values if allowed by Map implementation")
	void testMapWithNullValues() {
		Map<String, String> map = new HashMap<>();
		map.put("valid_key", "valid_value");
		map.put("null_key", null);

		byte[] primitive = TestMapDataType.INSTANCE.toPrimitive(map, context);
		Map<String, String> deserialized = TestMapDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserialized);
		assertEquals(map.size(), deserialized.size());
		assertEquals(null, deserialized.get("null_key"));
		assertEquals("valid_value", deserialized.get("valid_key"));
	}

	@Test
	@DisplayName("Should verify round-trip consistency")
	void testRoundTripConsistency() {
		Map<String, String> original = new HashMap<>();
		original.put("a", "1");
		original.put("b", "2");
		original.put("c", "3");

		byte[] bytes = TestMapDataType.INSTANCE.toPrimitive(original, context);
		Map<String, String> roundTrip = TestMapDataType.INSTANCE.fromPrimitive(bytes, context);

		assertEquals(original, roundTrip);
	}

	@Test
	@DisplayName("Should have correct primitive and complex types")
	void testTypeInformation() {
		assertEquals(byte[].class, TestMapDataType.INSTANCE.getPrimitiveType());
		assertEquals(Map.class, TestMapDataType.INSTANCE.getComplexType());
	}

	@Test
	@DisplayName("Should throw runtime exception on IO exception")
	void testParseThrow() {
		Map<String, String> map = mock(Map.class);
		when(map.get(anyString())).thenThrow(ClassCastException.class);

		assertThrows(RuntimeException.class, () -> {
			TestMapDataType.INSTANCE.toPrimitive(map, context);
		});

		assertThrows(RuntimeException.class, () -> {
			TestMapDataType.INSTANCE.fromPrimitive(null, context);
		});
	}
}