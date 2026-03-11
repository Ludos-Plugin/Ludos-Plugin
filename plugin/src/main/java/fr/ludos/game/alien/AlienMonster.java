package fr.ludos.game.alien;

import java.util.Comparator;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class AlienMonster implements Listener {
	private static final double VISION_RANGE = 20.0;
	private static final double VISION_ANGLE = Math.toRadians(120.0);
	private static final double HEARING_RANGE = 30.0;
	private static final double HEARING_VELOCITY_THRESHOLD = 0.1;
	private static final double KILL_RANGE = 2.0;

	private final AlienGame game;
	private final LivingEntity alienEntity;

	private BukkitTask task;
	private Player currentTarget;

	private AlienMonster(AlienGame game, LivingEntity alienEntity) {
		this.game = game;
		this.alienEntity = alienEntity;

		this.alienEntity.customName(net.kyori.adventure.text.Component.text("Alien"));
		this.alienEntity.setCustomNameVisible(true);
		this.alienEntity.setRemoveWhenFarAway(false);
		this.alienEntity.setInvulnerable(true);
		this.alienEntity.setSilent(false);

		if (this.alienEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
			this.alienEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.35);
		}

		Bukkit.getPluginManager().registerEvents(this, game.getPlugin());
	}

	public static AlienMonster spawn(AlienGame game, Location location) {
		World world = location.getWorld();
		if (world == null) {
			throw new IllegalArgumentException("Spawn world is null");
		}

		Entity entity = world.spawnEntity(location, EntityType.PILLAGER);
		if (!(entity instanceof LivingEntity living)) {
			return null;
		}

		return new AlienMonster(game, living);
	}

	public LivingEntity getEntity() {
		return alienEntity;
	}

	public void startAI() {
		if (task != null) {
			task.cancel();
		}

		task = new BukkitRunnable() {
			@Override
			public void run() {
				if (alienEntity == null || alienEntity.isDead()) {
					return;
				}

				Optional<Player> nearest = game.getTeamController().getLivingPlayers().stream()
						.filter(Player::isOnline)
						.filter(p -> p.getGameMode() != GameMode.SPECTATOR)
						.filter(p -> p.getWorld().equals(alienEntity.getWorld()))
						.filter(p -> canSeePlayer(p) || canHearPlayer(p))
						.min(Comparator
								.comparingDouble(p -> p.getLocation().distanceSquared(alienEntity.getLocation())));

				if (nearest.isEmpty()) {
					currentTarget = null;
					if (alienEntity instanceof Creature creature) {
						creature.setTarget(null);
					}
					return;
				}

				Player player = nearest.get();

				boolean seen = canSeePlayer(player);
				boolean heard = canHearPlayer(player);

				if (!seen && !heard) {
					return;
				}

				if (currentTarget == null || !currentTarget.equals(player)) {
					currentTarget = player;
					if (seen) {
						playEndermanSound();
					} else {
						playHearingSound();
					}
				}

				if (alienEntity instanceof Creature creature) {
					creature.setTarget(player);
				}

				if (alienEntity.getLocation().distance(player.getLocation()) <= KILL_RANGE) {
					player.setHealth(0.0);
				}
			}
		}.runTaskTimer(game.getPlugin(), 0L, 10L);
	}

	private void playEndermanSound() {
		if (alienEntity == null || alienEntity.isDead()) {
			return;
		}
		alienEntity.getWorld().playSound(alienEntity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.0f);
	}

	private void playHearingSound() {
		if (alienEntity == null || alienEntity.isDead()) {
			return;
		}
		alienEntity.getWorld().playSound(alienEntity.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 0.8f);
	}

	private boolean canSeePlayer(Player player) {
		if (alienEntity == null || player == null) {
			return false;
		}
		if (!player.getWorld().equals(alienEntity.getWorld())) {
			return false;
		}

		double dist = player.getLocation().distance(alienEntity.getLocation());
		if (dist > VISION_RANGE) {
			return false;
		}

		Vector toPlayer = player.getEyeLocation().toVector().subtract(alienEntity.getEyeLocation().toVector())
				.normalize();
		Vector forward = alienEntity.getEyeLocation().getDirection().normalize();
		double dot = forward.dot(toPlayer);

		if (dot < Math.cos(VISION_ANGLE / 2.0)) {
			return false;
		}

		return alienEntity.hasLineOfSight(player);
	}

	private boolean canHearPlayer(Player player) {
		if (alienEntity == null || player == null) {
			return false;
		}
		if (!player.getWorld().equals(alienEntity.getWorld())) {
			return false;
		}

		double dist = player.getLocation().distance(alienEntity.getLocation());
		if (dist > HEARING_RANGE) {
			return false;
		}

		if (player.isSprinting()) {
			return true;
		}

		return player.getVelocity() != null && player.getVelocity().length() > HEARING_VELOCITY_THRESHOLD;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();

		if (alienEntity == null || alienEntity.isDead()) {
			return;
		}
		if (!game.getTeamController().getLivingPlayers().contains(player)) {
			return;
		}
		if (currentTarget != null && currentTarget.equals(player)) {
			return;
		}

		if (canHearPlayer(player) && !canSeePlayer(player)) {
			currentTarget = player;
			if (alienEntity instanceof Creature creature) {
				creature.setTarget(player);
			}
			playHearingSound();
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity().equals(alienEntity)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!event.getEntity().equals(alienEntity)) {
			return;
		}
		event.setCancelled(true);

		if (event.getDamager() instanceof Player) {
			playEndermanSound();
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity().equals(alienEntity) && event.getTarget() instanceof Player) {
			playEndermanSound();
		}
	}

	public void killInstantly() {
		if (alienEntity != null && !alienEntity.isDead()) {
			alienEntity.setInvulnerable(false);
			alienEntity.remove();
		}
	}

	public void despawn() {
		if (task != null) {
			task.cancel();
			task = null;
		}

		HandlerList.unregisterAll(this);

		if (alienEntity != null && !alienEntity.isDead()) {
			alienEntity.remove();
		}
	}
}