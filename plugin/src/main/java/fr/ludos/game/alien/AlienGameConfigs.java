package fr.ludos.game.alien;

import java.util.function.Supplier;

public enum AlienGameConfigs {
	players("players", () -> "[player1] [player2] ..."),
	prey("prey", () -> "[player]"),
	location("location", AlienLocationOptions::getUsage),
	spawn("spawn", () -> "[here]"),
	reveal("reveal", AlienRevealOptions::getUsage),
	alien_spawn_distance("alien_spawn_distance", () -> "[number]");

	private final String name;
	private final Supplier<String> usageGetter;

	@Override
	public String toString() {
		return name;
	}

	public String getUsage() {
		return usageGetter.get();
	}

	private AlienGameConfigs(String name, Supplier<String> usageGetter) {
		this.name = name;
		this.usageGetter = usageGetter;
	}
}