package fr.ludos.game.arena;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ArenaModeOption {
	duel("duel", "Duel", "1v1 rounds"),
	multi("multi", "Multi", "team rounds"),
	waves("waves", "Waves", "co-op survival waves");

	private final String id;
	private final String displayName;
	private final String description;

	ArenaModeOption(String id, String displayName, String description) {
		this.id = id;
		this.displayName = displayName;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public boolean isWaves() {
		return this == waves;
	}

	public boolean isDuel() {
		return this == duel;
	}

	public static Optional<ArenaModeOption> resolve(String value) {
		if (value == null || value.isBlank()) return Optional.empty();

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return Arrays.stream(ArenaModeOption.values())
			.filter(option -> option.id.equalsIgnoreCase(normalized) || option.name().equalsIgnoreCase(normalized))
			.findFirst();
	}

	public static ArenaModeOption fromConfig(String value, ArenaModeOption fallback) {
		return resolve(value).orElse(fallback);
	}

	public static List<String> getOptions() {
		return Arrays.stream(ArenaModeOption.values())
			.map(ArenaModeOption::getId)
			.collect(Collectors.toList());
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
}
