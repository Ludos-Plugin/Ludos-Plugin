package fr.ludos.game.alien;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.boss.BarStyle;

public enum AlienRevealOptions {
	one_minute("1min", 60, BarStyle.SEGMENTED_6),
	two_minutes("2min", 120, BarStyle.SEGMENTED_12),
	three_minutes("3min", 180, BarStyle.SEGMENTED_6),
	four_minutes("4min", 240, BarStyle.SEGMENTED_12),
	five_minutes("5min", 300, BarStyle.SEGMENTED_6),
	six_minutes("6min", 360, BarStyle.SEGMENTED_12);

	private final String name;
	private final int duration;
	private final BarStyle barStyle;

	@Override
	public String toString() {
		return name;
	}

	public int getDuration() {
		return duration;
	}

	public BarStyle getBarStyle() {
		return barStyle;
	}

	private AlienRevealOptions(String name, int duration, BarStyle barStyle) {
		this.name = name;
		this.duration = duration;
		this.barStyle = barStyle;
	}

	public static String getUsage() {
		return "<" + Arrays.stream(AlienRevealOptions.values())
				.map(AlienRevealOptions::toString)
				.collect(Collectors.joining(" | ")) + ">";
	}
}