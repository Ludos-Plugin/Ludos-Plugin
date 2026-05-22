package fr.ludos.game.areaController.worldborder;

import org.bukkit.Location;
import org.bukkit.WorldBorder;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.GameAreaController;

public class WorldBorderAreaController extends GameAreaController {
	private Location gameLocation;

	private final WorldBorderAreaOption areaOption;
	public final int getAreaDiameter() {
		return areaOption.getSize();
	}
	private final int getAreaRadius() {
		return getAreaDiameter() / 2;
	}


	public WorldBorderAreaController(Game game, WorldBorderAreaOption area) {
		super(game);

		this.areaOption = area;
	}

	@Override
	protected void onStart() {
		super.onStart();
		gameLocation = getGame().getWorldController().getWorld().getSpawnLocation();
		setBorder(gameLocation, getAreaDiameter(), 3);
	}

	@Override
	protected void onStop() {
		gameLocation = null;
		super.onStop();
	}

	public void setBorder(Location center, double size, long time) {
		WorldBorder border = getGame().getWorldController().getWorld().getWorldBorder();

		border.setCenter(center);
		border.setSize(size, time);
	}

	@Override
	public Location pickRandom(double startFactor, double endFactor) {
		int areaRadius = getAreaRadius();
		return Utility.getGroundedLocationAround(gameLocation, (int)(areaRadius * startFactor), (int)(areaRadius * endFactor), gameLocation);
	}
	@Override
	public Location constrain(Location location) {
		WorldBorder border = getGame().getWorldController().getWorld().getWorldBorder();
		double halfSize = border.getSize() / 2.0;

		double minX = border.getCenter().getX() - halfSize;
		double maxX = border.getCenter().getX() + halfSize;
		double x = Math.max(minX, Math.min(maxX, location.getX()));

		double minZ = border.getCenter().getZ() - halfSize;
		double maxZ = border.getCenter().getZ() + halfSize;
		double z = Math.max(minZ, Math.min(maxZ, location.getZ()));

		return new Location(location.getWorld(), x, location.getY(), z);
	}
	@Override
	public Location getCenter() {
		return gameLocation;
	}
	@Override
	public boolean isInside(Location location) {
		return getGame().getWorldController().getWorld().getWorldBorder().isInside(location);
	}
}
