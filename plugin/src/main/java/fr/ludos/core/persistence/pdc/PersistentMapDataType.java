package fr.ludos.core.persistence.pdc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * PersistentDataType implementation for serializing a Map to a byte array.
 *
 * @param <TKey> the type of map keys
 * @param <TVal> the type of map values
 */
public abstract class PersistentMapDataType<TKey extends Serializable, TVal extends Serializable> implements PersistentDataType<byte[], Map<TKey, TVal>> {

	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Map<TKey, TVal>> getComplexType() {
		return (Class<Map<TKey, TVal>>) (Class<?>) Map.class;
	}
	@Override
	public byte @NotNull [] toPrimitive(@NotNull Map<TKey, TVal> complex, @NotNull PersistentDataAdapterContext context) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(complex); // Requires State to be Serializable or handled by custom resolver
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize State map", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public @NotNull Map<TKey, TVal> fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(primitive);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Map<TKey, TVal>) ois.readObject();
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize State map", e);
		}
	}
}