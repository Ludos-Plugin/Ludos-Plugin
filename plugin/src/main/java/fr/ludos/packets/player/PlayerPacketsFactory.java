package fr.ludos.packets.player;

import org.bukkit.Bukkit;

public final class PlayerPacketsFactory {
	public static PlayerPackets createHandler() {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		switch (version) {
			case "v1_19_R1", "v1_19_R2", "v1_19_R3":
				return new PlayerPackets_1_19();
			default:
				throw new UnsupportedOperationException("Version " + version + " is not supported.");
		}
	}
}