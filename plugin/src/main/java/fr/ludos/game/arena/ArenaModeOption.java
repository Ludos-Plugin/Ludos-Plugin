package fr.ludos.game.arena;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ArenaModeOption {
	duel,
	multi,
	waves;

	public static List<String> getOptions() {
		return Arrays.stream(ArenaModeOption.values())
			.map(ArenaModeOption::name)
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
