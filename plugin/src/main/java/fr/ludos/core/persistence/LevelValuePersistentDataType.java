package fr.ludos.core.persistence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import fr.ludos.core.item.level.LevelValue;


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
		return complex.level() + "|" + complex.xp();
	}

	@Override
	public LevelValue fromPrimitive(String primitive, PersistentDataAdapterContext context) {
		String[] split = primitive.split("|");
		return new LevelValue(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
	}
}
