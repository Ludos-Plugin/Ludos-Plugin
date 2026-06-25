package fr.ludos.core.packets.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class PlayerPacketsFactory {
	public static PlayerPackets createHandler() {
		String versionName = Bukkit.getServer().getClass().getPackage().getName();
		try {
			String version = versionName.split("\\.")[3];
			switch (version) {
				case "v1_19_R1", "v1_19_R2", "v1_19_R3":
					return new PlayerPackets_1_19();
				default:
					throw new UnsupportedOperationException("Version " + version + " is not supported.");
			}
		} catch (Exception e) {
			return new NullPlayerPackets(versionName);
		}
	}

	private static class NullPlayerPackets implements PlayerPackets {
		private final String versionName;

		public NullPlayerPackets(String versionName) {
			this.versionName = versionName;
		}

		@Override
		public void setGlowForPlayer(Entity target, Player viewer, boolean value) {
			throw new UnsupportedOperationException("Glow cannot be used. Version " + versionName + " cannot be parsed");
		}
	}
}