package fr.ludos.core.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AreaTest {
	private ServerMock server;
	private Area testArea;
	private Location mockLocation;
	private World mockWorld;


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


		// Create a concrete implementation for testing
		testArea = new Area() {
			@Override
			public Builder<? extends Area> getBuilder() {
				return new Builder<Area>() {
					@Override
					public Area build() {
						return testArea;
					}
				};
			}
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
			@Override
			protected JavaPlugin getPlugin() {
				return mock(JavaPlugin.class);
			}
		};
	}


	@Test
	@DisplayName("Should mutate area using consumer")
	void testMutate() {
		Consumer<Area.Builder<?>> config = builder -> {
			// Mock the builder behavior
		};


		Area result = testArea.mutate(config);


		assertNotNull(result);
		assertEquals(testArea, result);
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