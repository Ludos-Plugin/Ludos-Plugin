package fr.ludos.game.manhunt;

import java.util.function.Supplier;

public enum ManhuntGameConfigs {
	players ("players", () -> "[player1] [player2] ..."),
	prey ("prey", () -> "[player]"),
	area ("area", ManhuntAreaOptions::getUsage),
	location ("location", ManhuntLocationOptions::getUsage),
	reveal ("reveal", ManhuntRevealOptions::getUsage);

	private String name;
	public String toString() {
		return name;
	}

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private ManhuntGameConfigs(String name, Supplier<String> usageGetter) {
		this.name = name;
		this.usageGetter = usageGetter;
	}
}