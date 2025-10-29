package fr.ludos.game.manhunt;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ManhuntAreaOptions {
	large ("large", 350),
	medium ("medium", 250),
	small ("small", 150);

	private String name;
	public String toString() {
		return name;
	}

	private int size;
	public int getSize() {
		return size;
	}


	private ManhuntAreaOptions(String name, int size) {
		this.name = name;
		this.size = size;
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(ManhuntAreaOptions.values()).map(ManhuntAreaOptions::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}