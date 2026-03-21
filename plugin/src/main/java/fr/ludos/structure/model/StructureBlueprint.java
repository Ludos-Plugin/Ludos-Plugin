package fr.ludos.structure.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StructureBlueprint {
	private final String name;
	private final int sizeX;
	private final int sizeY;
	private final int sizeZ;
	private final List<StructureBlock> blocks;

	public StructureBlueprint(String name, int sizeX, int sizeY, int sizeZ, List<StructureBlock> blocks) {
		this.name = name;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.blocks = Collections.unmodifiableList(new ArrayList<>(blocks));
	}

	public String getName() {
		return name;
	}

	public int getSizeX() {
		return sizeX;
	}

	public int getSizeY() {
		return sizeY;
	}

	public int getSizeZ() {
		return sizeZ;
	}

	public List<StructureBlock> getBlocks() {
		return blocks;
	}
}