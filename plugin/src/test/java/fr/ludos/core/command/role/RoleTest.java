package fr.ludos.core.command.role;

import static org.junit.jupiter.api.Assertions.assertEquals;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;

abstract class RoleTest extends MockBukkitTestBase {

	@Override
	public void initPlayer(PlayerMock player) {
		super.initPlayer(player);
		clearMessages(player);
		player.performCommand("ludos role get");
		assertEquals("none", player.nextMessage(), "Created player with a role");
		clearMessages(player);
	}
}
