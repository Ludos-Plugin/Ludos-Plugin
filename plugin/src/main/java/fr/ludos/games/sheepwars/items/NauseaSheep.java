package fr.ludos.games.sheepwars.items;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Nausea Sheep - Applies inverted controls to nearby players.
 */
public class NauseaSheep extends AbstractSheep {
	// Map of players with inverted controls (UUID -> end timestamp)
	private static final HashMap<UUID, Long> INVERTED_PLAYERS = new HashMap<>();

	public NauseaSheep(String name, String description, Material material) {
		super(name, description, material);
	}

	@Override
	public void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
		Sheep sheep = spawnAndLaunchSheep(launcher, event, item, DyeColor.LIME);

		startSheepTimer(sheep, () -> {
			applyInvertedControls(sheep.getLocation());
		}, launcher);
	}

	/**
	 * Applies inverted controls to nearby players.
	 *
	 * @param center The center location
	 */
	private void applyInvertedControls(Location center) {
		double radius = 8.0;
		long duration = 8000; // 8 seconds effect
		long endTime = System.currentTimeMillis() + duration;

		List<Player> nearbyPlayers = center.getWorld().getPlayers().stream()
			.filter(p -> p.getLocation().distance(center) <= radius)
			.toList();

		for (Player player : nearbyPlayers) {
			INVERTED_PLAYERS.put(player.getUniqueId(), endTime);
			// CONFUSION effect (nausea) to distort the screen - level 1, 160 ticks = 8 seconds
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 1, false, true));
			player.sendMessage(Component.text("Vos contrôles sont inversés!", NamedTextColor.RED));
			player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 1, 0), 30);
		}

		center.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.5f);
	}

	/**
	 * Clears inverted controls for a specific player.
	 * @param playerId The UUID of the player
	 */
	public static void clearInvertedPlayer(UUID playerId) {
		INVERTED_PLAYERS.remove(playerId);
	}

	/**
	 * Handles inverted controls movement for affected players.
	 * @param event
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		if (!INVERTED_PLAYERS.containsKey(playerId)) return;

		// Check if the effect has expired
		if (System.currentTimeMillis() > INVERTED_PLAYERS.get(playerId)) {
			INVERTED_PLAYERS.remove(playerId);
			player.sendMessage(Component.text("Vos contrôles sont revenus à la normale!", NamedTextColor.GREEN));
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		if (to == null) return;

		// Calculate movement
		double deltaX = to.getX() - from.getX();
		double deltaZ = to.getZ() - from.getZ();

		// If player moves, invert the movement
		if (deltaX != 0 || deltaZ != 0) {
			Location inversedLoc = from.clone();
			inversedLoc.subtract(deltaX * 2, 0, deltaZ * 2);
			inversedLoc.setY(to.getY());
			inversedLoc.setYaw(to.getYaw());
			inversedLoc.setPitch(to.getPitch());

			event.setTo(inversedLoc);
		}
	}
}
