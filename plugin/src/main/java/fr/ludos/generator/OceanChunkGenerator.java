package fr.ludos.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Utility;

public class OceanChunkGenerator extends ChunkGenerator {
	@Override
	public boolean shouldGenerateNoise() {
		// Return false to disable vanilla terrain noise so we can create a flat ocean floor
		return true;
	}

	@Override
	public boolean shouldGenerateSurface() {
		// Return true to let vanilla handle water placement and surface features,
		// or false if you want to manually place water blocks in generateSurface.
		return true;
	}

	@Override
	public boolean shouldGenerateBedrock() {
		// Let vanilla handle bedrock, or return false to handle it in generateBedrock
		return true;
	}

	@Override
	public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
		int maxY = 150;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y <= maxY; y++) {
					BlockData blockData = chunkData.getBlockData(x, y, z);
					if (blockData.getMaterial().isAir()) {
						chunkData.setBlock(x, y, z, Material.WATER);
					}
				}
			}
		}
	}

	@Override
	public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
		// Return a custom BiomeProvider to ensure the entire world uses only ocean biomes
		return new OceanBiomeProvider();
	}

	// Simple BiomeProvider that returns only ocean biomes
	private static class OceanBiomeProvider extends BiomeProvider {
		@Override
		public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
			return Biome.OCEAN; // Or pick randomly from WATER_BIOMES
		}

		@Override
		public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
			return new ArrayList<>(Utility.waterBiomes);
		}
	}
}