package fr.ludos.core.persistence;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoubleArrayPersistentDataTypeTest {
	private final PersistentDataAdapterContext context = mock(PersistentDataAdapterContext.class);

	@Test
	@DisplayName("Should serialize double array to long array correctly")
	void testSerialize() {
		double[] doubles = {1.0, 2.5, -3.7, 0.0, Double.MAX_VALUE, Double.MIN_VALUE};
		long[] primitive = DoubleArrayPersistentDataType.INSTANCE.toPrimitive(doubles, context);

		assertNotNull(primitive);
		assertEquals(doubles.length, primitive.length);

		// Verify round-trip manually for a few values
		for (int i = 0; i < doubles.length; i++) {
			assertEquals(Double.doubleToLongBits(doubles[i]), primitive[i]);
		}
	}

	@Test
	@DisplayName("Should deserialize long array to double array correctly")
	void testDeserialize() {
		long[] longs = {
			Double.doubleToLongBits(1.0),
			Double.doubleToLongBits(2.5),
			Double.doubleToLongBits(-3.7),
			Double.doubleToLongBits(0.0),
			Double.doubleToLongBits(Double.MAX_VALUE),
			Double.doubleToLongBits(Double.MIN_VALUE)
		};

		double[] deserialized = DoubleArrayPersistentDataType.INSTANCE.fromPrimitive(longs, context);

		assertNotNull(deserialized);
		assertEquals(longs.length, deserialized.length);

		assertArrayEquals(
			Arrays.stream(longs).mapToDouble(Double::longBitsToDouble).toArray(),
			deserialized,
			0.0
		);
	}

	@Test
	@DisplayName("Should handle empty double array")
	void testEmptyArray() {
		double[] empty = {};
		long[] primitive = DoubleArrayPersistentDataType.INSTANCE.toPrimitive(empty, context);
		double[] deserialized = DoubleArrayPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserialized);
		assertEquals(0, deserialized.length);
	}

	@Test
	@DisplayName("Should handle special double values (NaN, Infinity)")
	void testSpecialValues() {
		double[] special = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
		long[] primitive = DoubleArrayPersistentDataType.INSTANCE.toPrimitive(special, context);
		double[] deserialized = DoubleArrayPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertNotNull(deserialized);
		assertEquals(special.length, deserialized.length);

		assertTrue(Double.isNaN(deserialized[0]));
		assertTrue(Double.isInfinite(deserialized[1]));
		assertTrue(deserialized[1] > 0); // Positive Infinity
		assertTrue(Double.isInfinite(deserialized[2]));
		assertTrue(deserialized[2] < 0); // Negative Infinity
	}

	@Test
	@DisplayName("Should handle large double arrays")
	void testLargeArray() {
		int size = 10000;
		double[] large = new double[size];
		for (int i = 0; i < size; i++) {
			large[i] = i * 0.123456789;
		}

		long[] primitive = DoubleArrayPersistentDataType.INSTANCE.toPrimitive(large, context);
		double[] deserialized = DoubleArrayPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(large.length, deserialized.length);
		assertArrayEquals(large, deserialized, 0.0); // Exact match for double precision
	}

	@Test
	@DisplayName("Should handle array with single element")
	void testSingleElement() {
		double[] single = {42.42};
		long[] primitive = DoubleArrayPersistentDataType.INSTANCE.toPrimitive(single, context);
		double[] deserialized = DoubleArrayPersistentDataType.INSTANCE.fromPrimitive(primitive, context);

		assertEquals(1, deserialized.length);
		assertEquals(42.42, deserialized[0], 0.0);
	}

	@Test
	@DisplayName("Should have correct primitive and complex types")
	void testTypeInformation() {
		assertEquals(long[].class, DoubleArrayPersistentDataType.INSTANCE.getPrimitiveType());
		assertEquals(double[].class, DoubleArrayPersistentDataType.INSTANCE.getComplexType());
	}
}