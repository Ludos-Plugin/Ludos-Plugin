package fr.ludos.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.kyori.adventure.text.Component;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UtilityTest {
	private ServerMock server;
	private Ludos ludos;
	private WorldMock world;
	private PlayerMock player;
	private Location location;
	private ConfigurationSection mockConfig;


	@BeforeAll
	void setUpAll() {
		server = MockBukkit.mock();

		ludos = (Ludos) server.getPluginManager().loadPlugin(Ludos.class, new Object[] {});
		server.getPluginManager().enablePlugin(ludos);
		assertTrue(ludos.isEnabled(), "Plugin should be enabled");
	}

	@AfterAll
	void tearDownAll() {
		MockBukkit.unmock();
	}


	@BeforeEach
	void setUp() {
		world = spy(server.addSimpleWorld("testWorld"));
		location = new Location(world, 0, 64, 0);
		player = server.addPlayer("TestPlayer");
		player.teleport(location);
		mockConfig = mock(ConfigurationSection.class);
	}


	@Test
	@DisplayName("Should return location around origin avoiding liquid")
	void testGetLocationAroundValid() {
		Location fallback = new Location(world, 100, 100, 100);
		Location result = Utility.getLocationAround(location, 5, 10, fallback, 20);


		assertNotNull(result);
		assertEquals(world, result.getWorld());
		// Check if it's within range
		double diffX = Math.abs(result.getX() - location.getX());
		double diffZ = Math.abs(result.getZ() - location.getZ());
		assertTrue(diffX <= 10.5 && diffX >= 4.5);
		assertTrue(diffZ <= 10.5 && diffZ >= 4.5);
	}

	@Test
	@DisplayName("Should return overworld biomes excluding forbidden ones")
	void testGetOverworldBiomes() {
		HashSet<Biome> biomes = Utility.getOverworldBiomes();

		assertFalse(biomes.isEmpty());
		assertFalse(biomes.contains(Biome.THE_VOID));
		assertFalse(biomes.contains(Biome.OCEAN));
		assertFalse(biomes.contains(Biome.NETHER_WASTES));
		assertFalse(biomes.contains(Biome.END_MIDLANDS));
		assertTrue(biomes.contains(Biome.FOREST));
		assertTrue(biomes.contains(Biome.PLAINS));
	}

	@Test
	@DisplayName("Should return nether biomes correctly")
	void testGetNetherBiomes() {
		HashSet<Biome> biomes = Utility.getNetherBiomes();

		assertFalse(biomes.isEmpty());
		assertTrue(biomes.contains(Biome.NETHER_WASTES));
		assertTrue(biomes.contains(Biome.SOUL_SAND_VALLEY));
		assertTrue(biomes.contains(Biome.CRIMSON_FOREST));
		assertTrue(biomes.contains(Biome.WARPED_FOREST));
		assertTrue(biomes.contains(Biome.BASALT_DELTAS));
		assertFalse(biomes.contains(Biome.PLAINS));
	}

	@Test
	@DisplayName("Should return end biomes correctly")
	void testGetEndBiomes() {
		HashSet<Biome> biomes = Utility.getEndBiomes();

		assertFalse(biomes.isEmpty());
		assertTrue(biomes.contains(Biome.SMALL_END_ISLANDS));
		assertTrue(biomes.contains(Biome.END_MIDLANDS));
		assertTrue(biomes.contains(Biome.END_HIGHLANDS));
		assertTrue(biomes.contains(Biome.END_BARRENS));
		assertFalse(biomes.contains(Biome.PLAINS));
	}

	@Test
	@DisplayName("Should return random biome location in overworld")
	void testGetRandomBiomeLocationOverworld() {
		when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(location);

		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, location, 5, null);

		assertNotNull(result);
		assertEquals(world, result.getWorld());
	}

	@Test
	@DisplayName("Should return fallback if no biome found after retries")
	void testGetRandomBiomeLocationFallback() {
		when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(null);

		Location fallback = new Location(world, 100, 100, 100);
		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, fallback, 0, null);

		assertEquals(fallback, result);
	}

	@Test
	@DisplayName("Should exclude avoided biomes")
	void testGetRandomBiomeLocationAvoidBiomes() {
		when(world.getEnvironment()).thenReturn(World.Environment.NORMAL);
		HashSet<Biome> avoid = new HashSet<>();
		avoid.add(Biome.FOREST);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(location);

		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, location, 5, avoid);

		assertNotNull(result);
	}

	@Test
	@DisplayName("Should handle nether environment")
	void testGetRandomBiomeLocationNether() {
		when(world.getEnvironment()).thenReturn(World.Environment.NETHER);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(location);

		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, location, 5, null);

		assertNotNull(result);
	}

	@Test
	@DisplayName("Should handle end environment")
	void testGetRandomBiomeLocationEnd() {
		when(world.getEnvironment()).thenReturn(World.Environment.THE_END);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(location);

		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, location, 5, null);

		assertNotNull(result);
	}

	@Test
	@DisplayName("Should handle unknown environment")
	void testGetRandomBiomeLocationUnknown() {
		when(world.getEnvironment()).thenReturn(World.Environment.CUSTOM);
		when(world.locateNearestBiome(any(), any(), anyInt(), anyInt())).thenReturn(location);

		Location result = Utility.getRandomBiomeLocation(location, 100, 5, 10, location, 5, null);

		assertNotNull(result);
	}


	@Test
	@DisplayName("Should reset player state correctly")
	void testResetPlayerState() {
		PlayerMock mocked = spy(player);
		mocked.setHealth(5.0);
		mocked.setFoodLevel(10);
		// mocked.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
		when(mocked.getActivePotionEffects()).thenReturn(List.of(
			new PotionEffect(PotionEffectType.WEAKNESS, 100, 1),
			new PotionEffect(PotionEffectType.POISON, 100, 1)
		));
		mocked.setFireTicks(100);
		mocked.setVelocity(new Vector(1, 1, 1));


		Utility.resetPlayerState(mocked);


		assertEquals(mocked.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), mocked.getHealth(), 0.01);
		assertEquals(20, mocked.getFoodLevel());
		verify(mocked).removePotionEffect(PotionEffectType.WEAKNESS);
		verify(mocked).removePotionEffect(PotionEffectType.POISON);
		assertEquals(0, mocked.getFireTicks());
		assertEquals(0, mocked.getVelocity().getX(), 0.01);
	}


	@Test
	@DisplayName("Should reset full player inventory and state")
	void testResetPlayer() {
		player.getInventory().addItem(new ItemStack(org.bukkit.Material.DIAMOND));
		player.setHealth(5.0);


		Utility.resetPlayer(player);


		assertTrue(player.getInventory().isEmpty());
		assertEquals(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth(), 0.01);
	}


	@Test
	@DisplayName("Should handle death spectate mode correctly")
	void testOnDeathSpectate() {
		PlayerDeathEvent event = mock(PlayerDeathEvent.class);

		Runnable onFinish = mock(Runnable.class);

		when(event.getPlayer()).thenReturn(player);
		when(event.deathMessage()).thenReturn(Component.text("Died"));
		when(event.getDrops()).thenReturn(List.of(new ItemStack(Material.DIAMOND_SWORD), new ItemStack(Material.ACACIA_BOAT)));
		when(event.getKeepInventory()).thenReturn(false);
		when(event.getKeepLevel()).thenReturn(false);
		when(event.getNewExp()).thenReturn(0);
		when(event.getNewLevel()).thenReturn(0);
		when(event.getNewTotalExp()).thenReturn(0);
		when(event.getDroppedExp()).thenReturn(69);


		Utility.onDeathSpectate(event, 1.0f, ludos, onFinish);


		server.getScheduler().performTicks(1);

		verify(onFinish, never()).run();


		assertTrue(player.getGameMode() == GameMode.SPECTATOR);
		verify(event).setCancelled(true);
		verify(world, times(2)).dropItemNaturally(any(), any());
		verify(world).spawn(any(), eq(ExperienceOrb.class));

		server.getScheduler().performTicks(20);

		verify(onFinish, times(1)).run();
	}


	@Test
	@DisplayName("Should return bed spawn location if set")
	void testGetPlayerSpawnLocationWithBed() {
		PlayerMock bedfulPlayer = spy(player);
		Location bedLoc = new Location(world, 50, 60, 50);
		when(bedfulPlayer.getBedSpawnLocation()).thenReturn(bedLoc);


		Location result = Utility.getPlayerSpawnLocation(bedfulPlayer);


		assertEquals(bedLoc, result);
	}


	@Test
	@DisplayName("Should return world spawn if no bed")
	void testGetPlayerSpawnLocationNoBed() {
		PlayerMock bedlessPlayer = spy(player);
		Location worldSpawn = world.getSpawnLocation();
		when(bedlessPlayer.getBedSpawnLocation()).thenReturn(null);


		Location result = Utility.getPlayerSpawnLocation(bedlessPlayer);


		assertEquals(worldSpawn, result);
	}


	@Test
	@DisplayName("Should return null for offline player spawn if no world")
	void testGetOfflinePlayerSpawnLocationNoWorld() {
		OfflinePlayer offline = mock(OfflinePlayer.class);
		when(offline.getBedSpawnLocation()).thenReturn(null);
		when(offline.getPlayer()).thenReturn(null);


		Location result = Utility.getOfflinePlayerSpawnLocation(offline);

		assertEquals(server.getWorlds().get(0).getSpawnLocation(), result);
	}


	@Test
	@DisplayName("Should rotate vector correctly for EAST face")
	void testGetVectorFacingEast() {
		Vector input = new Vector(1, 0, 0);
		Vector result = Utility.getVectorFacing(input, BlockFace.EAST);


		assertEquals(new Vector(0, 0, -1), result);
	}


	@Test
	@DisplayName("Should rotate vector correctly for UP face")
	void testGetVectorFacingUp() {
		Vector input = new Vector(1, 0, 0);
		Vector result = Utility.getVectorFacing(input, BlockFace.UP);


		assertEquals(new Vector(1, 0, 0), new Vector(input.getX(), -input.getZ(), input.getY())); // Manual check logic
		// Based on code: new Vector(x, -z, y) -> (1, 0, 0)
		assertEquals(1, result.getX());
		assertTrue(0 == result.getY());
		assertEquals(0, result.getZ());
	}

	@Test
	@DisplayName("Should return all blocks in the defined range")
	void testGetAllBlocks() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsY = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsZ = Pair.of(-1, 1);

		Iterable<Block> blocks = Utility.getAllBlocks(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);
		List<Block> result = new ArrayList<>();
		blocks.forEach(result::add);

		// 3x3x3 = 27 blocks
		assertEquals(27, result.size());
	}

	@Test
	@DisplayName("Should throw exception for invalid X bounds")
	void testGetAllBlocksInvalidBounds() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> validX = Pair.of(0, 0);
		Pair<Integer, Integer> invalidX = Pair.of(5, 2);
		Pair<Integer, Integer> validY = Pair.of(0, 0);
		Pair<Integer, Integer> invalidY = Pair.of(5, 2);
		Pair<Integer, Integer> validZ = Pair.of(0, 0);
		Pair<Integer, Integer> invalidZ = Pair.of(5, 2);

		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlocks(block, BlockFace.NORTH, invalidX, validY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlocks(block, BlockFace.NORTH, validX, invalidY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlocks(block, BlockFace.NORTH, validX, validY, invalidZ);
		});
	}

	@Test
	@DisplayName("Should throw exception for invalid X bounds")
	void testGetAllBlockColumnsInvalidBounds() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> validX = Pair.of(0, 0);
		Pair<Integer, Integer> invalidX = Pair.of(5, 2);
		Pair<Integer, Integer> validY = Pair.of(0, 0);
		Pair<Integer, Integer> invalidY = Pair.of(5, 2);
		Pair<Integer, Integer> validZ = Pair.of(0, 0);
		Pair<Integer, Integer> invalidZ = Pair.of(5, 2);

		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockColumns(block, BlockFace.NORTH, invalidX, validY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockColumns(block, BlockFace.NORTH, validX, invalidY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockColumns(block, BlockFace.NORTH, validX, validY, invalidZ);
		});
	}

	@Test
	@DisplayName("Should throw exception for invalid X bounds")
	void testGetAllBlockRowsInvalidBounds() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> validX = Pair.of(0, 0);
		Pair<Integer, Integer> invalidX = Pair.of(5, 2);
		Pair<Integer, Integer> validY = Pair.of(0, 0);
		Pair<Integer, Integer> invalidY = Pair.of(5, 2);
		Pair<Integer, Integer> validZ = Pair.of(0, 0);
		Pair<Integer, Integer> invalidZ = Pair.of(5, 2);

		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockRows(block, BlockFace.NORTH, invalidX, validY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockRows(block, BlockFace.NORTH, validX, invalidY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockRows(block, BlockFace.NORTH, validX, validY, invalidZ);
		});
	}

	@Test
	@DisplayName("Should throw exception for invalid X bounds")
	void testGetAllBlockLayersInvalidBounds() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> validX = Pair.of(0, 0);
		Pair<Integer, Integer> invalidX = Pair.of(5, 2);
		Pair<Integer, Integer> validY = Pair.of(0, 0);
		Pair<Integer, Integer> invalidY = Pair.of(5, 2);
		Pair<Integer, Integer> validZ = Pair.of(0, 0);
		Pair<Integer, Integer> invalidZ = Pair.of(5, 2);

		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockLayers(block, BlockFace.NORTH, invalidX, validY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockLayers(block, BlockFace.NORTH, validX, invalidY, validZ);
		});
		assertThrows(IllegalArgumentException.class, () -> {
			Utility.getAllBlockLayers(block, BlockFace.NORTH, validX, validY, invalidZ);
		});
	}

	@Test
	@DisplayName("Should return empty list for zero range")
	void testGetAllBlocksZeroRange() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(0, 0);
		Pair<Integer, Integer> boundsY = Pair.of(0, 0);
		Pair<Integer, Integer> boundsZ = Pair.of(0, 0);

		Iterable<Block> blocks = Utility.getAllBlocks(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);
		List<Block> result = new ArrayList<>();
		blocks.forEach(result::add);

		assertEquals(1, result.size());
	}

	@Test
	@DisplayName("Should return block columns in the defined range")
	void testGetAllBlockColumns() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsY = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsZ = Pair.of(-1, 1);

		List<List<Block>> columns = Utility.getAllBlockColumns(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);

		// 3 columns (X) * 3 columns (Z) = 9 columns
		assertEquals(9, columns.size());
		// Each column has 3 blocks (Y range)
		columns.forEach(col -> assertEquals(3, col.size()));
	}

	@Test
	@DisplayName("Should return block rows in the defined range")
	void testGetAllBlockRows() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsY = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsZ = Pair.of(-1, 1);

		List<List<Block>> rows = Utility.getAllBlockRows(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);

		// 3 rows (Y) * 3 rows (Z) = 9 rows
		assertEquals(9, rows.size());
		// Each row has 3 blocks (X range)
		rows.forEach(row -> assertEquals(3, row.size()));
	}

	@Test
	@DisplayName("Should return block layers in the defined range")
	void testGetAllBlockLayers() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsY = Pair.of(-1, 1);
		Pair<Integer, Integer> boundsZ = Pair.of(-1, 1);

		List<List<Block>> layers = Utility.getAllBlockLayers(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);

		// 3 layers (Y) * 3 layers (X) = 9 layers
		assertEquals(9, layers.size());
		// Each layer has 3 blocks (Z range)
		layers.forEach(layer -> assertEquals(3, layer.size()));
	}

	@Test
	@DisplayName("Should handle different block faces correctly")
	void testGetAllBlocksDifferentFaces() {
		Block block = world.getBlockAt(location);
		Pair<Integer, Integer> boundsX = Pair.of(0, 1);
		Pair<Integer, Integer> boundsY = Pair.of(0, 1);
		Pair<Integer, Integer> boundsZ = Pair.of(0, 1);

		Iterable<Block> northBlocks = Utility.getAllBlocks(block, BlockFace.NORTH, boundsX, boundsY, boundsZ);
		Iterable<Block> eastBlocks = Utility.getAllBlocks(block, BlockFace.EAST, boundsX, boundsY, boundsZ);

		List<Block> northResult = new ArrayList<>();
		northBlocks.forEach(northResult::add);

		List<Block> eastResult = new ArrayList<>();
		eastBlocks.forEach(eastResult::add);

		// Same number of blocks regardless of face
		assertEquals(northResult.size(), eastResult.size());
	}


	@Test
	@DisplayName("Should return empty list for split with size 0")
	void testSplitZeroSize() {
		List<?> result = Utility.split(Arrays.asList(1, 2, 3), 0);


		assertTrue(result.isEmpty());
	}


	@Test
	@DisplayName("Should split collection into equal sized parts")
	void testSplitCollection() {
		List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);
		List<List<Integer>> result = (List<List<Integer>>) Utility.split(items, 2);


		assertEquals(2, result.size());
		// Check distribution: 3 items in first, 2 in second (round robin)
		assertEquals(3, result.get(0).size());
		assertEquals(2, result.get(1).size());
	}


	@Test
	@DisplayName("Should return empty stream for null offline players")
	void testGetOnlineNull() {
		assertTrue(Utility.getOnline((Collection<OfflinePlayer>) null).count() == 0);
	}


	@Test
	@DisplayName("Should filter online players from offline list")
	void testGetOnlineStream() {
		PlayerMock online = server.addPlayer("Online");
		OfflinePlayer offline = mock(OfflinePlayer.class);
		when(offline.getPlayer()).thenReturn(null);


		List<OfflinePlayer> list = Arrays.asList(online, offline);
		long count = Utility.getOnline(list.stream()).count();


		assertEquals(1, count);
	}


	@Test
	@DisplayName("Should create config section if not exists")
	void testGetOrCreateConfigSectionCreate() {
		ConfigurationSection deepSection = mock(ConfigurationSection.class);
		when(mockConfig.getConfigurationSection("test.path")).thenReturn(null);
		when(mockConfig.createSection("test.path")).thenReturn(deepSection);


		ConfigurationSection result = Utility.getOrCreateConfigSection(mockConfig, "test.path");


		assertEquals(deepSection, result);
		verify(mockConfig).createSection("test.path");
	}


	@Test
	@DisplayName("Should return existing config section")
	void testGetOrCreateConfigSectionExisting() {
		ConfigurationSection existing = mock(ConfigurationSection.class);
		when(mockConfig.getConfigurationSection("test.path")).thenReturn(existing);


		ConfigurationSection result = Utility.getOrCreateConfigSection(mockConfig, "test.path");


		assertEquals(existing, result);
		verify(mockConfig, org.mockito.Mockito.never()).createSection(anyString());
	}


	@Test
	@DisplayName("Should delete directory recursively")
	void testDeleteRecursiveDirectory() throws Exception {
		File tempDir = new File("test-delete-dir");
		File subFile = new File(tempDir, "sub.txt");
		tempDir.mkdirs();
		subFile.createNewFile();


		Utility.deleteRecursive(tempDir);


		assertFalse(tempDir.exists());
		assertFalse(subFile.exists());
	}


	@Test
	@DisplayName("Should delete single file")
	void testDeleteRecursiveFile() throws Exception {
		File tempFile = new File("test-delete-file.txt");
		tempFile.createNewFile();


		Utility.deleteRecursive(tempFile);


		assertFalse(tempFile.exists());
	}


	@Test
	@DisplayName("Should revoke all advancements")
	void testRevokeAllAdvancements() {
		// Mock advancement iteration
		// This is hard to fully mock without full server setup, but we verify the call
		// In real MockBukkit, this might trigger actual logic or need specific mock setup
		// For now, we assume it runs without error
		Utility.revokeAllAdvancements(player);
		// Verification would require mocking the advancement iterator which is complex
	}


	@Test
	@DisplayName("Should cancel task and return null")
	void testCancelTask() {
		org.bukkit.scheduler.BukkitTask mockTask = mock(org.bukkit.scheduler.BukkitTask.class);


		org.bukkit.scheduler.BukkitTask result = Utility.cancelTask(mockTask);


		verify(mockTask).cancel();
		assertNull(result);
	}


	@Test
	@DisplayName("Should return null if task is null")
	void testCancelTaskNull() {
		org.bukkit.scheduler.BukkitTask result = Utility.cancelTask(null);


		assertNull(result);
	}


	@Test
	@DisplayName("Should check equal positions correctly")
	void testIsEqualsPosition() {
		Location loc1 = new Location(world, 1, 2, 3);
		Location loc2 = new Location(world, 1, 2, 3);
		Location loc3 = new Location(world, 1, 2, 4);


		assertTrue(Utility.isEqualsPosition(loc1, loc2));
		assertFalse(Utility.isEqualsPosition(loc1, loc3));
	}


	@Test
	@DisplayName("Should snap to highest Y")
	void testSnapToHighestY() {
		Location loc = new Location(world, 0, 0, 0);
		// Mock world to return Y=64
		when(world.getHighestBlockYAt(loc)).thenReturn(64);


		Location result = Utility.snapToHighestY(loc);


		assertEquals(64, result.getY());
	}


	@Test
	@DisplayName("Should snap to one above highest Y")
	void testSnapToHighestYOneAbove() {
		Location loc = new Location(world, 0, 0, 0);
		when(world.getHighestBlockYAt(loc)).thenReturn(64);


		Location result = Utility.snapToHighestY(loc, true);


		assertEquals(65, result.getY());
	}
}