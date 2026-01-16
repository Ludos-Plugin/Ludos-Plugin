package fr.ludos.game.alien;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

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
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

enum AlienState {
	IDLE,
	HUNTING,
	ATTACKING,
	FEAR
}

public class AlienMonster implements Listener {
	private final AlienGame Game;
	private LivingEntity AlienEntity = null;

	private Player currentTarget = null;
	private AlienState currentState = AlienState.IDLE;
	private AlienLocationOptions spawnOption = AlienLocationOptions.random;

	AlienMonster(AlienGame game, LivingEntity alienEntity, @Nullable Player player, @Nullable Location spawnLocation, @Nullable AlienState initialState) {
		this.Game = game;
		this.AlienEntity = alienEntity;

		this.currentTarget = player;
		this.currentState = initialState != null ? initialState : AlienState.IDLE;
		this.spawnOption = spawnLocation != null ? AlienLocationOptions.fromLocation(spawnLocation) : AlienLocationOptions.random;
	}

	private AlienGame getGame() {
		return this.Game;
	}

	private LivingEntity getEntity() {
		return this.AlienEntity;
	}

	private static final double VISION_RANGE = 28.0;
	private static final double VISION_ANGLE = Math.toRadians(110.0);
	private static final double HEARING_RANGE = 20.0;
	private static final double HEARING_VELOCITY_THRESHOLD = 0.15;

	List<Runnable> differentAlienModeList = List.of(
		() -> HuntMode(28.0, 110.0, 20.0, 0.15),
		() -> IdleMode(),
		() -> AttackMode(6, 20, 5.0f),
		() -> FearMode()
	);

	private List<HashMap<NamespacedKey, Object>> alienDataMap = new java.util.ArrayList<>(
		java.util.List.of(put(getGame().getPlugin(), "alien_mode_task"),
		put(getGame().getPlugin(), "alien_rage"),
		put(getGame().getPlugin(), "alien_last_noise"),
		put(getGame().getPlugin(), "alien_rage_target"))
	);

	private static <K, V> HashMap<K, V> put(JavaPlugin javaPlugin, String string) {
		return new HashMap<>();
	}

	NamespacedKey KEY_TASK = alienDataMap.get(0).keySet().iterator().next();
	NamespacedKey KEY_RAGE = alienDataMap.get(1).keySet().iterator().next();
	NamespacedKey KEY_LAST_NOISE = alienDataMap.get(2).keySet().iterator().next();
	NamespacedKey KEY_RAGE_TARGET = alienDataMap.get(3).keySet().iterator().next();

	private Boolean alienisAlive = getEntity() != null && !getEntity().isDead() ? true : false;

	private PersistentDataContainer alienPersistantData = getEntity().getPersistentDataContainer();

	private boolean checkIfPlayerIsAlive(Player player) {
		return player == null || !player.isOnline() || player.isDead();
	}

	private void overrideMovement() {
		if (!(AlienEntity instanceof Creature creature)) return;

		creature.setTarget(null);

		switch (currentState) {
			case HUNTING -> differentAlienModeList.get(0).run();
			case IDLE -> differentAlienModeList.get(1).run();
			case ATTACKING -> differentAlienModeList.get(2).run();
			case FEAR -> differentAlienModeList.get(3).run();
		}
	}

	private void updateAlienState(AlienState state, @Nullable BukkitRunnable currentRunnable, @Nullable Player targetPlayer) {
		this.currentTarget = targetPlayer;
		this.currentState = state;

		Integer oldTaskId = alienPersistantData.get(KEY_TASK, PersistentDataType.INTEGER);
		if (oldTaskId != null) Bukkit.getScheduler().cancelTask(oldTaskId);

		currentRunnable.cancel();

		alienPersistantData.set(KEY_TASK, PersistentDataType.INTEGER, currentRunnable.getTaskId());
		overrideMovement();
	}

	private void intializeAlienData() {
		if (alienPersistantData.get(KEY_RAGE, PersistentDataType.DOUBLE).isNaN()) {
			alienPersistantData.set(KEY_RAGE, PersistentDataType.DOUBLE, 0.0);
		}

		if (alienPersistantData.get(KEY_LAST_NOISE, PersistentDataType.LONG) == null) {
			alienPersistantData.set(KEY_LAST_NOISE, PersistentDataType.LONG, System.currentTimeMillis());
		}
	}

	public void AttackMode(double attackDamage, int attackCooldownTicks, float attackRange) {
		this.currentState = AlienState.ATTACKING;

		Integer oldTaskId = alienPersistantData.get(KEY_TASK, PersistentDataType.INTEGER);
		if (oldTaskId != null) Bukkit.getScheduler().cancelTask(oldTaskId);

		BukkitRunnable task = AttackRunnable(
			attackDamage,
			attackCooldownTicks,
			attackRange
		);

		int taskId = task.runTaskTimer(getGame().getPlugin(), 1L, 1L).getTaskId();
		alienPersistantData.set(KEY_TASK, PersistentDataType.INTEGER, taskId);
	}

