package fr.ludos.structure;

import org.bukkit.Location;

public abstract class Structure {
	protected final Location location;
	public Structure(Location location) {
		this.location = location;
	}

	public final Location getLocation() {
		return location;
	}

	public abstract Location getEntranceLocation();
	abstract public void destroy();

	public abstract static class Builder {
		public abstract Structure build(Location location);
	}
}
