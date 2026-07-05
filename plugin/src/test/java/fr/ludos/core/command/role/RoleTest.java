package fr.ludos.core.command.role;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.role.Role;

abstract class RoleTest extends MockBukkitTestBase {
	@BeforeEach
	void reset() {
		super.resetPlayers();
		resetRole(player1);
		resetRole(player2);
	}

	protected void resetRole(PlayerMock player) {
		player.performCommand("ludos role reset");
		clearMessages(player);
		player.performCommand("ludos role get");
		assertEquals(Role.noneLabel, player.nextMessage(), "Role is not unset by default");
	}
}
