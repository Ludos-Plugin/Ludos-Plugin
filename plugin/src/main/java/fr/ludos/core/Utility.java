package fr.ludos.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType.Category;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

/**
 * Various utility functions for Ludos.
 */
public class Utility {
	private static final Random RANDOM = new Random();

	public static Location getLocationAround(Location searchOrigin, int min, int max, Location fallback) {
		return getLocationAround(searchOrigin, min, max, fallback, 0);
	}

	public static HashSet<Biome> allBiomes = new HashSet<Biome>(){{
		addAll(Arrays.stream(Biome.values()).collect(Collectors.toSet()));
	}};

	public static HashSet<Biome> netherBiomes = new HashSet<Biome>(){{
		add(Biome.NETHER_WASTES); add(Biome.SOUL_SAND_VALLEY); add(Biome.CRIMSON_FOREST); add(Biome.WARPED_FOREST); add(Biome.BASALT_DELTAS);
	}};
	public static HashSet<Biome> endBiomes = new HashSet<Biome>(){{
		add(Biome.SMALL_END_ISLANDS); add(Biome.END_MIDLANDS); add(Biome.END_HIGHLANDS); add(Biome.END_BARRENS);
	}};
	public static HashSet<Biome> waterBiomes = new HashSet<Biome>(){{
		add(Biome.OCEAN); add(Biome.WARM_OCEAN); add(Biome.LUKEWARM_OCEAN); add(Biome.COLD_OCEAN); add(Biome.FROZEN_OCEAN);
		add(Biome.DEEP_OCEAN); add(Biome.DEEP_LUKEWARM_OCEAN); add(Biome.DEEP_COLD_OCEAN); add(Biome.DEEP_FROZEN_OCEAN);
		add(Biome.RIVER); add(Biome.FROZEN_RIVER);
	}};
	public static HashSet<Biome> forbiddenOverworldBiomes = new HashSet<Biome>(){{
		add(Biome.THE_VOID);
		addAll(waterBiomes);
		addAll(netherBiomes); addAll(endBiomes);
	}};

	public static HashSet<Biome> getOverworldBiomes() {
		@SuppressWarnings("unchecked")
		HashSet<Biome> biomes = (HashSet<Biome>) allBiomes.clone();
		biomes.removeAll(forbiddenOverworldBiomes);

		return biomes;
	}

	public static HashSet<Biome> getNetherBiomes() {
		@SuppressWarnings("unchecked")
		HashSet<Biome> biomes = (HashSet<Biome>) netherBiomes.clone();

		return biomes;
	}

	public static HashSet<Biome> getEndBiomes() {
		@SuppressWarnings("unchecked")
		HashSet<Biome> biomes = (HashSet<Biome>) endBiomes.clone();

		return biomes;
	}

	@SuppressWarnings("unchecked")
	public static Location getRandomBiomeLocation(Location searchOrigin, int biomeSearchSize, int min, int max, Location fallback, int retries, Set<Biome> avoidBiomes) {
		World world = searchOrigin.getWorld();

		Set<Biome> biomes;

		switch (world.getEnvironment()) {
			case NORMAL -> biomes = getOverworldBiomes();
			case NETHER -> biomes = getNetherBiomes();
			case THE_END -> biomes = getEndBiomes();
			default -> biomes = (HashSet<Biome>) allBiomes.clone();
		}

		if (avoidBiomes != null) {
			biomes.removeAll(avoidBiomes);
		}

		Location biomeLocation;
		int biomeSearchRetries = retries;

		do {
			Biome randomBiome = biomes.stream().skip(RANDOM.nextInt(biomes.size())).findFirst().orElse(null);
			biomes.remove(randomBiome);

			biomeLocation = world.locateNearestBiome(searchOrigin, randomBiome, biomeSearchSize, 16);
			biomeSearchRetries--;
		}
		while (biomeLocation == null && biomeSearchRetries > 0);

		if (biomeLocation == null) return fallback.clone();

		return getLocationAround(biomeLocation, min, max, fallback, retries);
	}

