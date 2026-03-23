package fr.ludos.game.sheepwars;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SheepwarsLocationOptions {
	here ("here"),
	random ("random");

	private String name;
	public String toString() {
		return name;
	}

	private SheepwarsLocationOptions(String name) {
		this.name = name;
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(SheepwarsLocationOptions.values()).map(SheepwarsLocationOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}