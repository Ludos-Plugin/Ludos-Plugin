package fr.ludos.game.teamController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.ludos.game.areaController.worldborder.WorldBorderLocationOption;

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
