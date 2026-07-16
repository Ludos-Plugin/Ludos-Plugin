package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.command.ludos.config.LudosConfigMap;
import fr.ludos.core.config.BooleanConfigOptions;

class LoginMessageTest extends MockBukkitTestBase {
	@BeforeEach
	void resetConfig() {
		PlayerMock player1 = createPlayer("Player1");
		player1.setOp(true);
		clearMessages(player1);

		player1.performCommand("ludos config global ludos " + LudosConfigMap.GUIDEBOOK_MESSAGE.key() + " " + LudosConfigMap.GUIDEBOOK_MESSAGE.emptyValue());
		assertEquals(LudosConfigMap.GUIDEBOOK_MESSAGE.getName() + " reset", player1.nextMessage(), "Invalid Reset message for configuration.");
	}

	@Test
	void testDisableLoginMessage() {
		PlayerMock player1 = createPlayer("Player1");
		assertNotNull(player1.nextMessage());
		player1.setOp(true);

		player1.performCommand("ludos config global ludos " + LudosConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanConfigOptions.FALSE_STRING);
		PlayerMock player2 = createPlayer("Player2");
		assertNull(player2.nextMessage(), "Player received login message when disabled.");

		player1.performCommand("ludos config global ludos " + LudosConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanConfigOptions.TRUE_STRING);
		PlayerMock player3 = createPlayer("Player3");
		assertNotNull(player3.nextMessage(), "Player dit not receive login message when enabled.");
	}
}
