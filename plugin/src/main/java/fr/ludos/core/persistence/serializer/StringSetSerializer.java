package fr.ludos.core.persistence.serializer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;

/**
 * .
 * @param <T>
 */
public class StringSetSerializer<T> implements Serializer<Set<T>, List<String>> {
	private final Serializer<T, String> serializer;
	private final String separator;

	public StringSetSerializer(Serializer<T, String> serializer, String separator) {
		this.serializer = Objects.requireNonNull(serializer);
		this.separator = Objects.requireNonNull(separator);
	}
	public StringSetSerializer(Serializer<T, String> serializer) {
		this(serializer, ", ");
	}

	public final Serializer<T, String> getSerializer() {
		return this.serializer;
	}

	@Override
	public Set<T> parse(List<String> primitive) {
		return primitive.stream()
			.map(serializer::parse)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}

	@Override
	public List<String> serialize(Set<T> value) {
		return value.stream()
			.map(serializer::serialize)
			.filter(Objects::nonNull)
			.toList();
	}

	@Override
	public List<String> getPrimitive(String key, ConfigurationSection config) {
		if (! config.isList(key)) return null;
		return config.getStringList(key);
	}

	@Override
	public Set<T> get(String key, ConfigurationSection config) {
		if (! config.isList(key)) return null;
		List<String> underlying = config.getStringList(key);

		if (underlying.isEmpty()) return null;

		return underlying.stream()
			.map(serializer::parse)
			.collect(Collectors.toSet());
	}

	@Override
	public Set<T> fromString(String value) {
		if (value == null) return null;
		return Arrays.stream(value.split(Pattern.quote(separator)))
			.map(serializer::fromString)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	}
	@Override
	public String toString(Set<T> value) {
		if (value == null) return null;
		return value.stream()
			.map(serializer::toString)
			.filter(Objects::nonNull)
			.collect(Collectors.joining(separator));
	}

}
