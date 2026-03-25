package fr.ludos.game.arena.monster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import fr.ludos.game.arena.ArenaGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DarkKnightBoss extends SpecialMonster<WitherSkeleton> {
	private enum CombatProfile {
		DODGEABLE(45, 95, 72, 125, 6.0, 4.5, 2.5, 3.5, 1.45, 90, 13, 4, 2, 0.62, 18.0, 0.70, 5, 36),
		DENSE(34, 82, 62, 108, 8.5, 5.5, 3.5, 5.0, 1.7, 120, 8, 5, 3, 0.78, 22.0, 0.82, 7, 36),
		INFERNAL(24, 70, 52, 92, 11.0, 7.0, 4.5, 6.5, 1.95, 140, 5, 7, 4, 0.92, 26.0, 0.96, 9, 48);

		final int attackCooldown, lightningMeleeCooldown, dashCooldown, attractionCooldown;
		final double lightningDamage, dashDamage, erosionDamage, earthShatterDamage, dashSpeed;
		final int orbPatternTicks, orbVolleyInterval, orbCount, strikeCount;
		final double fireballSpeed, pullRadius, pullPower;
		final int explosionDamage, dashTriggerChance;

		CombatProfile(int attackCooldown, int lightningMeleeCooldown, int dashCooldown, int attractionCooldown,
					  double lightningDamage, double dashDamage, double erosionDamage, double earthShatterDamage,
					  double dashSpeed, int orbPatternTicks, int orbVolleyInterval, int orbCount, int strikeCount,
					  double fireballSpeed, double pullRadius, double pullPower, int explosionDamage, int dashTriggerChance) {
			this.attackCooldown = attackCooldown;
			this.lightningMeleeCooldown = lightningMeleeCooldown;
			this.dashCooldown = dashCooldown;
			this.attractionCooldown = attractionCooldown;
			this.lightningDamage = lightningDamage;
			this.dashDamage = dashDamage;
			this.erosionDamage = erosionDamage;
			this.earthShatterDamage = earthShatterDamage;
			this.dashSpeed = dashSpeed;
			this.orbPatternTicks = orbPatternTicks;
			this.orbVolleyInterval = orbVolleyInterval;
			this.orbCount = orbCount;
			this.strikeCount = strikeCount;
			this.fireballSpeed = fireballSpeed;
			this.pullRadius = pullRadius;
			this.pullPower = pullPower;
			this.explosionDamage = explosionDamage;
			this.dashTriggerChance = dashTriggerChance;
		}
	}

	private enum AttackPattern {
		LIGHTNING_MELEE {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				if (!(focusTarget != null && self.lightningMeleeCooldown <= 0 && focusTarget.getLocation().distanceSquared(boss.getLocation()) <= MELEE_DIST_SQ)) return false;
				self.lightningPattern(boss); self.lightningMeleeCooldown = profile.lightningMeleeCooldown; return true;
			}
		},
		DASH {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				if (!(focusTarget != null && self.dashCooldown <= 0 && focusTarget.getLocation().distanceSquared(boss.getLocation()) > DASH_DIST_SQ)) return false;
				self.dashPattern(boss, focusTarget, profile); self.dashCooldown = profile.dashCooldown; return true;
			}
		},
		EROSION {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				if (focusTarget == null) return false;

				self.erosionBurstPattern(boss, focusTarget, profile);
				return true;
			}
		},
		GRAVITY {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				Player p = rangedTarget != null ? rangedTarget : focusTarget;
				if (!self.gravityAttractionPattern(boss, p, profile, true)) return false;
				self.attractionCooldown = profile.attractionCooldown; return true;
			}
		},
		EARTH_SHATTER {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				self.earthShatterPattern(boss);
				return true;
			}
		},
		ORBITING_ORBS {
			@Override
			boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile) {
				self.orbitingOrbsPattern(boss);
				return true;
			}
		};

		abstract boolean perform(DarkKnightBoss self, WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player focusTarget, @org.jetbrains.annotations.Nullable Player rangedTarget, CombatProfile profile);
	}

	private static final double MAX_HEALTH = 460.0;
	private static final int DARK_KNIGHT_MODEL_DATA = 1;
	private static final double CHEST_Y = 0.1, LEGGINGS_Y = -0.08;
	private static final int NO_HIT_TICKS_FOR_ATTRACTION = 120;
	private static final int FAR_DISTANCE_TICKS_FOR_FORCE_PULL = 80;

	private static final double MELEE_DIST_SQ = 49.0, DASH_DIST_SQ = 20.25, FOCUS_DIST_SQ = 1600.0, RANGED_DIST_SQ = 3025.0;
	private static final double LIGHTNING_DIST_SQ = 64.0, EROSION_DIST_SQ = 6.25;
	private static final double GRAVITY_EXEMPT_RADIUS = 3.0, RANGED_MIN_DIST_SQ = 49.0;
	private static final double FORCE_PULL_DIST_SQ = 324.0;
	private static final int STUCK_TICKS_THRESHOLD = 45;
	private static final double STUCK_MOVE_EPSILON_SQ = 0.0025;
	private static final double LOS_PENALTY = 5.5, VERTICAL_PENALTY = 3.0, VERTICAL_CHECK = 4.0;

	private int attackTick = 0;
	private int phase = 1;
	private int lightningMeleeCooldown = 0;
	private int dashCooldown = 0;
	private int attractionCooldown = 0;
	private int ticksSinceBossHitAPlayer = 0;
	private int distantTicks = 0;
	private int stuckTicks = 0;

	private final List<ArmorStand> activeOrbs = new ArrayList<>();

	@org.jetbrains.annotations.Nullable
	private ArmorStand chestVisual;

	@org.jetbrains.annotations.Nullable
	private ArmorStand leggingsVisual;

	@org.jetbrains.annotations.Nullable
	private Location previousBossLocation;

	@org.jetbrains.annotations.Nullable
	private Location lastStuckCheckLocation;

	private double armorAnimPhase = 0.0;
	private final Map<UUID, Double> aggroScores = new HashMap<>();

	@org.jetbrains.annotations.Nullable
	private EntityDamageEvent lastProcessedDamageEvent;

	@org.jetbrains.annotations.Nullable
	private BukkitTask orbitTask;

	public DarkKnightBoss(ArenaGame game) {
		super("dark_knight", game);
	}

	@Override
	protected WitherSkeleton spawnMonsterEntity(Location location) {
		World w = location.getWorld();

		if (w == null) throw new IllegalStateException("Boss location world is null");

		WitherSkeleton b = (WitherSkeleton) w.spawnEntity(location, EntityType.WITHER_SKELETON);

		b.customName(Component.text("The Dark Knight").color(NamedTextColor.DARK_PURPLE));

		b.setCustomNameVisible(true);
		b.setRemoveWhenFarAway(false);
		b.setPersistent(true);
		b.setInvisible(false);

		setAttributeBase(b, Attribute.GENERIC_FOLLOW_RANGE, 64.0);
		setAttributeBase(b, Attribute.GENERIC_MOVEMENT_SPEED, 0.33);
		setAttributeBase(b, Attribute.GENERIC_ATTACK_DAMAGE, 12.0);
		setMaxHealth(b, MAX_HEALTH);

		b.setHealth(MAX_HEALTH);

		b.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
		b.getEquipment().setItemInMainHandDropChance(0.0f);
		b.getEquipment().setHelmet(createDarkKnightHelmet());
		b.getEquipment().setHelmetDropChance(0.0f);

		b.setCanPickupItems(false); b.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

		return b;
	}

	private ItemStack createDarkKnightHelmet() {
		return createCustomModelItem(Material.CARVED_PUMPKIN, DARK_KNIGHT_MODEL_DATA);
	}

	private ItemStack createDarkKnightChestplate() {
		return createCustomModelItem(Material.IRON_CHESTPLATE, DARK_KNIGHT_MODEL_DATA);
	}

	private ItemStack createDarkKnightLeggings() {
		return createCustomModelItem(Material.IRON_LEGGINGS, DARK_KNIGHT_MODEL_DATA);
	}

	private ItemStack createCustomModelItem(Material material, int modelData) {
		ItemStack i = new ItemStack(material);
		ItemMeta m = i.getItemMeta();

		m.setCustomModelData(modelData);
		i.setItemMeta(m);

		return i;
	}

	@Override
	protected void onMonsterSpawn(WitherSkeleton entity) {
		spawnArmorVisuals(entity);
	}

	@Override
	protected void onMonsterTick(WitherSkeleton entity) {
		ensureHelmetVisual(entity);
		processIncomingAggro(entity);
		decayAggroScores();
		enforceWaterStride(entity);
		updateDistantPressure(entity);
		handleStuckMovement(entity);
		spawnAmbientAura(entity);
		updateArmorVisuals(entity);
		updatePhase(entity);

		CombatProfile p = resolveProfile(entity);

		ticksSinceBossHitAPlayer++;
		lightningMeleeCooldown = tickCooldown(lightningMeleeCooldown);
		dashCooldown = tickCooldown(dashCooldown);
		attractionCooldown = tickCooldown(attractionCooldown);

		if (phase >= 3) {
			lightningMeleeCooldown = tickCooldown(lightningMeleeCooldown);
			dashCooldown = tickCooldown(dashCooldown);
			attractionCooldown = tickCooldown(attractionCooldown);

			if (attackTick > 0) attackTick--;
		}

		if (attackTick > 0) {
			attackTick--;
			return;
		}

		triggerPattern(entity);
		attackTick = p.attackCooldown;
	}

	@Override
	protected void onMonsterDespawn(WitherSkeleton entity) {
		cleanupOrbs();
	}

	@Override
	protected void onMonsterDeath(WitherSkeleton entity) {
		cleanupOrbs();

		entity.getWorld().strikeLightningEffect(entity.getLocation());
		entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, entity.getLocation(), 3, 1.0, 0.6, 1.0, 0.0);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.85f, 0.9f);
	}


	private void spawnArmorVisuals(WitherSkeleton boss) {
		World w = boss.getWorld();

		chestVisual = createVisualStand(w, createDarkKnightChestplate(), false, new EulerAngle(Math.toRadians(-2.0), 0.0, 0.0));
		leggingsVisual = createVisualStand(w, createDarkKnightLeggings(), false, new EulerAngle(0.0, 0.0, 0.0));

		previousBossLocation = boss.getLocation().clone();
		armorAnimPhase = 0.0;

		updateArmorVisuals(boss);
	}

	private ArmorStand createVisualStand(World w, ItemStack item, boolean small, EulerAngle head) {
		ArmorStand s = (ArmorStand) w.spawnEntity(w.getSpawnLocation(), EntityType.ARMOR_STAND);

		s.setVisible(false);
		s.setMarker(true);
		s.setGravity(false);
		s.setInvulnerable(true);
		s.setSilent(true);
		s.setBasePlate(false);
		s.setArms(false);
		s.setPersistent(false);
		s.setCollidable(false);
		s.setSmall(small);
		s.setHeadPose(head);
		s.getEquipment().setHelmet(item);

		return s;
	}

	private void ensureHelmetVisual(WitherSkeleton boss) {
		ItemStack currentHelmet = boss.getEquipment() != null ? boss.getEquipment().getHelmet() : null;
		if (currentHelmet == null || currentHelmet.getType().isAir()) {
			boss.getEquipment().setHelmet(createDarkKnightHelmet());
			boss.getEquipment().setHelmetDropChance(0.0f);
		}
	}

	private void updateArmorVisuals(WitherSkeleton boss) {
		if (!boss.isValid() || boss.isDead()) return;

		if (chestVisual == null || !chestVisual.isValid() || leggingsVisual == null || !leggingsVisual.isValid()) {
			spawnArmorVisuals(boss);
			return;
		}

		Location base = boss.getLocation();
		double moveSpeed = previousBossLocation != null && previousBossLocation.getWorld() != null && previousBossLocation.getWorld().equals(base.getWorld())
			? Math.sqrt(previousBossLocation.distanceSquared(base)) : 0.0;

		previousBossLocation = base.clone();

		armorAnimPhase += 0.22 + Math.min(0.65, moveSpeed * 3.5);
		double motionFactor = Math.min(1.0, moveSpeed * 10.0);
		double bob = Math.sin(armorAnimPhase * 1.7) * (0.012 + motionFactor * 0.022);
		double chestPitch = Math.toRadians(-3.0 + Math.sin(armorAnimPhase) * (2.0 + motionFactor * 3.0));
		double leggingsPitch = Math.toRadians(Math.sin(armorAnimPhase + 1.1) * (1.2 + motionFactor * 2.0));
		double subtleYaw = Math.toRadians(Math.cos(armorAnimPhase * 0.8) * (1.8 + motionFactor * 2.4));

		Location chestLoc = base.clone().add(0.0, CHEST_Y + bob, 0.0);
		Location legsLoc = base.clone().add(0.0, LEGGINGS_Y + (bob * 0.55), 0.0);

		chestLoc.setYaw(base.getYaw());
		chestLoc.setPitch(0.0f);
		legsLoc.setYaw(base.getYaw());
		legsLoc.setPitch(0.0f);

		chestVisual.setHeadPose(new EulerAngle(chestPitch, subtleYaw, 0.0));
		leggingsVisual.setHeadPose(new EulerAngle(leggingsPitch, -subtleYaw, 0.0));
		chestVisual.teleport(chestLoc);
		leggingsVisual.teleport(legsLoc);
	}

	private void updatePhase(WitherSkeleton boss) {
		double healthRatio = boss.getHealth() / MAX_HEALTH;
		int nextPhase = healthRatio > 0.66 ? 1 : healthRatio > 0.33 ? 2 : 3;

		if (nextPhase == phase) return;

		phase = nextPhase;
		boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.7f + (0.1f * phase));
		boss.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, boss.getLocation().add(0, 1.4, 0), 40, 0.6, 0.6, 0.6, 0.02);
	}

	private void triggerPattern(WitherSkeleton boss) {
		int random = ThreadLocalRandom.current().nextInt(100);
		CombatProfile profile = resolveProfile(boss);
		Player focusTarget = selectFocusTarget(boss, boss.getWorld(), boss.getLocation(), FOCUS_DIST_SQ);
		Player rangedAggro = selectRangedAggroTarget(boss, boss.getWorld(), boss.getLocation(), RANGED_DIST_SQ);

		if (distantTicks >= FAR_DISTANCE_TICKS_FOR_FORCE_PULL && attractionCooldown <= 0
			&& violentAttractionPattern(boss, profile)) {
			attractionCooldown = Math.max(12, profile.attractionCooldown / 2);
			return;
		}

		if (attractionCooldown <= 0 && ticksSinceBossHitAPlayer >= NO_HIT_TICKS_FOR_ATTRACTION
			&& AttackPattern.GRAVITY.perform(this, boss, focusTarget, rangedAggro, profile)) return;

		if (rangedAggro != null && attractionCooldown <= 0) {
			if (random < 56 && AttackPattern.GRAVITY.perform(this, boss, focusTarget, rangedAggro, profile)) return;
			if (random < 88) {
				AttackPattern.ORBITING_ORBS.perform(this, boss, focusTarget, rangedAggro, profile);
				return;
			}
		}

		if (AttackPattern.LIGHTNING_MELEE.perform(this, boss, focusTarget, rangedAggro, profile)) return;
		if (random < profile.dashTriggerChance && AttackPattern.DASH.perform(this, boss, focusTarget, rangedAggro, profile)) return;

		chooseCorePattern(random, profile, focusTarget != null).perform(this, boss, focusTarget, rangedAggro, profile);
	}

	private AttackPattern chooseCorePattern(int random, CombatProfile profile, boolean hasFocusTarget) {
		if (phase == 1) {
			return (random < 52 || random >= 86) ? AttackPattern.EARTH_SHATTER : AttackPattern.ORBITING_ORBS;
		}

		if (phase == 3) {
			if (hasFocusTarget && random < 55) return AttackPattern.EROSION;
			return random < 82 ? AttackPattern.ORBITING_ORBS : AttackPattern.EARTH_SHATTER;
		}

		int earthTier = profile == CombatProfile.INFERNAL ? 0 : profile == CombatProfile.DENSE ? 1 : 2;
		int[] earthChances = {30, 38, 45};
		int[] orbitChances = {66, 62, 70};

		return random < earthChances[earthTier] ? AttackPattern.EARTH_SHATTER :
			   random < orbitChances[earthTier] ? AttackPattern.ORBITING_ORBS :
			   hasFocusTarget ? AttackPattern.EROSION : AttackPattern.ORBITING_ORBS;
	}

	private void lightningPattern(WitherSkeleton boss) {
		World world = boss.getWorld();
		Player target = selectFocusTarget(boss, world, boss.getLocation(), LIGHTNING_DIST_SQ);

		if (target == null || target.getLocation().distanceSquared(boss.getLocation()) > LIGHTNING_DIST_SQ) return;

		CombatProfile profile = resolveProfile(boss);
		Location targetLoc = target.getLocation();

		spawnGroundRing(targetLoc, 1.1, Particle.ELECTRIC_SPARK, 20);
		world.playSound(targetLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.9f, 0.75f);

		for (int i = 0; i < profile.strikeCount; i++) {
			Location strikePos = targetLoc.clone().add(
				ThreadLocalRandom.current().nextDouble(-0.85, 0.85),
				0,
				ThreadLocalRandom.current().nextDouble(-0.85, 0.85)
			);

			world.strikeLightningEffect(strikePos);
			world.spawnParticle(Particle.CRIT_MAGIC, strikePos.clone().add(0, 1.0, 0), 12, 0.25, 0.4, 0.25, 0.03);
		}

		dealBossDamage(boss, target, profile.lightningDamage);
		erosionBurstPattern(boss, target, profile);
		world.playSound(boss.getLocation().clone().add(0.0, 1.0, 0.0), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
	}

	private void erosionBurstPattern(WitherSkeleton boss, Player focusTarget, CombatProfile profile) {
		if (!focusTarget.isOnline() || focusTarget.isDead()) return;

		World world = boss.getWorld();
		Location targetLoc = focusTarget.getLocation();

		focusTarget.addPotionEffect(new PotionEffect(
			PotionEffectType.SLOW, 60,
			profile == CombatProfile.INFERNAL ? 1 : 0,
			false, true, true
		));

		focusTarget.addPotionEffect(new PotionEffect(
			PotionEffectType.WEAKNESS, 80, 0,
			false, true, true
		));

		erodeArmorDurability(focusTarget, profile == CombatProfile.INFERNAL ? 2 : 1);

		for (int i = 0; i < 2; i++) {
			Location burstLoc = targetLoc.clone().add(
				ThreadLocalRandom.current().nextDouble(-1.15, 1.15),
				0.15,
				ThreadLocalRandom.current().nextDouble(-1.15, 1.15)
			);

			world.spawnParticle(Particle.EXPLOSION_NORMAL, burstLoc, 9, 0.2, 0.2, 0.2, 0.02);
			world.spawnParticle(Particle.SQUID_INK, burstLoc, 8, 0.25, 0.1, 0.25, 0.02);
			world.spawnParticle(Particle.SMOKE_LARGE, burstLoc, 12, 0.18, 0.18, 0.18, 0.03);
		}

		getArenaTargets(world, targetLoc, EROSION_DIST_SQ)
			.forEach(player -> dealBossDamage(boss, player, profile.erosionDamage));

		world.playSound(targetLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.9f, 0.65f);
	}

	private void erodeArmorDurability(Player player, int steps) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] armorContents = inventory.getArmorContents();

		if (armorContents == null) return;

		var damageableSlots = new ArrayList<Integer>();
		for (int slotIdx = 0; slotIdx < armorContents.length; slotIdx++) {
			ItemStack armorPiece = armorContents[slotIdx];

			if (armorPiece != null && !armorPiece.getType().isAir()) {
				ItemMeta meta = armorPiece.getItemMeta();
				if (meta instanceof Damageable) {
					damageableSlots.add(slotIdx);
				}
			}
		}

		if (damageableSlots.isEmpty()) return;

		for (int damageStep = 0; damageStep < steps; damageStep++) {
			int selectedSlot = damageableSlots.get(ThreadLocalRandom.current().nextInt(damageableSlots.size()));
			ItemStack selectedArmor = armorContents[selectedSlot];

			if (selectedArmor == null || selectedArmor.getType().isAir()) continue;

			ItemMeta armorMeta = selectedArmor.getItemMeta();
			if (!(armorMeta instanceof Damageable damageable)) continue;

			int maxDurability = selectedArmor.getType().getMaxDurability();
			if (maxDurability <= 1) continue;

			int damageAmount = ThreadLocalRandom.current().nextInt(1, 3);
			damageable.setDamage(Math.min(maxDurability - 1, damageable.getDamage() + damageAmount));
			selectedArmor.setItemMeta(armorMeta);
		}
		inventory.setArmorContents(armorContents);
	}

	private void dashPattern(WitherSkeleton boss, Player focusTarget, CombatProfile profile) {
		Location startLoc = boss.getLocation();
		spawnGroundRing(startLoc, 1.4, Particle.SOUL_FIRE_FLAME, 24);
		spawnGroundRing(focusTarget.getLocation(), 0.9, Particle.CRIT_MAGIC, 14);

		Vector dashDirection = focusTarget.getLocation().toVector().subtract(startLoc.toVector());
		if (dashDirection.lengthSquared() < 0.0001) return;

		dashDirection.normalize().multiply(profile.dashSpeed).setY(0.23);
		boss.setVelocity(dashDirection);

		World world = boss.getWorld();
		world.playSound(startLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.7f);
		world.spawnParticle(Particle.CLOUD, startLoc.clone().add(0, 0.2, 0), 18, 0.4, 0.15, 0.4, 0.01);

		java.util.Set<Player> hitPlayers = new java.util.HashSet<>();

		new BukkitRunnable() {
			private int ticks = 0;

			@Override
			public void run() {
				if (!isAlive() || boss.isDead() || ticks > 10) {
					cancel();
					return;
				}

				Location trailLoc = boss.getLocation().clone().add(0, 1.0, 0);

				world.spawnParticle(Particle.SOUL_FIRE_FLAME, trailLoc, 4, 0.22, 0.1, 0.22, 0.01);
				world.spawnParticle(Particle.SWEEP_ATTACK, trailLoc, 2, 0.2, 0.1, 0.2, 0.0);
				world.spawnParticle(Particle.SMOKE_NORMAL, trailLoc, 6, 0.2, 0.2, 0.2, 0.01);

				getArenaTargets(world, trailLoc, 4.84).stream()
					.filter(p -> !hitPlayers.contains(p))
					.forEach(p -> {
						dealBossDamage(boss, p, profile.dashDamage);
						hitPlayers.add(p);
					});
				ticks++;
			}
		}.runTaskTimer(getGame().getPlugin(), 0L, 1L);
	}

	private void earthShatterPattern(WitherSkeleton boss) {
		World world = boss.getWorld();
		Location center = boss.getLocation();
		CombatProfile profile = resolveProfile(boss);

		spawnGroundRing(center, 2.1, Particle.BLOCK_CRACK, 28);
		spawnGroundRing(center, 3.2, Particle.CRIT, 32);

		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.65f);
		world.spawnParticle(Particle.BLOCK_CRACK, center, 80, 3.5, 0.2, 3.5, Material.DEEPSLATE.createBlockData());

		getArenaTargets(world, center, 196).forEach(player -> {
			double distanceSquared = player.getLocation().distanceSquared(center);
			if (distanceSquared > MELEE_DIST_SQ) {
				Vector knockback = player.getLocation().toVector().subtract(center.toVector());
				if (knockback.lengthSquared() < 0.0001) {
					knockback = new Vector(0.1, 0.0, 0.1);
				}
				player.setVelocity(knockback.normalize().multiply(1.25).setY(0.75));
			}

			dealBossDamage(boss, player, profile.earthShatterDamage);
		});
	}

	private boolean gravityAttractionPattern(WitherSkeleton boss, @org.jetbrains.annotations.Nullable Player preferredTarget, CombatProfile profile, boolean farOnly) {
		World world = boss.getWorld();
		Location center = boss.getLocation();
		double exemptRadius = farOnly ? GRAVITY_EXEMPT_RADIUS : 0.0;

		java.util.List<Player> affectedPlayers = getArenaTargets(world, center, profile.pullRadius * profile.pullRadius).stream()
			.filter(p -> !farOnly || p.getLocation().distanceSquared(center) > exemptRadius * exemptRadius)
			.collect(Collectors.toList());

		if (affectedPlayers.isEmpty()) return false;

		spawnGroundRing(center, 2.6, Particle.PORTAL, 28);

		world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 1.0, 0), 45, 1.8, 0.45, 1.8, 0.02);
		world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.1f, 0.75f);
		world.playSound(center, Sound.ENTITY_ENDERMAN_SCREAM, 0.75f, 0.5f);

		affectedPlayers.forEach(player -> {
			Vector pullVector = center.toVector().subtract(player.getLocation().toVector());

			if (pullVector.lengthSquared() < 0.0001) return;

			pullVector.normalize().multiply(profile.pullPower).setY(0.22);

			player.setVelocity(player.getVelocity().multiply(0.45).add(pullVector));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 28, 1, false, true, true));
		});

		if (preferredTarget != null && affectedPlayers.contains(preferredTarget)) {
			dealBossDamage(boss, preferredTarget, profile == CombatProfile.INFERNAL ? 2.8 : 2.0);
		}

		return true;
	}

	private boolean violentAttractionPattern(WitherSkeleton boss, CombatProfile profile) {
		World world = boss.getWorld();
		Location center = boss.getLocation();

		List<Player> farPlayers = getArenaTargets(world, center, RANGED_DIST_SQ).stream()
			.filter(player -> player.getLocation().distanceSquared(center) >= FORCE_PULL_DIST_SQ)
			.collect(Collectors.toList());

		if (farPlayers.isEmpty()) return false;

		spawnGroundRing(center, 3.4, Particle.PORTAL, 34);
		world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 1.0, 0), 80, 2.2, 0.6, 2.2, 0.03);
		world.playSound(center, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.35f);
		world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.2f, 0.55f);

		double violentPull = (profile.pullPower * 2.4) + 0.55;
		for (Player player : farPlayers) {
			Vector pullVector = center.toVector().subtract(player.getLocation().toVector());
			if (pullVector.lengthSquared() < 0.0001) continue;

			pullVector.normalize().multiply(violentPull).setY(0.42);
			player.setVelocity(player.getVelocity().multiply(0.15).add(pullVector));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 3, false, true, true));
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 45, 1, false, true, true));
		}

		Player draggedPlayer = farPlayers.stream().max((p1, p2) -> Double.compare(
			p1.getLocation().distanceSquared(center),
			p2.getLocation().distanceSquared(center)
		)).orElse(null);

		if (draggedPlayer != null) {
			double burstDamage = profile == CombatProfile.INFERNAL ? 5.5 : 3.5;
			dealBossDamage(boss, draggedPlayer, burstDamage);
		}

		return true;
	}

	private void updateDistantPressure(WitherSkeleton boss) {
		Player focusTarget = selectFocusTarget(boss, boss.getWorld(), boss.getLocation(), FOCUS_DIST_SQ);
		if (focusTarget == null) {
			distantTicks = 0;
			return;
		}

		double distanceSquared = focusTarget.getLocation().distanceSquared(boss.getLocation());
		if (distanceSquared >= FORCE_PULL_DIST_SQ) {
			distantTicks++;
		} else {
			distantTicks = 0;
		}
	}

	private void handleStuckMovement(WitherSkeleton boss) {
		Location currentLocation = boss.getLocation();
		if (lastStuckCheckLocation == null || !currentLocation.getWorld().equals(lastStuckCheckLocation.getWorld())) {
			lastStuckCheckLocation = currentLocation.clone();
			stuckTicks = 0;
			return;
		}

		double movedSquared = currentLocation.distanceSquared(lastStuckCheckLocation);
		lastStuckCheckLocation = currentLocation.clone();

		Player focusTarget = selectFocusTarget(boss, boss.getWorld(), currentLocation, FOCUS_DIST_SQ);
		boolean farFromFight = focusTarget != null && focusTarget.getLocation().distanceSquared(currentLocation) >= FORCE_PULL_DIST_SQ;
		boolean inLiquid = currentLocation.getBlock().isLiquid();

		if (movedSquared <= STUCK_MOVE_EPSILON_SQ && (boss.isOnGround() || inLiquid || farFromFight)) {
			stuckTicks++;
		} else {
			stuckTicks = 0;
		}

		if (stuckTicks < STUCK_TICKS_THRESHOLD) return;

		Player pullTarget = focusTarget;
		if (pullTarget == null) {
			pullTarget = getGame().pickRandomArenaPlayer();
		}

		if (pullTarget != null) {
			Location reposition = pullTarget.getLocation().clone().add(
				ThreadLocalRandom.current().nextDouble(-1.6, 1.6),
				0,
				ThreadLocalRandom.current().nextDouble(-1.6, 1.6)
			);
			getGame().moveToHighestGround(reposition);

			if (reposition.distanceSquared(currentLocation) < 4.0) {
				reposition.add(
					ThreadLocalRandom.current().nextDouble(-3.5, 3.5),
					0,
					ThreadLocalRandom.current().nextDouble(-3.5, 3.5)
				);
				getGame().moveToHighestGround(reposition);
			}

			boss.teleport(reposition);
			boss.getWorld().spawnParticle(Particle.CLOUD, reposition.clone().add(0, 0.2, 0), 25, 0.35, 0.2, 0.35, 0.02);
			boss.getWorld().playSound(reposition, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.65f);

			Vector chaseVector = pullTarget.getLocation().toVector().subtract(reposition.toVector());
			if (chaseVector.lengthSquared() > 0.0001) {
				boss.setVelocity(chaseVector.normalize().multiply(0.65).setY(0.18));
			}
		} else {
			Vector boost = boss.getVelocity().clone();
			boost.setY(Math.max(0.55, boost.getY() + 0.4));
			boss.setVelocity(boost);
		}

		stuckTicks = 0;
	}

	@Nullable
	private Player selectFocusTarget(WitherSkeleton boss, World world, Location center, double maxDistanceSq) {
		return getArenaTargets(world, center, maxDistanceSq).stream()
			.filter(p -> !p.isDead())
			.min((p1, p2) -> {
				double dist1 = Math.sqrt(p1.getLocation().distanceSquared(center));
				double dist2 = Math.sqrt(p2.getLocation().distanceSquared(center));

				double healthRatio1 = Math.max(0.0, p1.getHealth() / Math.max(1.0, p1.getMaxHealth()));
				double healthRatio2 = Math.max(0.0, p2.getHealth() / Math.max(1.0, p2.getMaxHealth()));

				double armor1 = getArmorValue(p1);
				double armor2 = getArmorValue(p2);

				double losPenalty1 = boss.hasLineOfSight(p1) ? 0.0 : LOS_PENALTY;
				double losPenalty2 = boss.hasLineOfSight(p2) ? 0.0 : LOS_PENALTY;

				double verticalPenalty1 = Math.abs(p1.getLocation().getY() - center.getY()) > VERTICAL_CHECK ? VERTICAL_PENALTY : 0.0;
				double verticalPenalty2 = Math.abs(p2.getLocation().getY() - center.getY()) > VERTICAL_CHECK ? VERTICAL_PENALTY : 0.0;

				double score1 = (dist1 * 1.25) + (healthRatio1 * 9.5) + (armor1 * 0.35) + losPenalty1 + verticalPenalty1;
				double score2 = (dist2 * 1.25) + (healthRatio2 * 9.5) + (armor2 * 0.35) + losPenalty2 + verticalPenalty2;

				return Double.compare(score1, score2);
			}).orElse(null);
	}

	private double getArmorValue(Player player) {
		AttributeInstance armorAttribute = player.getAttribute(Attribute.GENERIC_ARMOR);
		return armorAttribute != null ? armorAttribute.getValue() : 0.0;
	}

	private void enforceWaterStride(WitherSkeleton boss) {
		Location current = boss.getLocation();
		boolean inLiquid = current.getBlock().isLiquid() || current.clone().add(0, 0.6, 0).getBlock().isLiquid();

		if (!inLiquid) return;

		Vector velocity = boss.getVelocity();
		if (velocity.getY() < 0.05) {
			velocity.setY(0.09);
			boss.setVelocity(velocity);
		}

		boss.setFallDistance(0.0f);

		boss.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 30, 0, false, false, false));
		boss.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 30, 0, false, false, false));

		boss.getWorld().spawnParticle(Particle.BUBBLE_COLUMN_UP, current.clone().add(0, 0.15, 0), 6, 0.22, 0.05, 0.22, 0.01);
	}

	private void spawnAmbientAura(WitherSkeleton boss) {
		Location auraCenter = boss.getLocation().clone().add(0, 1.05, 0);
		CombatProfile profile = resolveProfile(boss);
		int particleCount = profile == CombatProfile.DODGEABLE ? 3 : profile == CombatProfile.DENSE ? 6 : 9;

		boss.getWorld().spawnParticle(Particle.SOUL, auraCenter, particleCount, 0.35, 0.45, 0.35, 0.01);
		if (profile == CombatProfile.INFERNAL) {
			boss.getWorld().spawnParticle(Particle.DRAGON_BREATH, auraCenter, 2, 0.28, 0.35, 0.28, 0.01);
		}
	}

	private void spawnGroundRing(Location center, double radius, Particle particle, int points) {
		World world = center.getWorld();
		if (world == null) return;

		for (int i = 0; i < points; i++) {
			double angle = (Math.PI * 2 * i) / points;
			double x = center.getX() + (Math.cos(angle) * radius);
			double z = center.getZ() + (Math.sin(angle) * radius);
			Location ringPoint = new Location(world, x, center.getY() + 0.05, z);

			if (particle == Particle.BLOCK_CRACK) {
				world.spawnParticle(particle, ringPoint, 1, 0.0, 0.0, 0.0, 0.0, Material.DEEPSLATE.createBlockData());
			} else {
				world.spawnParticle(particle, ringPoint, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
	}

	private void orbitingOrbsPattern(WitherSkeleton boss) {
		if (orbitTask != null && !orbitTask.isCancelled()) return;
		cleanupOrbs();

		World world = boss.getWorld();
		Location center = boss.getLocation().clone().add(0, 1.2, 0);
		CombatProfile profile = resolveProfile(boss);

		for (int i = 0; i < profile.orbCount; i++) {
			ArmorStand orb = (ArmorStand) world.spawnEntity(center, EntityType.ARMOR_STAND);

			orb.setVisible(false);
			orb.setMarker(true);
			orb.setGravity(false);
			orb.setSmall(true);
			orb.setInvulnerable(true);

			orb.getEquipment().setHelmet(new ItemStack(Material.FIRE_CHARGE));

			activeOrbs.add(orb);
		}

		world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.9f, 1.4f);

		orbitTask = getGame().getPlugin().getServer().getScheduler().runTaskTimer(getGame().getPlugin(), new Runnable() {
			private int ticks = 0;
			private double angle = 0.0;

			@Override
			public void run() {
				if (!isAlive() || activeOrbs.isEmpty()) {
					cleanupOrbs();
					return;
				}

				Location bossCenter = boss.getLocation().clone().add(0, 1.2, 0);
				double orbitRadius = 1.8 + (0.3 * Math.sin(angle * 1.4));

				for (int i = 0; i < activeOrbs.size(); i++) {
					ArmorStand currentOrb = activeOrbs.get(i);

					double orbAngle = angle + (i * (Math.PI * 2 / activeOrbs.size()));

					Location orbLocation = bossCenter.clone().add(
						Math.cos(orbAngle) * orbitRadius,
						0.25 * Math.sin(angle * 2.0),
						Math.sin(orbAngle) * orbitRadius
					);

					currentOrb.teleport(orbLocation);
					boss.getWorld().spawnParticle(Particle.SOUL, orbLocation, 2, 0.02, 0.02, 0.02, 0.0);
				}

				if (ticks % profile.orbVolleyInterval == 0) {
					fireVolleyFromOrbs(boss, world, bossCenter, profile);
				}

				angle += 0.35;
				ticks++;

				if (ticks >= profile.orbPatternTicks) {
					explodeOrbs(boss);
					cleanupOrbs();
				}
			}
		}, 0L, 1L);
	}

	private void fireVolleyFromOrbs(WitherSkeleton boss, World world, Location bossLoc, CombatProfile profile) {
		java.util.List<Player> targets = getArenaTargets(world, bossLoc, 900);
		targets.removeIf(p -> p.getLocation().distanceSquared(bossLoc) <= RANGED_MIN_DIST_SQ);

		if (targets.isEmpty()) return;

		java.util.Collections.shuffle(targets);
		int shotsPerOrb = profile == CombatProfile.DODGEABLE ? 1 : profile == CombatProfile.DENSE ? 2 : 3;

		world.playSound(bossLoc, Sound.ITEM_FIRECHARGE_USE, 1.0f, profile == CombatProfile.INFERNAL ? 0.62f : 0.84f);

		int targetCount = 0;
		for (ArmorStand orb : activeOrbs) {
			if (orb == null || !orb.isValid()) continue;

			for (int shot = 0; shot < shotsPerOrb; shot++) {
				Player target = targets.get(targetCount % targets.size());
				targetCount++;

				Location orbPos = orb.getLocation().clone().add(0.0, 0.15, 0.0);
				Vector direction = target.getEyeLocation().toVector().subtract(orbPos.toVector());

				if (direction.lengthSquared() < 0.0001) continue;

				direction.normalize().multiply(profile.fireballSpeed);

				SmallFireball fireball = (SmallFireball) world.spawnEntity(orbPos, EntityType.SMALL_FIREBALL);

				fireball.setShooter(boss);
				fireball.setDirection(direction);
				fireball.setVelocity(direction);
				fireball.setIsIncendiary(true);
				fireball.setYield(0.0f);

				world.spawnParticle(Particle.FLAME, orbPos, 3, 0.02, 0.02, 0.02, 0.01);
			}
		}
	}

	private void processIncomingAggro(WitherSkeleton boss) {
		EntityDamageEvent damageEvent = boss.getLastDamageCause();
		if (damageEvent == null || damageEvent == lastProcessedDamageEvent) return;

		lastProcessedDamageEvent = damageEvent;
		Player attacker = null;

		if (damageEvent instanceof EntityDamageByEntityEvent damageByEntity) {
			if (damageByEntity.getDamager() instanceof Player player) {
				attacker = player;
			} else if (damageByEntity.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
				attacker = shooter;
			}
		}

		if (attacker == null || !getGame().isArenaPlayer(attacker)) return;

		double distance = Math.sqrt(attacker.getLocation().distanceSquared(boss.getLocation()));
		double totalAggro = damageEvent.getFinalDamage() * (distance > 8.0 ? 1.45 : 1.0);
		aggroScores.merge(attacker.getUniqueId(), totalAggro, Double::sum);
	}

	private void decayAggroScores() {
		if (aggroScores.isEmpty()) return;

		aggroScores.replaceAll((id, score) -> score * 0.992);
		aggroScores.entrySet().removeIf(e -> e.getValue() < 0.12);
	}

	@org.jetbrains.annotations.Nullable
	private Player selectRangedAggroTarget(WitherSkeleton boss, World world, Location center, double maxDistanceSquared) {
		return getArenaTargets(world, center, maxDistanceSquared).stream()
			.filter(p -> !p.isDead() && p.getLocation().distanceSquared(center) > RANGED_MIN_DIST_SQ)
			.max((p1, p2) -> Double.compare(
				aggroScores.getOrDefault(p1.getUniqueId(), 0.0),
				aggroScores.getOrDefault(p2.getUniqueId(), 0.0)))
			.filter(p -> aggroScores.getOrDefault(p.getUniqueId(), 0.0) >= 2.0)
			.orElse(null);
	}

	private void dealBossDamage(WitherSkeleton boss, Player player, double amount) {
		player.damage(amount, boss);
		ticksSinceBossHitAPlayer = 0;
	}

	private void explodeOrbs(WitherSkeleton boss) {
		World world = boss.getWorld();
		CombatProfile profile = resolveProfile(boss);
		java.util.Set<UUID> damagedPlayers = new java.util.HashSet<>();

		for (ArmorStand orb : activeOrbs) {
			Location orbLoc = orb.getLocation();

			world.spawnParticle(Particle.EXPLOSION_LARGE, orbLoc, 1, 0.0, 0.0, 0.0, 0.0);
			world.playSound(orbLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.4f);

			getArenaTargets(world, orbLoc, 12.25).stream()
				.filter(player -> damagedPlayers.add(player.getUniqueId()))
				.forEach(player -> dealBossDamage(boss, player, profile.explosionDamage));
		}
	}

	private void cleanupOrbs() {
		if (orbitTask != null) {
			orbitTask.cancel();
			orbitTask = null;
		}
		for (ArmorStand orb : activeOrbs) {
			if (orb != null && orb.isValid()) {
				orb.remove();
			}
		}
		activeOrbs.clear();
	}

	private CombatProfile resolveProfile(WitherSkeleton boss) {
		double healthRatio = boss.getHealth() / MAX_HEALTH;

		return healthRatio > 0.33 ? CombatProfile.DODGEABLE :
		       healthRatio > 0.18 ? CombatProfile.DENSE :
		       CombatProfile.INFERNAL;
	}

	private List<Player> getArenaTargets(World world, Location center, double maxDistanceSquared) {
		return world.getPlayers().stream()
			.filter(player -> isArenaTarget(player, world, center, maxDistanceSquared))
			.collect(Collectors.toList());
	}
}
