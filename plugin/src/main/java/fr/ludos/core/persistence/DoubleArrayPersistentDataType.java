package fr.ludos.core.persistence;

import java.util.Arrays;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;


/**
 * Persistent data type for converting between double arrays and long arrays.
 */
public class DoubleArrayPersistentDataType implements PersistentDataType<long[], double[]> {

	public static final DoubleArrayPersistentDataType INSTANCE = new DoubleArrayPersistentDataType();

	@Override
	public Class<long[]> getPrimitiveType() {
		return long[].class;
	}

	@Override
	public Class<double[]> getComplexType() {
		return double[].class;
	}

	@Override
	public long[] toPrimitive(double[] complex, PersistentDataAdapterContext context) {
		return Arrays.stream(complex).mapToLong(Double::doubleToLongBits).toArray();
	}

	@Override
	public double[] fromPrimitive(long[] primitive, PersistentDataAdapterContext context) {
		return Arrays.stream(primitive).mapToDouble(Double::longBitsToDouble).toArray();
	}
}
