package fr.ludos.games.arena;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ArenaModeOption {
	duel("Duel", "1v1 rounds"),
	multi("Multi", "team rounds");

	private final String displayName;
	private final String description;

	ArenaModeOption(String displayName, String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public static Optional<ArenaModeOption> resolve(String value) {
		if (value == null || value.isBlank()) return Optional.empty();

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(ArenaModeOption.values())
			.filter(option -> option.name().equalsIgnoreCase(normalized) || option.name().equalsIgnoreCase(normalized))
			.findFirst();
	}

	public static ArenaModeOption fromConfig(String value, ArenaModeOption fallback) {
		return resolve(value).orElse(fallback);
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(ArenaModeOption.values())
			.map(ArenaModeOption::name)
			.collect(Collectors.joining(" | "))
		);
		sb.append(">");

		return sb.toString();
	}

	public static final List<String> options = Arrays.stream(ArenaModeOption.values())
		.map(Enum::name)
		.collect(Collectors.toList());
}
