package fr.ludos.core.structure;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public abstract class BoundingBoxStructure extends Structure {

	public BoundingBoxStructure(Location location) {
		super(location);
	}

	public abstract BoundingBox getBoundingBox();

	@Override
	public boolean contains(Location location) {
		return getBoundingBox().contains(location.toVector());
	}
	@Override
	public boolean contains(BoundingBox bb) {
		return getBoundingBox().contains(bb);
	}
}
