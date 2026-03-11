package fr.ludos.game.alien;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import fr.ludos.Utility;
import fr.ludos.game.GameAreaController;

public class AlienAreaController extends GameAreaController {
	private final AlienLocationOptions locationOption;

	private Location initialLocation;
	private Location center;
	private final int borderSize = 150;

	public AlienAreaController(AlienGame game, AlienLocationOptions locationOption) {
		super(game);
		this.locationOption = locationOption;
	}

	@Override
	protected void onStart() {
		World world = initialLocation.getWorld();
		if (world == null) {
			throw new IllegalStateException("Initial location world is null");
		}

		AlienGame.setCachedBorder(world, getPlugin());

		switch (locationOption) {
			case random:
				center = Utility.getGroundedLocationAround(initialLocation, 150, 400, initialLocation, 5);
				break;
			case here:
			default:
				center = initialLocation.clone();
				break;
		}

		center.setY(world.getHighestBlockYAt(center) + 1);

		WorldBorder border = world.getWorldBorder();
		border.setCenter(center);
		border.setSize(borderSize, 3);
	}

	@Override
	protected void onStop() {
		AlienGame.resetBorder(getPlugin());
	}

	@Override
	public void setup(Location base) {
		this.initialLocation = base.clone();
		this.center = base.clone();
	}

	@Override
	public Location getCenter() {
		return center.clone();
	}

	@Override
	public Location pickRandom(double startFactor, double endFactor) {
		int radius = borderSize / 2;
		return Utility.getGroundedLocationAround(center,
				(int) (radius * startFactor),
				Math.max(1, (int) (radius * endFactor)),
				center);
	}

	public Location pickAlienSpawn(Location around, int distance) {
		Location spawn = Utility.getGroundedLocationAround(around, distance, distance, around, 8);
		return constrain(spawn);
	}

	@Override
	public Location constrain(Location location) {
		World world = center.getWorld();
		WorldBorder border = world.getWorldBorder();
		double halfSize = border.getSize() / 2.0;

		double minX = border.getCenter().getX() - halfSize;
		double maxX = border.getCenter().getX() + halfSize;
		double minZ = border.getCenter().getZ() - halfSize;
		double maxZ = border.getCenter().getZ() + halfSize;

		double x = Math.max(minX, Math.min(maxX, location.getX()));
		double z = Math.max(minZ, Math.min(maxZ, location.getZ()));
		double y = world.getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z)) + 1;

		return new Location(world, x, y, z);
	}

	@Override
	public boolean isInside(Location location) {
		World world = Bukkit.getWorld(center.getWorld().getUID());
		return world != null && world.getWorldBorder().isInside(location);
	}
}