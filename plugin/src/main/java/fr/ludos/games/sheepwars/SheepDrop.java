package fr.ludos.games.sheepwars;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.games.sheepwars.items.AbstractSheep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Manages wool drops for players in Sheepwars.
 * Every 15 seconds, a random wool is given to each player based on drop rates.
 * A BossBar displays the countdown timer.
 */
public class SheepDrop {

	// Drop rates for each wool type (percentages, must total 100)
	private static final Map<Material, Double> DROP_RATES = new LinkedHashMap<>();

	static {
		// Common (50% total)
		DROP_RATES.put(Material.ORANGE_WOOL, 12.0);         // Explosion Sheep
		DROP_RATES.put(Material.BLUE_WOOL, 12.0);           // Water Sheep
		DROP_RATES.put(Material.LIGHT_BLUE_WOOL, 13.0);     // Freeze Sheep
		DROP_RATES.put(Material.BLACK_WOOL, 13.0);          // Blind Sheep

		// Uncommon (30% total)
		DROP_RATES.put(Material.RED_WOOL, 7.5);             // Large Explosion Sheep
		DROP_RATES.put(Material.YELLOW_WOOL, 7.5);          // Lava Sheep
		DROP_RATES.put(Material.MAGENTA_WOOL, 7.5);         // Poison Sheep
		DROP_RATES.put(Material.PINK_WOOL, 7.5);            // Heal Sheep

		// Rare (15% total)
		DROP_RATES.put(Material.LIGHT_GRAY_WOOL, 3.0);      // Monster Sheep
		DROP_RATES.put(Material.WHITE_WOOL, 3.0);           // All Aboard Sheep
		DROP_RATES.put(Material.GRAY_WOOL, 3.0);            // Anvil Sheep
		DROP_RATES.put(Material.CYAN_WOOL, 3.0);            // Fireball Sheep
		DROP_RATES.put(Material.LIME_WOOL, 3.0);            // Nausea Sheep

		// Legendary (5% total)
		DROP_RATES.put(Material.PURPLE_WOOL, 2.25);         // Teleport Sheep
		DROP_RATES.put(Material.GREEN_WOOL, 2.25);          // Grass Sheep
		DROP_RATES.put(Material.BROWN_WOOL, 0.5);           // Nuke Sheep
	}

	private final SheepwarsGame game;
	private final List<AbstractSheep> sheepList;
	private final int dropIntervalSeconds;

	private BukkitTask timerTask;
	private BossBar bossBar;
	private int currentTick;
	private final int totalTicks;

	/**
	 * Creates a new SheepDrop manager.
	 * @param game
	 * @param sheepList The list of available sheep types
	 * @param dropIntervalSeconds The interval in seconds between drops (default 15)
	 */
	public SheepDrop(SheepwarsGame game, List<AbstractSheep> sheepList, int dropIntervalSeconds) {
		this.game = game;
		this.sheepList = sheepList;
		this.dropIntervalSeconds = dropIntervalSeconds;
		this.totalTicks = dropIntervalSeconds * 20; // Convert to ticks (20 ticks = 1 second)
		this.currentTick = 0;
	}

	/**
	 * Creates a new SheepDrop manager with default 15 second interval.
	 * @param game
	 * @param sheepList The list of available sheep types
	 */
	public SheepDrop(SheepwarsGame game, List<AbstractSheep> sheepList) {
		this(game, sheepList, 15);
	}

