package fr.ludos.core.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.item.level.LevelValue;


public class LevelValueMapPersistentDataType extends PersistentMapDataType<String, LevelValue> {

	public static final LevelValueMapPersistentDataType INSTANCE = new LevelValueMapPersistentDataType();

	@Override
	public byte @NotNull [] toPrimitive(@NotNull Map<String, LevelValue> complex, @NotNull PersistentDataAdapterContext context) {
		try {
			// Bukkit's ConfigurationSerialization handles Maps of ConfigurationSerializables automatically
			// if the inner class (State) is registered.
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(complex); // Requires State to be Serializable or handled by custom resolver
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to serialize State map", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public @NotNull Map<String, LevelValue> fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(primitive);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Map<String, LevelValue>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to deserialize State map", e);
		}
	}
}
