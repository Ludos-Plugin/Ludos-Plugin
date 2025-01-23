package fr.ludos;

import org.bukkit.*;


import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

public class Utility {

	public static void respawnPlayer(Player player) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager()
			.createPacket(com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND);

		packet.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);

		ProtocolLibrary.getProtocolManager().receiveClientPacket(player, packet);
	}

	public static void onDeathSpectate(Player player, float spectateSeconds) {
		Location deathLocation = player.getLocation().clone();

		try {
			respawnPlayer(player);
		}
		catch (NoClassDefFoundError e) {
			Bukkit.getLogger().warning("ProtocolLib.jar is missing, spectating on Death will not work correctly.");
		}
		finally {
			player.setGameMode(GameMode.SPECTATOR);
			new BukkitRunnable() {
				public void run() { player.teleport(deathLocation); }
			}.runTaskLater(Ludos.getInstance(), 1);

			new BukkitRunnable() {
				public void run() {
					if (player.getGameMode() == GameMode.SPECTATOR) {
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(player.getBedSpawnLocation());
					}
				}
			}.runTaskLater(Ludos.getInstance(), (long)(20 * spectateSeconds));
		}
	}
}