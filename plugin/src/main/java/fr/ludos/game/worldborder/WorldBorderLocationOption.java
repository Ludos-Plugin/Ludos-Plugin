package fr.ludos.game.worldborder;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum WorldBorderLocationOption {
	here (),
	random ();


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(WorldBorderLocationOption.values()).map(WorldBorderLocationOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}