package fr.ludos.core.persistence.serializer;

import java.time.Duration;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 */
public class TimeSerializer implements Serializer<Duration, String> {
	public static final TimeSerializer INSTANCE = new TimeSerializer();

	private TimeSerializer() {}

	@Override
	public Duration parse(String primitive) {
		return Duration.parse(primitive);
	}

	@Override
	public String serialize(Duration value) {
		return value.toString();
	}

	@Override
	public String getPrimitive(String key, ConfigurationSection config) {
		return config.getString(key);
	}

	@Override
	public Duration fromString(String string) {
		String compliant = string.charAt(0) == '-'
			? "-PT" + string.substring(1)
			: "PT" + string;
			return Duration.parse(compliant);
	}

	@Override
	public String toString(Duration value) {
		String compliant = value.toString();
		return compliant.charAt(0) == '-'
			? '-' + compliant.substring(3)
			: compliant.substring(2);
	}

}
