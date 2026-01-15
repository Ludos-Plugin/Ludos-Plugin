package fr.ludos.game.alien;

import java.util.function.Supplier;

public enum AlienGameConfigs {
	players ("players", () -> "[player1] [player2] ..."),
	prey ("prey", () -> "[player]"),
	location ("location", AlienLocationOptions::getUsage),
	spawn ("spawn", () -> "[here]"),
	reveal ("reveal", AlienRevealOptions::getUsage);

	private String name;
	public String toString() {
		return name;
	}

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private AlienGameConfigs(String name, Supplier<String> usageGetter) {
		this.name = name;
		this.usageGetter = usageGetter;
	}
}