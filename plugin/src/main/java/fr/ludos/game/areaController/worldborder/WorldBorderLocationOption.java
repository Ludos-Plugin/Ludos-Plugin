package fr.ludos.game.areaController.worldborder;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Location;

public enum WorldBorderLocationOption {
	here () {
		@Override
		public final Location getLocation(Location location) {
			return location.clone();
		}
	};

	public abstract Location getLocation(Location location);

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