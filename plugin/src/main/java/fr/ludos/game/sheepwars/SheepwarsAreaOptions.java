package fr.ludos.game.sheepwars;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum SheepwarsAreaOptions {
	large ("large", 500),
	medium ("medium", 350),
	small ("small", 200);

	private String name;
	public String toString() {
		return name;
	}

	private int size;
	public int getSize() {
		return size;
	}

	private SheepwarsAreaOptions(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(SheepwarsAreaOptions.values()).map(SheepwarsAreaOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}