package fr.ludos.core.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.item.level.LevelValue;

/**
 * Persistent data type for {@link LevelValue}.
 */
public class LevelValuePersistentDataType implements PersistentDataType<String, LevelValue> {

	public static final LevelValuePersistentDataType INSTANCE = new LevelValuePersistentDataType();

	@Override
	public Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public Class<LevelValue> getComplexType() {
		return LevelValue.class;
	}

	@Override
	public String toPrimitive(LevelValue complex, PersistentDataAdapterContext context) {
		String res = complex.level() + "|" + complex.xp();
		return res;
	}

	@Override
	public LevelValue fromPrimitive(String primitive, PersistentDataAdapterContext context) {
		String[] split = primitive.split("\\|");
		LevelValue res = new LevelValue(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
		return res;
	}
}
