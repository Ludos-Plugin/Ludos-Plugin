package fr.ludos.game.manhunt;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.boss.BarStyle;

public enum ManhuntRevealOptions {
	one_minute ("1min", 60, BarStyle.SEGMENTED_6),
	two_minutes ("2min", 120, BarStyle.SEGMENTED_12),
	three_minutes ("3min", 180, BarStyle.SEGMENTED_6),
	four_minutes ("4min", 240, BarStyle.SEGMENTED_12),
	five_minutes ("5min", 300, BarStyle.SEGMENTED_6),
	six_minutes ("6min", 360, BarStyle.SEGMENTED_12);

	private String displayName;
	public String displayName() {
		return displayName;
	}

	private int duration;
	public int getDuration() {
		return duration;
	}

	private BarStyle barStyle;
	public BarStyle getBarStyle() {
		return barStyle;
	}


	private ManhuntRevealOptions(String name, int duration, BarStyle barStyle) {
		this.displayName = name;
		this.duration = duration;
		this.barStyle = barStyle;
	}


	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(ManhuntRevealOptions.values()).map(ManhuntRevealOptions::name)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}