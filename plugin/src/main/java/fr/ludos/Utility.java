package fr.ludos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Biome;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;

public class Utility {

	public static Location getGroundedLocationAround(Location searchOrigin, int min, int max, Location fallback) {
		return getGroundedLocationAround(searchOrigin, min, max, fallback, 0);
	}

	private static HashSet<Biome> allBiomes = new HashSet<Biome>(){{
		addAll(Arrays.stream(Biome.values()).collect(Collectors.toSet()));
	}};

	private static HashSet<Biome> netherBiomes = new HashSet<Biome>(){{
		add(Biome.NETHER_WASTES); add(Biome.SOUL_SAND_VALLEY); add(Biome.CRIMSON_FOREST); add(Biome.WARPED_FOREST); add(Biome.BASALT_DELTAS);
	}};
	private static HashSet<Biome> endBiomes = new HashSet<Biome>(){{
		add(Biome.SMALL_END_ISLANDS); add(Biome.END_MIDLANDS); add(Biome.END_HIGHLANDS); add(Biome.END_BARRENS);
	}};
	private static HashSet<Biome> forbiddenOverworldBiomes = new HashSet<Biome>(){{
		add(Biome.OCEAN); add(Biome.DEEP_OCEAN); add(Biome.FROZEN_OCEAN); add(Biome.DRIPSTONE_CAVES); add(Biome.LUSH_CAVES);
		add(Biome.RIVER); add(Biome.FROZEN_RIVER); addAll(netherBiomes); addAll(endBiomes);
	}};

	private static HashSet<Biome> getOverworldBiomes() {
		HashSet<Biome> biomes = (HashSet<Biome>) allBiomes.clone();
		biomes.removeAll(forbiddenOverworldBiomes);
		return biomes;
	}

	private static HashSet<Biome> getNetherBiomes() {
		return (HashSet<Biome>) netherBiomes.clone();
	}

	private static HashSet<Biome> getEndBiomes() {
		return (HashSet<Biome>) endBiomes.clone();
	}

	public static Location getRandomBiomeLocation(Location searchOrigin, int biomeSearchSize, int min, int max, Location fallback, int retries, Set<Biome> avoidBiomes) {
		Random rand = new Random();

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
			Biome randomBiome = biomes.stream().skip(rand.nextInt(biomes.size())).findFirst().orElse(null);
			biomes.remove(randomBiome);
			Bukkit.broadcastMessage(randomBiome.toString());

			biomeLocation = world.locateNearestBiome(searchOrigin, randomBiome, biomeSearchSize, 16);
			biomeSearchRetries--;
		}
		while (biomeLocation == null && biomeSearchRetries > 0);

		if (biomeLocation == null) return fallback.clone();


		Location searchLocation = biomeLocation.clone();

		do {
			searchLocation.setX(biomeLocation.getBlockX() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			searchLocation.setZ(biomeLocation.getBlockZ() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			searchLocation.setY(searchLocation.getWorld().getHighestBlockYAt(searchLocation));

			retries--;
		}
		while (searchLocation.getBlock().isLiquid() && retries >= 0);

		if (retries == 0) {
			Bukkit.getServer().broadcast(Component.text("Could not find valid play area"));
			return fallback.clone();
		}

		searchLocation.setY(searchLocation.getY() + 1);
		return searchLocation;
	}

	public static Location getGroundedLocationAround(Location searchOrigin, int min, int max, Location fallback, int retries) {
		Random rand = new Random();

		Location location = searchOrigin.clone();
		do {
			location.setX(searchOrigin.getBlockX() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			location.setZ(searchOrigin.getBlockZ() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			location.setY(location.getWorld().getHighestBlockYAt(location));

			retries--;
		}
		while (location.getBlock().isLiquid() && retries >= 0);

		if (retries == 0) {
			Bukkit.getServer().broadcast(Component.text("Could not find valid play area"));
			return fallback.clone();
		}

		location.setY(location.getY() + 1);
		return location;
	}

	public static void respawnPlayer(Player player) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager()
			.createPacket(com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND);

		packet.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);

		ProtocolLibrary.getProtocolManager().receiveClientPacket(player, packet);
	}

	public static void onDeathSpectate(Player player, float spectateSeconds, JavaPlugin plugin) {
		Location deathLocation = player.getLocation().clone();

		try {
			respawnPlayer(player);
		}
		catch (NoClassDefFoundError e) {
			Bukkit.getLogger().warning("ProtocolLib.jar is missing, spectating on Death will not work correctly.");
		}
		finally {
			player.setGameMode(GameMode.SPECTATOR);
			new BukkitRunnable() {
				public void run() { player.teleport(deathLocation); }
			}.runTaskLater(plugin, 1);

			new BukkitRunnable() {
				public void run() {
					if (player.getGameMode() == GameMode.SPECTATOR) {
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(player.getBedSpawnLocation());
					}
				}
			}.runTaskLater(plugin, (long)(20 * spectateSeconds));
		}
	}

	public static void revokeAllAdvancements(Player player) {
		Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();

		for (Advancement advancement = iterator.next(); iterator.hasNext(); advancement = iterator.next()) {
			AdvancementProgress progress = player.getAdvancementProgress(advancement);

			for (String criteria : progress.getAwardedCriteria())
				progress.revokeCriteria(criteria);
		}
	}
}