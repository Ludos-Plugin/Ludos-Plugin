package fr.ludos.game.manhunt;

import java.util.function.Supplier;

import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;

public enum ManhuntGameConfigs {
	players (() -> "[player1] [player2] ..."),
	prey (() -> "[player]"),
	area (WorldBorderAreaOption::getUsage),
	location (WorldBorderLocationOption::getUsage),
	reveal (ManhuntRevealOptions::getUsage);

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private ManhuntGameConfigs(Supplier<String> usageGetter) {
		this.usageGetter = usageGetter;
	}
}