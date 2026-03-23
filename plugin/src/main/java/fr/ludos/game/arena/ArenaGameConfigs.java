package fr.ludos.game.arena;

import java.util.function.Supplier;

import fr.ludos.game.GameJoinOption;
import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;

public enum ArenaGameConfigs {
	teamA(() -> "[player1] [player2] ..."),
	teamB(() -> "[player1] [player2] ..."),
	mode(ArenaModeOption::getUsage),
	rounds(() -> "<number>"),
	area(WorldBorderAreaOption::getUsage),
	location(WorldBorderLocationOption::getUsage),
	join(GameJoinOption::getUsage);

	private final Supplier<String> usageGetter;

	public String getUsage() {
		return usageGetter.get();
	}

	private ArenaGameConfigs(Supplier<String> usageGetter) {
		this.usageGetter = usageGetter;
	}
}
