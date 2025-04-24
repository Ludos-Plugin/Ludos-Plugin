package fr.ludos.game.manhunt;

import org.bukkit.boss.BarStyle;

public enum ManhuntRevealOptions {
	one_minute (60, BarStyle.SEGMENTED_6),
	often (120, BarStyle.SEGMENTED_12),
	occasional (180, BarStyle.SEGMENTED_6),
	rare (360, BarStyle.SEGMENTED_12),;

	private int duration;
	public int getDuration() {
		return duration;
	}

	private BarStyle barStyle;
	public BarStyle getBarStyle() {
		return barStyle;
	}

	private ManhuntRevealOptions(int duration, BarStyle barStyle) {
		this.duration = duration;
		this.barStyle = barStyle;
	}
}