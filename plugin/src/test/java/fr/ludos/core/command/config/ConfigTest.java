package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.persistence.config.valueEntry.ConfigEntry;

abstract class ConfigTest extends MockBukkitTestBase {
	private <TComplex, TPrimitive> void assertSetValidConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry, Collection<String> values) {
		for (String value : values) {
			String stringRep = entry.toString(entry.parseValueFromArgs(value.split(" "), player));

			player.performCommand(path + ' ' + entry.key() + " set " + value);
			assertEquals(entry.displayName().content() + " set to " + stringRep, player.nextMessage(), "Could not set Config entry value to a valid option.");

			player.performCommand(path + ' ' + entry.key() + " get");
			assertEquals(entry.getterMessage(stringRep), player.nextMessage(), "Invalid value return after fetching set Config entry.");
		}
	}
	private <TComplex, TPrimitive> void assertSetValidConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry) {
		assertSetValidConfigValues(player, path, entry, entry.options(player));
	}
	private <TComplex, TPrimitive> void assertResetConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry) {
		player.performCommand(path + ' ' + entry.key() + " reset");
		assertEquals(entry.displayName().content() + " reset", player.nextMessage(), "Could not reset Config entry value.");

		player.performCommand(path + ' ' + entry.key() + " get");
		assertEquals(entry.getterMessage((String) null), player.nextMessage(), "Value was not reset after resetting Config entry.");
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, entry);

		assertResetConfigValues(player, path, entry);
	}
	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry, Collection<String> additionalValues) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, entry);

		assertSetValidConfigValues(player, path, entry, additionalValues);

		assertResetConfigValues(player, path, entry);
	}


	private <TComplex, TPrimitive> void assertSetNonsenseConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry, String nonsense) {
		player.performCommand(path + ' ' + entry.key() + " get");
		String previousValue = player.nextMessage();
		assertNotNull(previousValue, "Could not fetch current Config entry value via get command.");

		player.performCommand(path + ' ' + entry.key() + " set " + nonsense);
		assertNull(player.nextMessage(), "Successfully set Config entry value to invalid option.");

		player.performCommand(path + ' ' + entry.key() + " get");
		String newValue = player.nextMessage();
		assertNotNull(newValue, "Could not fetch current Config entry value via get command.");

		assertEquals(previousValue, newValue);
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry, String nonsense) {
		assertSetConfigValues(player, path, entry);

		assertSetNonsenseConfigValues(player, path, entry, nonsense);
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ConfigEntry<TComplex, TPrimitive> entry, Collection<String> additionalValues, String nonsense) {
		assertSetConfigValues(player, path, entry, additionalValues);

		assertSetNonsenseConfigValues(player, path, entry, nonsense);
	}
}