	public static Location getLocationAround(Location searchOrigin, int min, int max, Location fallback, int retries) {
		Location location = searchOrigin.clone();
		do {
			location.setX(searchOrigin.getBlockX() + RANDOM.nextInt(min, max + 1) * (RANDOM.nextBoolean() ? 1 : -1) + 0.5);
			location.setZ(searchOrigin.getBlockZ() + RANDOM.nextInt(min, max + 1) * (RANDOM.nextBoolean() ? 1 : -1) + 0.5);

			retries--;
		}
		while (location.getBlock().isLiquid() && retries >= 0);

		if (retries == 0) {
			Bukkit.getLogger().warning("Could not find valid play area");
			return fallback.clone();
		}

		return location;
	}


	public static void resetPlayerState(Player player) {
		player.getActivePotionEffects()
			.forEach(effect -> {
				if (effect.getType().getEffectCategory() == Category.HARMFUL) {
					player.removePotionEffect(effect.getType());
				}
			});
		player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		player.setFoodLevel(20);
		player.setSaturation(20f);
		player.setFireTicks(0);

		player.setVelocity(new Vector(0, 0, 0));

		player.clearTitle();
	}

	public static void resetPlayer(Player player) {
		player.getInventory().clear();
		revokeAllAdvancements(player);

		resetPlayerState(player);
	}

	public static void onDeathSpectate(PlayerDeathEvent event, Float spectateSeconds, JavaPlugin plugin, Runnable onFinish) {
		Player player = event.getPlayer();
		Location deathLocation = player.getLocation().clone();

		event.setCancelled(true);
		player.setGameMode(GameMode.SPECTATOR);

		Bukkit.broadcast(event.deathMessage());

		for (ItemStack item : event.getDrops()) {
			player.getWorld().dropItemNaturally(deathLocation, item);
		}
		if (! event.getKeepInventory()) {
			player.getInventory().clear();
		}

		if (! event.getKeepLevel()) {
			int xp = event.getDroppedExp();
			if (xp > 0) {
				ExperienceOrb xpOrb = player.getWorld().spawn(deathLocation, ExperienceOrb.class);
				xpOrb.setExperience(xp);
			}
		}
		player.setLevel(event.getNewExp());
		player.setExp(event.getNewLevel());
		player.setTotalExperience(event.getNewTotalExp());


		new BukkitRunnable() {
			public void run() { player.teleport(deathLocation); }
		}.runTaskLater(plugin, 1);

		if (spectateSeconds != null) {
			new BukkitRunnable() {
				public void run() {
					if (player.getGameMode() == GameMode.SPECTATOR) {
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(getPlayerSpawnLocation(player));
						if (onFinish != null) {
							onFinish.run();
						}
					}
				}
			}.runTaskLater(plugin, (long)(20 * spectateSeconds));
		}
	}
	public static void onDeathSpectate(PlayerDeathEvent event, JavaPlugin plugin) {
		onDeathSpectate(event, null, plugin, null);
	}
	public static void onDeathSpectate(PlayerDeathEvent event, Float spectateSeconds, JavaPlugin plugin) {
		onDeathSpectate(event, spectateSeconds, plugin, null);
	}

	public static Location getPlayerSpawnLocation(Player player) {
		Location bedLocation = player.getBedSpawnLocation();
		if (bedLocation != null) return bedLocation;

		return player.getWorld().getSpawnLocation();
	}

	@Nullable
	public static Location getOfflinePlayerSpawnLocation(OfflinePlayer player) {
		Location bedLocation = player.getBedSpawnLocation();
		if (bedLocation != null) return bedLocation;

		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			return onlinePlayer.getWorld().getSpawnLocation();
		}

