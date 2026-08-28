package fr.ludos.core.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import fr.ludos.core.game.teamController.GameJoinOption;


class GameJoinOptionTest {
	@Test
	@DisplayName("Should return all join options and usage format")
	void testGetOptionsAndUsage() {
		String usage = GameJoinOption.getUsage();
		assertTrue(usage.contains("yes"));
		assertTrue(usage.contains("spectator"));
		assertTrue(usage.contains("no"));
		assertTrue(usage.startsWith("<") && usage.endsWith(">"));
	}
}