	/**
	 * Starts the wool drop timer.
	 */
	public void start() {
		// Create the BossBar
		bossBar = Bukkit.createBossBar(
			formatTimeDisplay(dropIntervalSeconds),
			BarColor.GREEN,
			BarStyle.SEGMENTED_10
		);
		bossBar.setProgress(1.0);

		// Add all players to the BossBar
		for (Player player : game.teamController().getOnlinePlayers()) {
			bossBar.addPlayer(player);
		}

		currentTick = totalTicks;

		// Start the timer task
		timerTask = new BukkitRunnable() {
			@Override
			public void run() {
				currentTick--;

				// Update BossBar progress
				double progress = (double) currentTick / (double) totalTicks;
				bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));

				// Update time display
				int remainingSeconds = currentTick / 20;
				bossBar.setTitle(formatTimeDisplay(remainingSeconds));

				// Update bar color based on time remaining
				if (remainingSeconds <= 3) {
					bossBar.setColor(BarColor.RED);
				} else if (remainingSeconds <= 7) {
					bossBar.setColor(BarColor.YELLOW);
				} else {
					bossBar.setColor(BarColor.GREEN);
				}

				// When timer reaches 0, give wool to all players and reset
				if (currentTick <= 0) {
					giveRandomWoolToAllPlayers();
					currentTick = totalTicks;
					bossBar.setProgress(1.0);
					bossBar.setColor(BarColor.GREEN);
				}
			}
		}.runTaskTimer(game.ludos(), 0L, 1L);
	}

	/**
	 * Stops the wool drop timer.
	 */
	public void stop() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

		if (bossBar != null) {
			bossBar.removeAll();
			bossBar = null;
		}
	}

	/**
	 * Formats the time display as "⏱ MM:SS".
	 * @param seconds The total seconds to format and display.
	 * @return The formatted Time string.
	 */
	private String formatTimeDisplay(int seconds) {
		int minutes = seconds / 60;
		int secs = seconds % 60;
		return String.format("§a⏱ §f%02d:%02d §7- §eNext Wool", minutes, secs);
	}

	/**
	 * Gives a random wool to all players based on drop rates.
	 */
	private void giveRandomWoolToAllPlayers() {
		for (Player player : game.teamController().getAlivePlayers()) {
			if (player.isOnline()) {
				giveRandomWool(player);
			}
		}
	}

	public void giveRandomWool(Player player) {
		Material selectedMaterial = getRandomWoolMaterial();

		// Find the corresponding AbstractSheep in the list
		AbstractSheep selectedAbstractSheep = null;
		for (AbstractSheep sheep : sheepList) {
			if (sheep.getMaterial() == selectedMaterial) {
				selectedAbstractSheep = sheep;
				break;
			}
		}

		if (selectedAbstractSheep != null) {
			ItemStack woolItem = selectedAbstractSheep.createSheepItem(1);
			player.getInventory().addItem(woolItem);

			// Notify the player
			player.sendMessage(Component.text("+ 1 ", NamedTextColor.GREEN)
				.append(Component.text(selectedAbstractSheep.getName(), getRarityColor(selectedMaterial))));
		}
	}

	/**
	 * Gets a random wool material based on drop rates.
	 * @return The {@link Material} used as a key for the Sheep.
	 */
	public static Material getRandomWoolMaterial() {
		double random = Math.random() * 100;
		double cumulative = 0.0;

		for (Map.Entry<Material, Double> entry : DROP_RATES.entrySet()) {
			cumulative += entry.getValue();
			if (random <= cumulative) {
				return entry.getKey();
			}
		}

		// Fallback (should never happen if rates total 100)
		return Material.WHITE_WOOL;
	}

	/**
	 * Gets the drop rate for a specific material.
	 * @param material The material to get the droprate for.
	 * @return The droprate of the {@link Material} used as a key for the Sheep.
	 */
	public static double getDropRate(Material material) {
		return DROP_RATES.getOrDefault(material, 0.0);
	}

	/**
	 * Gets the rarity color for display.
	 * @param material The material for which to get the rarity color.
	 * @return The {@link NamedTextColor} of the rarity of the Sheep material.
	 */
	public static NamedTextColor getRarityColor(Material material) {
		double rate = getDropRate(material);

		if (rate >= 10) {
			return NamedTextColor.WHITE;        // Common
		} else if (rate >= 5) {
			return NamedTextColor.GREEN;        // Uncommon
		} else if (rate >= 2) {
			return NamedTextColor.BLUE;         // Rare
		} else {
			return NamedTextColor.GOLD;         // Legendary
		}
	}

	/**
	 * Gets the rarity name for a material.
	 * @param material The material for which to get the rarity name.
	 * @return The name of the rarity of the Sheep material.
	 */
	public static String getRarityName(Material material) {
		double rate = getDropRate(material);

		if (rate >= 10) {
			return "Common";
		} else if (rate >= 5) {
			return "Uncommon";
		} else if (rate >= 2) {
			return "Rare";
		} else {
			return "Legendary";
		}
	}

	/**
	 * Gets the drop rates map.
	 * @return The droprate of all the {@link Material}s used as keys for the Sheep.
	 */
	public static Map<Material, Double> getDropRates() {
		return Collections.unmodifiableMap(DROP_RATES);
	}

	/**
	 * Adds a player to the BossBar (useful when a player joins mid-game).
	 * @param player The {@link Player} which will have the Countdown display added.
	 */
	public void addPlayer(Player player) {
		if (bossBar != null) {
			bossBar.addPlayer(player);
		}
	}

	/**
	 * Removes a player from the BossBar.
	 * @param player The {@link Player} which will have the Countdown display removed.
	 */
	public void removePlayer(Player player) {
		if (bossBar != null) {
			bossBar.removePlayer(player);
		}
	}
}
