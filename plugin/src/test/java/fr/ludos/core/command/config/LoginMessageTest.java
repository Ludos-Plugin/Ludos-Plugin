package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;

class LoginMessageTest extends ConfigTest {
	@BeforeEach
	void resetConfig() {
		PlayerMock player1 = createPlayer("Player1");
		player1.setOp(true);
		clearMessages(player1);

		player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + PlayerConfigMap.GUIDEBOOK_MESSAGE.placeholderValue());
		assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " reset", player1.nextMessage(), "Invalid Reset message for configuration.");
	}

	// @Test
	// void testDisableLoginMessage() {
	// 	PlayerMock player1 = createPlayer("Player1");
	// 	assertNotNull(player1.nextMessage());
	// 	player1.setOp(true);

	// 	assertSetConfigValues(player1, "ludos config global player", PlayerConfigMap.GUIDEBOOK_MESSAGE, "invalid");

	// 	player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanConfigOptions.FALSE_STRING);
	// 	assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " set to " + BooleanConfigOptions.FALSE_STRING, player1.nextMessage(), "Could not disable login message globally.");
	// 	PlayerMock player2 = createPlayer("Player2");
	// 	assertNull(player2.nextMessage(), "Player received login message when disabled.");

	// 	player1.performCommand("ludos config global player " + PlayerConfigMap.GUIDEBOOK_MESSAGE.key() + " " + BooleanConfigOptions.TRUE_STRING);
	// 	assertEquals(PlayerConfigMap.GUIDEBOOK_MESSAGE.getName() + " set to " + BooleanConfigOptions.TRUE_STRING, player1.nextMessage(), "Could not enable login message globally.");
	// 	PlayerMock player3 = createPlayer("Player3");
	// 	assertNotNull(player3.nextMessage(), "Player dit not receive login message when enabled.");
	// }
}
