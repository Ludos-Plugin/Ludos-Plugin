package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.persistence.config.valueEntry.ValueConfigEntry;

abstract class ConfigTest extends MockBukkitTestBase {
	private <TComplex, TPrimitive> void assertSetValidConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> entry, Collection<String> values) {
		for (String value : values) {
			String stringRep = entry.toString(entry.parseValueFromArgs(value.split(" "), player));

			player.performCommand(path + " " + entry.key() + " " + value);
			assertEquals(entry.getName() + " set to " + stringRep, player.nextMessage(), "Could not set Config options value to a valid option.");

			player.performCommand(path + " " + entry.key());
			assertEquals(entry.getterMessage(stringRep), player.nextMessage(), "Invalid value return after fetching set Config Options.");
		}
	}
	private <TComplex, TPrimitive> void assertSetValidConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options) {
		assertSetValidConfigValues(player, path, options, options.getValidOptions(player));
	}
	private <TComplex, TPrimitive> void assertResetConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options) {
		String placeHolder = options.placeholderValue();
		player.performCommand(path + " " + options.key() + " " + placeHolder);
		assertEquals(options.getName() + " reset", player.nextMessage(), "Could not reset Config options value.");

		player.performCommand(path + " " + options.key());
		assertEquals(options.getterMessage((String) null), player.nextMessage(), "Value was not reset after using Placeholder value in Config Options.");
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, options);

		assertResetConfigValues(player, path, options);
	}
	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options, Collection<String> additionalValues) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, options);

		assertSetValidConfigValues(player, path, options, additionalValues);

		assertResetConfigValues(player, path, options);
	}


	private <TComplex, TPrimitive> void assertSetNonsenseConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options, String nonsense) {
		player.performCommand(path + " " + options.key());
		String previousValue = player.nextMessage();
		assertNotNull(previousValue, "Could not fetch current Config Options value via parameterless call.");

		player.performCommand(path + " " + options.key() + " " + nonsense);
		assertNull(player.nextMessage(), "Successfully set Config options value to invalid option.");

		player.performCommand(path + " " + options.key());
		String newValue = player.nextMessage();
		assertNotNull(newValue, "Could not fetch current Config Options value via parameterless call.");

		assertEquals(previousValue, newValue);
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options, String nonsense) {
		assertSetConfigValues(player, path, options);

		assertSetNonsenseConfigValues(player, path, options, nonsense);
	}

	protected <TComplex, TPrimitive> void assertSetConfigValues(PlayerMock player, String path, ValueConfigEntry<TComplex, TPrimitive> options, Collection<String> additionalValues, String nonsense) {
		assertSetConfigValues(player, path, options, additionalValues);

		assertSetNonsenseConfigValues(player, path, options, nonsense);
	}
}
