package fr.ludos.core.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupOptionsTest {
	@Test
	@DisplayName("GroupJoinOption: Devrait retourner toutes les options et le format d'usage")
	void testGroupJoinOptions() {
		List<String> options = GroupJoinOption.getOptions();
		assertTrue(options.contains("auto_accept"));
		assertTrue(options.contains("manual_accept"));
		assertEquals(2, options.size());

		String usage = GroupJoinOption.getUsage();
		assertTrue(usage.contains("auto_accept"));
		assertTrue(usage.contains("manual_accept"));
		assertTrue(usage.startsWith("<") && usage.endsWith(">"));
	}

	@Test
	@DisplayName("GroupRightsOption: Devrait vérifier les capacités par niveau")
	void testGroupRightsOptionsCapabilities() {
		// none
		assertFalse(GroupRightsOption.none.canInvite());
		assertFalse(GroupRightsOption.none.canRunGames());
		assertFalse(GroupRightsOption.none.canConfig());
		assertFalse(GroupRightsOption.none.canManage());

		// invite
		assertTrue(GroupRightsOption.invite.canInvite());
		assertFalse(GroupRightsOption.invite.canRunGames());
		assertFalse(GroupRightsOption.invite.canConfig());
		assertFalse(GroupRightsOption.invite.canManage());

		// game
		assertTrue(GroupRightsOption.game.canInvite());
		assertTrue(GroupRightsOption.game.canRunGames());
		assertFalse(GroupRightsOption.game.canConfig());
		assertFalse(GroupRightsOption.game.canManage());

		// config
		assertTrue(GroupRightsOption.config.canConfig());
		assertTrue(GroupRightsOption.config.canRunGames());
		assertTrue(GroupRightsOption.config.canConfig());
		assertFalse(GroupRightsOption.config.canManage());

		// all
		assertTrue(GroupRightsOption.all.canConfig());
		assertTrue(GroupRightsOption.all.canRunGames());
		assertTrue(GroupRightsOption.all.canConfig());
		assertTrue(GroupRightsOption.all.canManage());
	}

	@Test
	@DisplayName("GroupRightsOption: Devrait retourner le format d'usage")
	void testGroupRightsUsage() {
		String usage = GroupRightsOption.getUsage();
		assertTrue(usage.contains("none"));
		assertTrue(usage.contains("all"));
		assertTrue(usage.startsWith("<") && usage.endsWith(">"));
	}
}