package fr.ludos.core.game.teamController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;

/**
 * Behaviour in the event that a Player joins a {@link Group} when a {@link Game} is in progress.
 */
public enum GameJoinOption {
	auto {},
	manual {},
	none {};

	public static List<String> getOptions() {
		return Arrays.stream(GameJoinOption.values())
			.map(GameJoinOption::toString)
			.collect(Collectors.toList());
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(GameJoinOption.values()).map(GameJoinOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}
