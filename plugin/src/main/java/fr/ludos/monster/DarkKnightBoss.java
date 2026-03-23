package fr.ludos.game.arena.monster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.game.arena.ArenaGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class DarkKnightBoss extends SpecialMonster<WitherSkeleton> {
	private static final double MAX_HEALTH = 360.0;
	private static final int ATTACK_COOLDOWN_TICKS = 45;

	private int attackTick = 0;
	private int phase = 1;

	private final List<ArmorStand> activeOrbs = new ArrayList<>();

	@org.jetbrains.annotations.Nullable
	private BukkitTask orbitTask;

	public DarkKnightBoss(ArenaGame game) {
		super("dark_knight", game);
	}

	@Override
	protected WitherSkeleton createEntity(Location location) {
		World world = location.getWorld();
		if (world == null) {
			throw new IllegalStateException("Boss location world is null");
		}

		WitherSkeleton boss = (WitherSkeleton) world.spawnEntity(location, EntityType.WITHER_SKELETON);
		boss.customName(Component.text("The Dark Knight").color(NamedTextColor.DARK_PURPLE));
		boss.setCustomNameVisible(true);
		boss.setRemoveWhenFarAway(false);
		boss.setPersistent(true);

		AttributeInstance followRange = boss.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
		if (followRange != null) {
			followRange.setBaseValue(64.0);
		}
		AttributeInstance movement = boss.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		if (movement != null) {
			movement.setBaseValue(0.33);
		}
		AttributeInstance damage = boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		if (damage != null) {
			damage.setBaseValue(12.0);
		}

		setMaxHealth(boss, MAX_HEALTH);
		boss.setHealth(MAX_HEALTH);

		boss.getEquipment().setHelmet(tintedLeather(Material.LEATHER_HELMET, Color.fromRGB(22, 22, 26)));
		boss.getEquipment().setChestplate(tintedLeather(Material.LEATHER_CHESTPLATE, Color.fromRGB(12, 12, 12)));
		boss.getEquipment().setLeggings(tintedLeather(Material.LEATHER_LEGGINGS, Color.fromRGB(18, 18, 18)));
		boss.getEquipment().setBoots(tintedLeather(Material.LEATHER_BOOTS, Color.fromRGB(10, 10, 10)));
		boss.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
		boss.getEquipment().setItemInMainHandDropChance(0.0f);

		boss.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));

		return boss;
	}

	@Override
	protected void onTick(WitherSkeleton entity) {
		updatePhase(entity);

		if (attackTick > 0) {
			attackTick--;
			return;
		}

		triggerPattern(entity);
		attackTick = ATTACK_COOLDOWN_TICKS;
	}

	@Override
	protected void onDespawn(WitherSkeleton entity) {
		cleanupOrbs();
	}

	@Override
	protected void onMonsterDeath(WitherSkeleton entity) {
		cleanupOrbs();
		entity.getWorld().strikeLightningEffect(entity.getLocation());
		entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, entity.getLocation(), 3, 1.0, 0.6, 1.0, 0.0);
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.85f, 0.9f);
	}

	private void updatePhase(WitherSkeleton boss) {
		double healthRatio = boss.getHealth() / MAX_HEALTH;
		int nextPhase;
		if (healthRatio > 0.66) nextPhase = 1;
		else if (healthRatio > 0.33) nextPhase = 2;
		else nextPhase = 3;

		if (nextPhase == phase) return;
		phase = nextPhase;
		boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.7f + (0.1f * phase));
		boss.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, boss.getLocation().add(0, 1.4, 0), 40, 0.6, 0.6, 0.6, 0.02);
	}

	private void triggerPattern(WitherSkeleton boss) {
		int random = ThreadLocalRandom.current().nextInt(100);

		if (phase == 1) {
			if (random < 65) lightningPattern(boss);
			else earthShatterPattern(boss);
			return;
		}
		if (phase == 2) {
			if (random < 40) lightningPattern(boss);
			else if (random < 75) earthShatterPattern(boss);
			else orbitingOrbsPattern(boss);
			return;
		}

		if (random < 30) lightningPattern(boss);
		else if (random < 60) earthShatterPattern(boss);
		else orbitingOrbsPattern(boss);
	}

	private void lightningPattern(WitherSkeleton boss) {
		World world = boss.getWorld();
		Location center = boss.getLocation();

		for (Player player : boss.getWorld().getPlayers()) {
			if (!getGame().isArenaPlayer(player)) continue;
			if (!player.getWorld().equals(world)) continue;
			if (player.getLocation().distanceSquared(center) > 32 * 32) continue;

			Location strike = player.getLocation().clone().add(
				ThreadLocalRandom.current().nextDouble(-1.5, 1.5),
				0,
				ThreadLocalRandom.current().nextDouble(-1.5, 1.5)
			);

			world.strikeLightningEffect(strike);
			player.damage(4.0, boss);
		}

		world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
	}

	private void earthShatterPattern(WitherSkeleton boss) {
		World world = boss.getWorld();
		Location center = boss.getLocation();

		world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.65f);
		world.spawnParticle(Particle.BLOCK_CRACK, center, 80, 3.5, 0.2, 3.5, Material.DEEPSLATE.createBlockData());

		for (Player player : world.getPlayers()) {
			if (!getGame().isArenaPlayer(player)) continue;
			if (player.getLocation().distanceSquared(center) > 14 * 14) continue;

			Vector knock = player.getLocation().toVector().subtract(center.toVector());
			if (knock.lengthSquared() < 0.0001) {
				knock = new Vector(0.1, 0.0, 0.1);
			}
			knock.normalize().multiply(1.25).setY(0.75);
			player.setVelocity(knock);
			player.damage(3.0, boss);
		}
	}

	private void orbitingOrbsPattern(WitherSkeleton boss) {
		if (orbitTask != null && !orbitTask.isCancelled()) {
			return;
		}

		cleanupOrbs();

		World world = boss.getWorld();
		Location center = boss.getLocation().clone().add(0, 1.2, 0);

		for (int i = 0; i < 3; i++) {
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

				Location bossLoc = boss.getLocation().clone().add(0, 1.2, 0);
				double radius = 1.8 + (0.3 * Math.sin(angle * 1.4));

				for (int i = 0; i < activeOrbs.size(); i++) {
					ArmorStand orb = activeOrbs.get(i);
					double orbAngle = angle + (i * (Math.PI * 2 / activeOrbs.size()));
					Location loc = bossLoc.clone().add(Math.cos(orbAngle) * radius, 0.25 * Math.sin(angle * 2.0), Math.sin(orbAngle) * radius);
					orb.teleport(loc);
					boss.getWorld().spawnParticle(Particle.SOUL, loc, 2, 0.02, 0.02, 0.02, 0.0);
				}

				angle += 0.35;
				ticks++;

				if (ticks >= 80) {
					explodeOrbs(boss);
					cleanupOrbs();
				}
			}
		}, 0L, 1L);
	}

	private void explodeOrbs(WitherSkeleton boss) {
		World world = boss.getWorld();

		for (ArmorStand orb : activeOrbs) {
			Location orbLoc = orb.getLocation();
			world.spawnParticle(Particle.EXPLOSION_LARGE, orbLoc, 1, 0.0, 0.0, 0.0, 0.0);
			world.playSound(orbLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.4f);

			for (Player player : world.getPlayers()) {
				if (!getGame().isArenaPlayer(player)) continue;
				if (player.getLocation().distanceSquared(orbLoc) > 3.5 * 3.5) continue;
				player.damage(5.0, boss);
			}
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

	private ItemStack tintedLeather(Material type, Color color) {
		ItemStack item = new ItemStack(type);
		if (!(item.getItemMeta() instanceof LeatherArmorMeta meta)) {
			return item;
		}

		meta.setColor(color);
		item.setItemMeta(meta);
		return item;
	}
}
