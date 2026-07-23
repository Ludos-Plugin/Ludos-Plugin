package fr.ludos.core.persistence.serializer;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public final class PlayerSerializer implements Serializer<OfflinePlayer, String> {
	public static final PlayerSerializer INSTANCE = new PlayerSerializer();

	private PlayerSerializer() {}

	@Override
	public OfflinePlayer parse(String primitive) {
		UUID uuid = UUIDSerializer.INSTANCE.parse(primitive);
		if (uuid == null) return null;
		return Bukkit.getOfflinePlayer(uuid);
	}
	@Override
	public String serialize(OfflinePlayer primitive) {
		return UUIDSerializer.INSTANCE.serialize(primitive.getUniqueId());
	}

	@Override
	public String getPrimitive(String key, ConfigurationSection config) {
		return UUIDSerializer.INSTANCE.getPrimitive(key, config);
	}

	@Override
	public OfflinePlayer fromString(String value) {
		if (value == null) return null;
		return Bukkit.getOfflinePlayer(value);
	}
	@Override
	public String toString(OfflinePlayer value) {
		if (value == null) return null;
		return value.getName();
	}
}
