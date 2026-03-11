package fr.ludos.game.alien;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Location;

public enum AlienLocationOptions {
	here("here"),
	random("random");

	private final String name;

	@Override
	public String toString() {
		return name;
	}

	private AlienLocationOptions(String name) {
		this.name = name;
	}

	public static String getUsage() {
		return "<" + Arrays.stream(AlienLocationOptions.values())
				.map(AlienLocationOptions::toString)
				.collect(Collectors.joining(" | ")) + ">";
	}


	static AlienLocationOptions fromLocation(Location spawnLocation) {
		if (spawnLocation == null) {
			return AlienLocationOptions.random;
		} else {
			return AlienLocationOptions.here;
		}
	}
}