		try {
			World defaultWorld = Bukkit.getServer().getWorlds().get(0);
			return defaultWorld.getSpawnLocation();
		} catch (Exception e) {
			return null;
		}
	}

	public static Vector getVectorFacing(double x, double y, double z, BlockFace face) {
		return switch (face) {
			case EAST -> new Vector(-z, y, -x);
			case WEST -> new Vector(z, y, x);
			case UP -> new Vector(x, -z, y);
			case DOWN -> new Vector(x, z, y);
			case SOUTH -> new Vector(x, y, -z);
			case NORTH -> new Vector(-x, y, z);
			default -> new Vector(x, y, z);
		};
	}
	public static Vector getVectorFacing(Vector vector, BlockFace face) {
		return getVectorFacing(vector.getX(), vector.getY(), vector.getZ(), face);
	}

	public static Iterable<Block> getAllBlocks(Block block, BlockFace face, Pair<Integer, Integer> boundsX, Pair<Integer, Integer> boundsY, Pair<Integer, Integer> boundsZ) {
		if (boundsX.getLeft() > boundsX.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsX);
		if (boundsY.getLeft() > boundsY.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsY);
		if (boundsZ.getLeft() > boundsZ.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsZ);

		return IntStream.rangeClosed(boundsZ.getLeft(), boundsZ.getRight())
			.boxed()
			.flatMap(zOffset ->
				IntStream.rangeClosed(boundsX.getLeft(), boundsX.getRight())
					.boxed()
					.flatMap(xOffset ->
						IntStream.rangeClosed(boundsY.getLeft(), boundsY.getRight())
							.mapToObj(yOffset -> {
								Vector vector = getVectorFacing(xOffset, yOffset, zOffset, face);
								return block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
							})
					)
			)
			.toList();
	}
	public static List<List<Block>> getAllBlockColumns(Block block, BlockFace face, Pair<Integer, Integer> boundsX, Pair<Integer, Integer> boundsY, Pair<Integer, Integer> boundsZ) {
		if (boundsX.getLeft() > boundsX.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsX);
		if (boundsY.getLeft() > boundsY.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsY);
		if (boundsZ.getLeft() > boundsZ.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsZ);

		return IntStream.rangeClosed(boundsX.getLeft(), boundsX.getRight())
			.boxed()
			.flatMap(xOffset ->
				IntStream.rangeClosed(boundsZ.getLeft(), boundsZ.getRight())
					.mapToObj(zOffset ->
						IntStream.rangeClosed(boundsY.getLeft(), boundsY.getRight())
							.mapToObj(yOffset -> {
								Vector vector = getVectorFacing(xOffset, yOffset, zOffset, face);
								return block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
							})
							.toList()
					)
			)
			.toList();
	}
	public static List<List<Block>> getAllBlockRows(Block block, BlockFace face, Pair<Integer, Integer> boundsX, Pair<Integer, Integer> boundsY, Pair<Integer, Integer> boundsZ) {
		if (boundsX.getLeft() > boundsX.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsX);
		if (boundsY.getLeft() > boundsY.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsY);
		if (boundsZ.getLeft() > boundsZ.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsZ);

		return IntStream.rangeClosed(boundsY.getLeft(), boundsY.getRight())
			.boxed()
			.flatMap(yOffset ->
				IntStream.rangeClosed(boundsZ.getLeft(), boundsZ.getRight())
					.mapToObj(zOffset ->
						IntStream.rangeClosed(boundsX.getLeft(), boundsX.getRight())
							.mapToObj(xOffset -> {
								Vector vector = getVectorFacing(xOffset, yOffset, zOffset, face);
								return block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
							})
							.toList()
					)
			)
			.toList();
	}
	public static List<List<Block>> getAllBlockLayers(Block block, BlockFace face, Pair<Integer, Integer> boundsX, Pair<Integer, Integer> boundsY, Pair<Integer, Integer> boundsZ) {
		if (boundsX.getLeft() > boundsX.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsX);
		if (boundsY.getLeft() > boundsY.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsY);
		if (boundsZ.getLeft() > boundsZ.getRight()) throw new IllegalArgumentException("Invalid bounds: " + boundsZ);

		return IntStream.rangeClosed(boundsY.getLeft(), boundsY.getRight())
			.boxed()
			.flatMap(yOffset ->
				IntStream.rangeClosed(boundsX.getLeft(), boundsX.getRight())
					.mapToObj(xOffset ->
						IntStream.rangeClosed(boundsZ.getLeft(), boundsZ.getRight())
							.mapToObj(zOffset -> {
								Vector vector = getVectorFacing(xOffset, yOffset, zOffset, face);
								return block.getRelative(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
							})
							.toList()
					)
			)
			.toList();
	}

	public static void deleteRecursive(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				deleteRecursive(subFile);
			}
		}
		file.delete();
	}

	public static void revokeAllAdvancements(Player player) {
		Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();

		for (Advancement advancement = iterator.next(); iterator.hasNext(); advancement = iterator.next()) {
			AdvancementProgress progress = player.getAdvancementProgress(advancement);

			for (String criteria : progress.getAwardedCriteria())
				progress.revokeCriteria(criteria);
		}
	}

	@Nullable
	public static BukkitTask cancelTask(@Nullable BukkitTask task) {
		if (task == null) return null;

		task.cancel();
		return task = null;
	}

	public static boolean isEqualsPosition(Location loc1, Location loc2) {
		return loc1.getX() == loc2.getX() && loc1.getY() == loc2.getY() && loc1.getZ() == loc2.getZ();
	}

	public static Location snapToHighestY(Location location) {
		return snapToHighestY(location, false);
	}
	public static Location snapToHighestY(Location location, boolean oneAbove) {
		location.setY(location.getWorld().getHighestBlockYAt(location));
		if (oneAbove) {
			location.add(0, 1, 0);
		}
		return location;
	}

	public static <T> List<? extends Collection<T>> split(Collection<T> items, int size) {
		if (size <= 0) return List.of();

		List<List<T>> result = new ArrayList<>(size);

		for (int i = 0; i < size; i++) result.add(new ArrayList<>());

		int idx = 0;
		for (T item : items) {
			result.get(idx).add(item);
			idx = (idx + 1) % size;
		}

		return result;
	}

	public static Stream<Player> getOnline(@Nullable Collection<OfflinePlayer> offline) {
		if (offline == null) return Stream.of();
		return getOnline(offline.stream());
	}
	public static Stream<Player> getOnline(Stream<OfflinePlayer> offline) {
		return offline
			.map(OfflinePlayer::getPlayer)
			.filter(Objects::nonNull);
	}


	public final static Predicate<Player> IS_PLAYER_IN_NORMAL_GAMEMODE = (p) -> p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE;
	public final static Predicate<LivingEntity> IS_ENTITY_ALIVE = (p) -> ! p.isDead() && (p instanceof Player player ? IS_PLAYER_IN_NORMAL_GAMEMODE.test(player) : true);
	public final static Predicate<Player> IS_PLAYER_ALIVE = (p) -> IS_PLAYER_IN_NORMAL_GAMEMODE.test(p) && ! p.isDead();

	public static Stream<Entity> getTeamEntities(Team team) {
		return team.getEntries().stream()
			.map(e -> {
				try {
					UUID uuid = UUID.fromString(e);
					return Bukkit.getEntity(uuid);
				} catch (IllegalArgumentException err) {
					return Bukkit.getPlayer(e);
				}
			});
	}
	public static Stream<OfflinePlayer> getTeamPlayers(Team team) {
		return team.getEntries().stream()
			.map(Bukkit::getPlayer);
	}
	public static Stream<Player> getTeamOnlinePlayers(Team team) {
		return getOnline(getTeamPlayers(team));
	}
	public static Stream<Player> getTeamAlivePlayers(Team team) {
		return getTeamOnlinePlayers(team).filter(IS_PLAYER_ALIVE);
	}


	public final static ConfigurationSection getOrCreateConfigSection(ConfigurationSection config, String path) {
		ConfigurationSection deeper = config.getConfigurationSection(path);
		if (deeper != null) return deeper;
		return config.createSection(path);
	}
}