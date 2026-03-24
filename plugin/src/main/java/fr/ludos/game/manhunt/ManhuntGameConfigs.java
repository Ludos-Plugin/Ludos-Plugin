package fr.ludos.game.manhunt;

import java.util.function.Supplier;

import fr.ludos.game.GameJoinOption;
import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;

public enum ManhuntGameConfigs {
	prey (() -> "[player]"),
	area (WorldBorderAreaOption::getUsage),
	location (WorldBorderLocationOption::getUsage),
	reveal (ManhuntRevealOptions::getUsage),
	join (GameJoinOption::getUsage);

	private Supplier<String> usageGetter;
	public String getUsage() {
		return usageGetter.get();
	}

	private ManhuntGameConfigs(Supplier<String> usageGetter) {
		this.usageGetter = usageGetter;
	}
}