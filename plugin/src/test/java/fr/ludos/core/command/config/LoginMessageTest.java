package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.persistence.serializer.BooleanSerializer;

class LoginMessageTest extends ConfigTest {
	@BeforeEach
	void resetConfig() {
		PlayerMock player1 = createPlayer("Player1");
		player1.setOp(true);
		clearMessages(player1);

		player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + PlayerConfigMap.GUIDEBOOK_MESSAGE.placeholderValue());
		assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " reset", player1.nextMessage(), "Invalid Reset message for configuration.");
	}

	@Test
	void testDisableLoginMessage() {
		PlayerMock player1 = createPlayer("Player1");
		assertNotNull(player1.nextMessage());
		player1.setOp(true);

		assertSetConfigValues(player1, "ludos config global player", PlayerConfigMap.GUIDEBOOK_MESSAGE, "invalid");

		player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanSerializer.FALSE_STRING);
		assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " set to " + BooleanSerializer.FALSE_STRING, player1.nextMessage(), "Could not disable login message globally.");
		PlayerMock player2 = createPlayer("Player2");
		assertNull(player2.nextMessage(), "Player received login message when disabled.");

		player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanSerializer.TRUE_STRING);
		assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " set to " + BooleanSerializer.TRUE_STRING, player1.nextMessage(), "Could not enable login message globally.");
		PlayerMock player3 = createPlayer("Player3");
		assertNotNull(player3.nextMessage(), "Player dit not receive login message when enabled.");
	}
}
