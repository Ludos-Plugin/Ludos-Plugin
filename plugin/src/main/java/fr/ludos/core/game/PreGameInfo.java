package fr.ludos.core.game;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.core.Utility;

/**
 * Information about a {@link OfflinePlayer} before a {@link Game} starts, such as their location before the game starts.
 * @param player
 * @param location
 * @param spawnLocation
 * @param gameMode
 */
public record PreGameInfo(OfflinePlayer player, Location location, Location spawnLocation, GameMode gameMode) {
	public PreGameInfo(Player player) {
		this(player, player.getLocation(), player.getBedSpawnLocation(), player.getGameMode());
	}
	public void apply() {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		if (onlinePlayer.isDead()) {
			onlinePlayer.spigot().respawn();
		}

		onlinePlayer.setGameMode(gameMode);

		Utility.resetPlayer(onlinePlayer);

		onlinePlayer.teleport(location, true);
		onlinePlayer.setBedSpawnLocation(spawnLocation, true);
	}
}
