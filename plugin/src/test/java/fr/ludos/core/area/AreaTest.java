package fr.ludos.core.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import fr.ludos.core.Ludos;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AreaTest {
	private ServerMock server;
	private Area testArea;
	private Location mockLocation;
	private World mockWorld;
	private Ludos mockLudos;


	@BeforeAll
	void setUpAll() {
		server = MockBukkit.mock();
	}

	@AfterAll
	void tearDownAll() {
		MockBukkit.unmock();
	}


	@BeforeEach
	void setUp() {
		mockWorld = server.addSimpleWorld("testWorld");
		mockLocation = new Location(mockWorld, 0, 64, 0);
		mockLudos = mock(Ludos.class);


		// Create a concrete implementation for testing
		testArea = new Area(mockWorld, mockLudos) {
			@Override
			public Location getCenter() {
				return mockLocation;
			}
			@Override
			public Location pickRandom(double startFactor, double endFactor) {
				return new Location(mockWorld, startFactor * 10, 64, endFactor * 10);
			}
			@Override
			public Location constrain(Location location) {
				return location;
			}
			@Override
			public boolean isInside(Location location) {
				return true;
			}
		};
	}

	@Test
	@DisplayName("Should pick random location starting from factor")
	void testPickRandomStartingAt() {
		Location result = testArea.pickRandomStartingAt(0.5);


		assertEquals(5.0, result.getX(), 0.01);
		assertEquals(10.0, result.getZ(), 0.01);
	}

	@Test
	@DisplayName("Should pick random location ending at factor")
	void testPickRandomEndingAt() {
		Location result = testArea.pickRandomEndingAt(0.3);


		assertEquals(0.0, result.getX(), 0.01);
		assertEquals(3.0, result.getZ(), 0.01);
	}
}