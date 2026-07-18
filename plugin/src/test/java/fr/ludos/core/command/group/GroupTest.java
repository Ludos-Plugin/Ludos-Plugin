package fr.ludos.core.command.group;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;

abstract class GroupTest extends MockBukkitTestBase {

	@Override
	public void initPlayer(PlayerMock player) {
		super.initPlayer(player);
		clearMessages(player);
	}
}