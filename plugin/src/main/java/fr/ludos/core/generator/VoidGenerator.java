package fr.ludos.core.generator;

import org.bukkit.generator.ChunkGenerator;

/**
 * {@link ChunkGenerator} for an empty void World.
 */
public class VoidGenerator extends ChunkGenerator {
	public static VoidGenerator INSTANCE = new VoidGenerator();

	private VoidGenerator() {}

	@Override
	public boolean shouldGenerateNoise() {
		return false;
	}
	@Override
	public boolean shouldGenerateSurface() {
		return false;
	}
	@Override
	public boolean shouldGenerateBedrock() {
		return false;
	}
	@Override
	public boolean shouldGenerateCaves() {
		return false;
	}
	@Override
	public boolean shouldGenerateDecorations() {
		return false;
	}
	@Override
	public boolean shouldGenerateMobs() {
		return false;
	}
	@Override
	public boolean shouldGenerateStructures() {
		return false;
	}
}