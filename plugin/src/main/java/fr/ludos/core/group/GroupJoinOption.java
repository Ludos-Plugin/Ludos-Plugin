package fr.ludos.core.group;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Behaviour for a Player attempting to join a {@link Group}.
 */
public enum GroupJoinOption {
	auto_accept {},
	manual_accept {};

	public static List<String> getOptions() {
		return Arrays.stream(GroupJoinOption.values())
			.map(GroupJoinOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(GroupJoinOption.values()).map(GroupJoinOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}
