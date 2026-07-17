package fr.ludos.core.command.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collection;

import be.seeseemelk.mockbukkit.entity.PlayerMock;
import fr.ludos.core.command.MockBukkitTestBase;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;

abstract class ConfigTest extends MockBukkitTestBase {
	private <T> void assertSetValidConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options, Collection<String> values) {
		for (String value : values) {
			String stringRep = options.toString(options.parseValueFromArgs(value.split(" "), player));

			player.performCommand(path + " " + options.key() + " " + value);
			assertEquals(options.getName() + " set to " + stringRep, player.nextMessage(), "Could not set Config options value to a valid option.");

			player.performCommand(path + " " + options.key());
			assertEquals(options.getterMessage(stringRep), player.nextMessage(), "Invalid value return after fetching set Config Options.");
		}
	}
	private <T> void assertSetValidConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options) {
		assertSetValidConfigValues(player, path, options, options.getValidOptions(player));
	}
	private <T> void assertResetConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options) {
		String placeHolder = options.placeholderValue();
		player.performCommand(path + " " + options.key() + " " + placeHolder);
		assertEquals(options.getName() + " reset", player.nextMessage(), "Could not reset Config options value.");

		player.performCommand(path + " " + options.key());
		assertEquals(options.getterMessage((String) null), player.nextMessage(), "Value was not reset after using Placeholder value in Config Options.");
	}

	protected <T> void assertSetConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, options);

		assertResetConfigValues(player, path, options);
	}
	protected <T> void assertSetConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options, Collection<String> additionalValues) {
		clearMessages(player);

		assertSetValidConfigValues(player, path, options);

		assertSetValidConfigValues(player, path, options, additionalValues);

		assertResetConfigValues(player, path, options);
	}


	private <T> void assertSetNonsenseConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options, String nonsense) {
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

	protected <T> void assertSetConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options, String nonsense) {
		assertSetConfigValues(player, path, options);

		assertSetNonsenseConfigValues(player, path, options, nonsense);
	}

	protected <T> void assertSetConfigValues(PlayerMock player, String path, ValueConfigOptions<T> options, Collection<String> additionalValues, String nonsense) {
		assertSetConfigValues(player, path, options, additionalValues);

		assertSetNonsenseConfigValues(player, path, options, nonsense);
	}
}
