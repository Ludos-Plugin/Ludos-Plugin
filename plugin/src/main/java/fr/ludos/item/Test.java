package fr.ludos.item;

import java.util.Arrays;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class Test implements PersistentDataType<long[], double[]> {

	public static Test DOUBLE_ARRAY = new Test();

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
