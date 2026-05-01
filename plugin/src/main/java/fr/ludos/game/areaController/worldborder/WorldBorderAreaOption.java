package fr.ludos.game.areaController.worldborder;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum WorldBorderAreaOption {
	large (350),
	medium (250),
	small (150);

	private int size;
	public int getSize() {
		return size;
	}


	private WorldBorderAreaOption(int size) {
		this.size = size;
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(WorldBorderAreaOption.values()).map(WorldBorderAreaOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}