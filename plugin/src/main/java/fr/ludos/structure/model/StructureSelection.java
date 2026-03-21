package fr.ludos.structure.model;

import org.bukkit.Location;

public final class StructureSelection {
	private final Location pos1;
	private final Location pos2;

	public StructureSelection(Location pos1, Location pos2) {
		this.pos1 = pos1;
		this.pos2 = pos2;
	}

	public Location getPos1() {
		return pos1;
	}

	public Location getPos2() {
		return pos2;
	}

	public boolean isComplete() {
		return pos1 != null && pos2 != null;
	}
}