package fr.ludos.game.manhunt;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ManhuntLocationOptions {
	here ("here"),
	random ("random");

	private String name;
	public String toString() {
		return name;
	}


	private ManhuntLocationOptions(String name) {
		this.name = name;
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(ManhuntLocationOptions.values()).map(ManhuntLocationOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}