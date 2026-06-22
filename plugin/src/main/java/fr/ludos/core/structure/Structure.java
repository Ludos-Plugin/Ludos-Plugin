package fr.ludos.core.structure;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public abstract class Structure {
	protected final Location location;
	public Structure(Location location) {
		this.location = location;
	}

	public final Location getLocation() {
		return location;
	}

	public abstract Location getEntranceLocation();
	public abstract boolean contains(BoundingBox bb);
	public abstract boolean contains(Location location);
	abstract public void destroy();

	public abstract static class Builder {
		public abstract Structure build(Location location);
	}
}
