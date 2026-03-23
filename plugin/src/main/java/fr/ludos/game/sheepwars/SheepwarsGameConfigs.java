package fr.ludos.game.sheepwars;

import java.util.function.Supplier;

public enum SheepwarsGameConfigs {
	players ("players", () -> "[player1] [player2] ..."),
	teams ("teams", SheepwarsTeamOptions::getUsage),
	area ("area", SheepwarsAreaOptions::getUsage),
	location ("location", SheepwarsLocationOptions::getUsage);

	private String name;
	public String toString() {
		return name;
	}

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private SheepwarsGameConfigs(String name, Supplier<String> usageGetter) {
		this.name = name;
		this.usageGetter = usageGetter;
	}
}