package fr.ludos.game.alien;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AlienLocationOptions {
	here ("here"),
	random ("random");

	private String name;
	public String toString() {
		return name;
	}


	private AlienLocationOptions(String name) {
		this.name = name;
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(AlienLocationOptions.values()).map(AlienLocationOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}