	public void HuntMode(double visionRange, double visionAngle, double hearingRange, double hearingVelocity) {
		this.currentState = AlienState.HUNTING;

		Integer oldTaskId = alienPersistantData.get(KEY_TASK, PersistentDataType.INTEGER);
		if (oldTaskId != null) Bukkit.getScheduler().cancelTask(oldTaskId);

		BukkitRunnable task = HuntRunnable(
			visionRange,
			visionAngle,
			hearingRange,
			hearingVelocity
		);

		int taskId = task.runTaskTimer(getGame().getPlugin(), 10L, 10L).getTaskId();
		alienPersistantData.set(KEY_TASK, PersistentDataType.INTEGER, taskId);
	}

	public static <T> T getOrDefault(@Nullable T value, T defaultValue, T condition) {
		if (value == null || value.equals(condition)) {
			return defaultValue;
		}

		return value;
	}

	private BukkitRunnable AttackRunnable (double attackDamage, int attackCooldownTicks, float attackRange) {
		return new BukkitRunnable() {
			int attacksDone, cooldown;

			@Override
			public void run() {
				if (alienisAlive) cancel();

				if (currentState != AlienState.ATTACKING) cancel();

				if (checkIfPlayerIsAlive(currentTarget)) {
					updateAlienState(AlienState.IDLE, this, null);
					return;
				}

				Location aLoc = getEntity().getLocation();
				Location tLoc = currentTarget.getLocation();

				if (aLoc.getWorld() == null || tLoc.getWorld() == null || !aLoc.getWorld().equals(tLoc.getWorld())) {
					updateAlienState(AlienState.HUNTING, this, null);
					return;
				}

				cooldown = (int) getOrDefault(cooldown - 1, cooldown, cooldown > 0);

				if (cooldown > 0) return;

				attacksDone++;

				cooldown = Math.max(1, attackCooldownTicks);

				Vector dir = tLoc.toVector().subtract(aLoc.toVector());

				if (dir.lengthSquared() > 0.0001) dir.normalize();

				getEntity().setVelocity(dir.clone().multiply(0.45).setY(Math.min(0.25, getEntity().getVelocity().getY() + 0.10)));

				currentTarget.damage(attackDamage, getEntity());

				currentTarget.getWorld().playSound(currentTarget.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 1.6f);
				getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.35f, 0.7f);

				currentTarget.setVelocity(currentTarget.getVelocity().add(dir.clone().multiply(0.35)));

				if (attacksDone >= 2) updateAlienState(AlienState.FEAR, this, currentTarget);
			}
		};
	}

	public void IdleMode() {
		new BukkitRunnable() {
			private Vector wanderDir = Vector.getRandom().setY(0).normalize();

			@Override
			public void run() {
				if (alienisAlive) cancel();

				if (Math.random() < 0.05) {
					wanderDir = Vector.getRandom().setY(0).normalize();
				}

				getEntity().setVelocity(wanderDir.multiply(0.15));
				getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 0.2f, 0.6f);
			}
		}.runTaskTimer(getGame().getPlugin(), 20, 20);
	}

	private BukkitRunnable HuntRunnable(double visionRange, double visionAngle, double hearingRange, double hearingVelocity) {
		return new BukkitRunnable() {
			@Override
			public void run() {
				if (alienisAlive) cancel();

				Optional<Player> nearest = Bukkit.getOnlinePlayers().stream()
					.filter(p -> p.getWorld().equals(getEntity().getWorld()))
					.filter(p -> !p.isDead())
					.map(p -> (Player) p)
					.min((a, b) -> Double.compare(
						a.getLocation().distanceSquared(getEntity().getLocation()),
						b.getLocation().distanceSquared(getEntity().getLocation())
					));

				if (nearest.isEmpty()) return;

				Player target = nearest.get();
				double dist = target.getLocation().distance(getEntity().getLocation());

				if (dist <= 2.5) {
					updateAlienState(AlienState.ATTACKING, this, target);
					return;
				}

				Vector dir = target.getLocation().toVector().subtract(getEntity().getLocation().toVector()).normalize();
				getEntity().setVelocity(dir.multiply(0.25));

				if (getEntity() instanceof Creature c) c.setTarget(target);
			}
		};
	}

	public void FearMode() {
		new BukkitRunnable() {
			private int ticks = 0;

			@Override
			public void run() {
				if (getEntity() == null || getEntity().isDead()) cancel();

				if (ticks++ > 60) {
					updateAlienState(AlienState.IDLE, this, null);
					return;
				}

				Vector back = getEntity().getLocation().getDirection().multiply(-0.4).setY(0.1);
				getEntity().setVelocity(back);
			}
		}.runTaskTimer(getGame().getPlugin(), 1, 1);
	}

	public void alienMovementMode(double VISION_RANGE, double VISION_ANGLE, double HEARING_RANGE, double HEARING_VELOCITY_THRESHOLD) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (getEntity() == null || getEntity().isDead()) return;

				Optional<Player> nearest = Bukkit.getOnlinePlayers().stream()
					.filter(p -> p.isOnline())
					.filter(p -> p.getGameMode() != org.bukkit.GameMode.SPECTATOR)
					.map(p -> (Player) p)
					.filter(p -> canSeePlayer(p) || canHearPlayer(p))
					.min((p1, p2) -> {
						double d1 = p1.getLocation().distance(getEntity().getLocation());
						double d2 = p2.getLocation().distance(getEntity().getLocation());
						return Double.compare(d1, d2);
					});

				if ((currentTarget = nearest.get()) != null) return;

				double dist = currentTarget.getLocation().distance(getEntity().getLocation());

				if (dist > Math.max(VISION_RANGE, HEARING_RANGE)) {
					currentTarget = null;

					if (AlienEntity instanceof Creature) ((Creature) AlienEntity).setTarget(null);

					return;
				}

				boolean seen = canSeePlayer(currentTarget);

				if (currentTarget == null || !currentTarget.equals(currentTarget)) {
					if (seen) playAlienAmbientSound(Sound.ENTITY_ENDERMAN_SCREAM, null, null);
					else playAlienAmbientSound(Sound.ENTITY_WOLF_GROWL, null, null);
				}

				if (AlienEntity instanceof Creature) ((Creature) AlienEntity).setTarget(currentTarget);
			}
		}.runTaskTimer(getGame().getPlugin(), 20, 20);
	}

	public static void runAlienMode(AlienMonster alienMonster) {
		alienMonster.differentAlienModeList.get(0).run();
	}

	public static AlienMonster spawn(AlienGame game, Location location) {
		World world = location.getWorld();
		if (world == null) return null;

		Entity e = world.spawnEntity(location, EntityType.PILLAGER);
		if (!(e instanceof LivingEntity)) return null;

		return new AlienMonster(game, (LivingEntity) e, null, location, null);
	}

	private void playAlienAmbientSound(Sound sound, @Nullable Float volume, @Nullable Float pitch) {
		if (getEntity() == null || getEntity().isDead()) return;

		getEntity().getWorld().playSound(getEntity().getLocation(), sound, volume != null ? volume : 1.0f, pitch != null ? pitch : 1.0f);
	}

	private boolean canSeePlayer(Player player) {
		if (getEntity() == null || player == null) return false;

		if (!player.getWorld().equals(getEntity().getWorld())) return false;

		double dist = player.getLocation().distance(getEntity().getLocation());

		if (dist > VISION_RANGE) return false;

		Vector toPlayer = player.getEyeLocation().toVector().subtract(getEntity().getEyeLocation().toVector()).normalize();
		Vector forward = getEntity().getEyeLocation().getDirection().normalize();
		double dot = forward.dot(toPlayer);

		if (dot < Math.cos(VISION_ANGLE / 2.0)) return false;

		return getEntity().hasLineOfSight(player);
	}

	private boolean canHearPlayer(Player player) {
		if (checkIfPlayerIsAlive(player) || alienisAlive) return false;

		if (!player.getWorld().equals(getEntity().getWorld())) return false;

		double dist = player.getLocation().distance(getEntity().getLocation());

		if (dist > HEARING_RANGE) return false;
		if (player.isSprinting()) return true;

		if (player.getVelocity() != null && player.getVelocity().length() > HEARING_VELOCITY_THRESHOLD) return true;

		return false;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player == null) return;

		if (alienisAlive) return;
		if (currentTarget != null && currentTarget.equals(player)) return;

		if (canHearPlayer(player) && !canSeePlayer(player)) {
			currentTarget = player;

			if (this.getEntity() instanceof Creature) {
				((Creature) this.getEntity()).setTarget(player);
			}

			playAlienAmbientSound(Sound.ENTITY_WOLF_GROWL, null, null);
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageEvent event) {
		if (event.getEntity() == this.getEntity() && event instanceof EntityDamageByEntityEvent enemy) {
			event.setCancelled(true);

			Entity damager = enemy.getDamager();

			if (damager instanceof Player){
				playAlienAmbientSound(Sound.ENTITY_ENDERMAN_SCREAM, null, null);
			}
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.getEntity() == this.getEntity() && event.getTarget() instanceof Player) {
			playAlienAmbientSound(Sound.ENTITY_ENDERMAN_SCREAM, null, null);
		}
	}

	public void despawn() {
		HandlerList.unregisterAll(this);

		if (alienisAlive) AlienEntity.remove();
	}
}
