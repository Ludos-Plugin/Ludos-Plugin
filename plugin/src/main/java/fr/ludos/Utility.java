package fr.ludos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;


public class Utility {

	public static Location getGroundedLocationAround(Location searchOrigin, int min, int max, Location fallback) {
		return getGroundedLocationAround(searchOrigin, min, max, fallback, 0);
	}

	public static Location getGroundedLocationAround(Location searchOrigin, int min, int max, Location fallback, int retries) {
		Random rand = new Random();

		Location location = searchOrigin.clone();
		do {
			location.setX(searchOrigin.getBlockX() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			location.setZ(searchOrigin.getBlockZ() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1) + 0.5);
			location.setY(location.getWorld().getHighestBlockYAt(location));

			retries--;
		}
		while (location.getBlock().isLiquid() && retries >= 0);

		if (retries == 0) {
			Bukkit.getServer().broadcast(Component.text("Could not find valid play area"));
			return fallback.clone();
		}

		location.setY(location.getY() + 1);
		return location;
	}

	public static void respawnPlayer(Player player) {
		PacketContainer packet = ProtocolLibrary.getProtocolManager()
			.createPacket(com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND);

		packet.getClientCommands().write(0, EnumWrappers.ClientCommand.PERFORM_RESPAWN);

		ProtocolLibrary.getProtocolManager().receiveClientPacket(player, packet);
	}

	public static void onDeathSpectate(Player player, float spectateSeconds, JavaPlugin plugin) {
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
			}.runTaskLater(plugin, 1);

			new BukkitRunnable() {
				public void run() {
					if (player.getGameMode() == GameMode.SPECTATOR) {
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(player.getBedSpawnLocation());
					}
				}
			}.runTaskLater(plugin, (long)(20 * spectateSeconds));
		}
	}

	// Utility: Character width map for Minecraft's default font
	private static final Map<Character, Integer> MC_CHAR_WIDTH = new HashMap<>() {{
		put(' ', 3);
		put('!', 1);
		put('"', 3);
		put('\'', 1);
		put('(', 3);
		put(')', 3);
		put('*', 3);
		put(',', 1);
		put('.', 1);
		put(':', 1);
		put(';', 1);
		put('<', 4);
		put('>', 4);
		put('@', 6);
		put('I', 3);
		put('[', 3);
		put(']', 3);
		put('`', 2);
		put('f', 4);
		put('i', 1);
		put('k', 4);
		put('l', 2);
		put('t', 3);
		put('{', 3);
		put('|', 1);
		put('}', 3);
		put('~', 6);
	}};

	public static final int MC_BOOK_LINE_WIDTH = 114; // Width of a book line in pixels
	public static final int MC_BOOK_LINE_COUNT = 14; // Number of lines in a book page
	public static final int MC_CHAR_WIDTH_DEFAULT = 5; // Default width for unknown characters

	public static final int getPixelWidth(char c) {
		return getPixelWidth(c, false);
	}
	public static final int getPixelWidth(char c, boolean bold) {
		return MC_CHAR_WIDTH.getOrDefault(c, MC_CHAR_WIDTH_DEFAULT) + (bold ? 1 : 0); // +1 for bold
	}

	// Utility: Calculate pixel width of a string
	public static final int getPixelWidth(String s) {
		return getPixelWidth(s, false);
	}
	public static final int getPixelWidth(String s, boolean bold) {
		int width = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int charWidth = getPixelWidth(c, bold);
			width += charWidth + 1; // +1 for spacing
		}
		return width > 0 ? width - 1 : 0; // Remove last spacing
	}

	// Calculate pixel width of a TextComponent, considering decorations
	public static int getPixelWidth(TextComponent component) {
		return getPixelWidth(component, false);
	}
	public static int getPixelWidth(TextComponent component, boolean parentBold) {
		int width = 0;
		boolean bold = component.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
		String content = component.content();
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			int charWidth = getPixelWidth(c, bold);
			width += charWidth + 1;
		}
		if (width > 0) width -= 1;
		for (Component child : component.children()) {
			if (child instanceof TextComponent tc) {
				width += getPixelWidth(tc, bold);
			}
		}
		return width;
	}


	// Center a TextComponent line, considering decorations (bold, italic)
	public static TextComponent centerBookLine(TextComponent component) {
		return centerBookLine(component, MC_BOOK_LINE_WIDTH);
	}

	public static TextComponent centerBookLine(TextComponent component, int lineWidth) {
		int textWidth = getPixelWidth(component);
		if (textWidth >= lineWidth) return component;

		int spaceWidth = getPixelWidth(' ') + 1;
		int totalPadding = lineWidth - textWidth;
		double padding = totalPadding / 2.0;
		int spaces = (int) (padding / spaceWidth);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			sb.append(' ');
		}
		return Component.text()
				.append(Component.text(sb.toString()))
				.append(component)
			.build();
	}
}