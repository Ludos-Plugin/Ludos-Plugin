package fr.ludos.game.alien;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Creature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.Ludos;

public class AlienMonster implements Listener {
	private final AlienGame Game;
	private final LivingEntity AlienEntity;
	private BukkitTask task;
	private Player currentTarget = null;

	private static final double VISION_RANGE = 20.0;
	private static final double VISION_ANGLE = Math.toRadians(120.0);
	private static final double HEARING_RANGE = 30.0;
	private static final double HEARING_VELOCITY_THRESHOLD = 0.1;

	AlienMonster(AlienGame game, LivingEntity alienEntity) {
		this.Game = game;
		this.AlienEntity = alienEntity;

		// Bukkit.getPluginManager().registerEvents(this, game.getPlugin());

		// startAI();
		// // Log des paramètres de perception pour vérification runtime
		// game.getPlugin().getLogger()
		// .info(String.format("Alien spawned at %s — VISION_RANGE=%.1f,
		// VISION_ANGLE=%.1f°, HEARING_RANGE=%.1f",
		// entity.getLocation(), VISION_RANGE, Math.toDegrees(VISION_A
		// GLE), HEARING_RANGE));
	}

	//
	// this.entity.setCustomName("Alien");
	// this.entity.setCustomNameVisible(true);
	// this.entity.setRemoveWhenFarAway(false);
	// this.entity.setInvulnerable(true);

	private AlienGame getGame() {
		return this.Game;
	}

	private LivingEntity getEntity() {
		return this.AlienEntity;
	}

	public static AlienMonster spawn(AlienGame game, Location location) {
		World world = location.getWorld();
		if (world == null)
			throw new IllegalArgumentException("Spawn world is null");

		// Pour l'instant on spawn un PILLAGER en tant qu'asset PNJ temporaire
		Entity e = world.spawnEntity(location, EntityType.PILLAGER);
		if (!(e instanceof LivingEntity))
			return null;

		return new AlienMonster(game, (LivingEntity) e);
	}

	public static void startAI() {
		// Task périodique: chercher le joueur le plus pertinent (priorité aux joueurs
		// vus, sinon aux joueurs entendus)
		this.task = new BukkitRunnable() {
			@Override
			public void run() {
				if (this.getEntity() == null || this.getEntity().isDead()) {
					return;
				}

				Optional<Player> nearest = Bukkit.getOnlinePlayers().stream()
						.filter(p -> p.isOnline())
						.filter(p -> p.getGameMode() != org.bukkit.GameMode.SPECTATOR)
						.map(p -> (Player) p)
						.filter(p -> canSeePlayer(p) || canHearPlayer(p))
						.min((a, b) -> {
							boolean aSeen = canSeePlayer(a);
							boolean bSeen = canSeePlayer(b);

							if (aSeen != bSeen)
								return aSeen ? -1 : 1; // priorité aux joueurs vus

							return Double.compare(a.getLocation().distanceSquared(entity.getLocation()),
									b.getLocation().distanceSquared(entity.getLocation()));
						});

				if (nearest.isPresent()) {
					Player player = nearest.get();
					double dist = player.getLocation().distance(entity.getLocation());

					// Si trop loin par rapport au range maximal (vision ou ouïe), on désengage
					if (dist > Math.max(VISION_RANGE, HEARING_RANGE)) {
						currentTarget = null;

						if (entity instanceof Creature)
							((Creature) entity).setTarget(null);

						return;
					}

					boolean seen = canSeePlayer(player);

					// Si nouveau target: jouer un bruit adapté
					if (currentTarget == null || !currentTarget.equals(player)) {
						currentTarget = player;

						if (seen)
							playEndermanSound();
						else
							playHearingSound();
					}

					// Forcer la cible si possible
					if (entity instanceof Creature)
						((Creature) entity).setTarget(player);
				}
			}
		}.runTaskTimer(game.getPlugin(), 20, 20);
	}

	private void playEndermanSound() {
		if (entity == null || entity.isDead())

			return;

		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 1.0f);
	}

	// Son pour quand il entend quelqu'un (différent du son de repérage visuel)
	private void playHearingSound() {
		if (entity == null || entity.isDead())
			return;

		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
	}

	// Champ de vision: regarde si le joueur est devant et sans obstacle
	private boolean canSeePlayer(Player player) {
		if (entity == null || player == null)
			return false;

		if (!player.getWorld().equals(entity.getWorld()))
			return false;

		double dist = player.getLocation().distance(entity.getLocation());

		if (dist > VISION_RANGE)
			return false;

		Vector toPlayer = player.getEyeLocation().toVector().subtract(entity.getEyeLocation().toVector()).normalize();
		Vector forward = entity.getEyeLocation().getDirection().normalize();
		double dot = forward.dot(toPlayer);

		if (dot < Math.cos(VISION_ANGLE / 2.0))
			return false;

		return entity.hasLineOfSight(player);
	}

	// Ouïe simple: détecte joueurs proches et en mouvement/sprint

	private boolean canHearPlayer(Player player) {
		if (entity == null || player == null)
			return false;
		if (!player.getWorld().equals(entity.getWorld()))
			return false;
		double dist = player.getLocation().distance(entity.getLocation());
		if (dist > HEARING_RANGE)
			return false;
		if (player.isSprinting())
			return true;
		if (player.getVelocity() != null && player.getVelocity().length() > HEARING_VELOCITY_THRESHOLD)
			return true;
		return false;
	}

	// Événement de mouvement pour déclencher une détection immédiate par l'ouïe
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (entity == null || entity.isDead())
			return;
		if (currentTarget != null && currentTarget.equals(player))
			return;
		if (canHearPlayer(player) && !canSeePlayer(player)) {
			currentTarget = player;
			if (entity instanceof Creature) {
				((Creature) entity).setTarget(player);
			}
			playHearingSound();
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() == entity) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageEvent event) {
		if (event.getEntity() == entity && event instanceof EntityDamageByEntityEvent ede) {
			Entity damager = ede.getDamager();

			if (damager instanceof Player)
				playEndermanSound();
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity() == entity && event.getTarget() instanceof Player) {
			playEndermanSound();
		}
	}

	public void despawn() {
		if (task != null) {
			task.cancel();
			task = null;
		}

		HandlerList.unregisterAll(this);

		if (entity != null && !entity.isDead()) {
			entity.remove();
		}
	}
}
