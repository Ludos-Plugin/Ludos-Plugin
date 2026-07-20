package fr.ludos.games.raid.monsters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.monster.SpecialMonster;

/**
 * Base class for raid boss monster implementations.
 * @param <TEntity> Specific type of the underlying {@link LivingEntity} of the Monster.
 */
public abstract class RaidMonsterBoss<TEntity extends LivingEntity> extends SpecialMonster<TEntity> {
	/**
	 * Elemental types for raid boss mechanics.
	 */
	public enum Element {
		EARTH,
		WATER,
		FIRE
	}

	private static final double DEFAULT_LOS_PENALTY = 5.5;
	private static final double DEFAULT_VERTICAL_PENALTY = 3.0;
	private static final double DEFAULT_VERTICAL_CHECK = 4.0;

	private final Element element;
	private final int totalPhases;
	private final Map<UUID, Double> aggroScores = new HashMap<>();

	@Nullable
	private BossBar bossBar;

	@Nullable
	private EntityDamageEvent lastProcessedDamageEvent;

	protected RaidMonsterBoss(String typeId, Game game) {
		this(typeId, game, Element.EARTH, 3);
	}

	protected RaidMonsterBoss(String typeId, Game game, Element element) {
		this(typeId, game, element, 3);
	}

	protected RaidMonsterBoss(String typeId, Game game, Element element, int totalPhases) {
		super(typeId, game);
		this.element = element != null ? element : Element.EARTH;
		this.totalPhases = Math.max(1, totalPhases);
	}

	public Element getElement() {
		return element;
	}

	public int getTotalPhases() {
		return totalPhases;
	}

	protected final int resolvePhaseFromHealth(double currentHealth, double maxHealth) {
		double safeMaxHealth = Math.max(1.0, maxHealth);
		double healthRatio = Math.max(0.0, Math.min(1.0, currentHealth / safeMaxHealth));
		int phaseBucket = (int) ((1.0 - healthRatio) * totalPhases);

		return Math.min(totalPhases, Math.max(1, phaseBucket + 1));
	}

	protected final List<Player> getArenaTargets(World world, Location center, double maxDistanceSquared) {
		return world.getPlayers().stream()
			.filter(player -> isArenaTarget(player, world, center, maxDistanceSquared))
			.collect(Collectors.toList());
	}

	@Nullable
	protected final Player selectFocusTarget(LivingEntity boss, World world, Location center, double maxDistanceSq) {
		return getArenaTargets(world, center, maxDistanceSq).stream()
			.filter(player -> !player.isDead() && player.getGameMode() != GameMode.SPECTATOR)
			.min((p1, p2) -> {
				double dist1 = Math.sqrt(p1.getLocation().distanceSquared(center));
				double dist2 = Math.sqrt(p2.getLocation().distanceSquared(center));

				double healthRatio1 = Math.max(0.0, p1.getHealth() / Math.max(1.0, p1.getMaxHealth()));
				double healthRatio2 = Math.max(0.0, p2.getHealth() / Math.max(1.0, p2.getMaxHealth()));

				double armor1 = getArmorValue(p1);
				double armor2 = getArmorValue(p2);

				double losPenalty1 = boss.hasLineOfSight(p1) ? 0.0 : DEFAULT_LOS_PENALTY;
				double losPenalty2 = boss.hasLineOfSight(p2) ? 0.0 : DEFAULT_LOS_PENALTY;

				double verticalPenalty1 = Math.abs(p1.getLocation().getY() - center.getY()) > DEFAULT_VERTICAL_CHECK ? DEFAULT_VERTICAL_PENALTY : 0.0;
				double verticalPenalty2 = Math.abs(p2.getLocation().getY() - center.getY()) > DEFAULT_VERTICAL_CHECK ? DEFAULT_VERTICAL_PENALTY : 0.0;

				double score1 = (dist1 * 1.25) + (healthRatio1 * 9.5) + (armor1 * 0.35) + losPenalty1 + verticalPenalty1;
				double score2 = (dist2 * 1.25) + (healthRatio2 * 9.5) + (armor2 * 0.35) + losPenalty2 + verticalPenalty2;

				return Double.compare(score1, score2);
			})
			.orElse(null);
	}

	protected final void processIncomingAggro(LivingEntity boss) {
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

		if (attacker == null || !getGame().getTeamController().contains(attacker)) return;

		double distance = Math.sqrt(attacker.getLocation().distanceSquared(boss.getLocation()));
		double totalAggro = damageEvent.getFinalDamage() * (distance > 8.0 ? 1.45 : 1.0);
		aggroScores.merge(attacker.getUniqueId(), totalAggro, Double::sum);
	}

	protected final void decayAggroScores() {
		if (aggroScores.isEmpty()) return;

		aggroScores.replaceAll((id, score) -> score * 0.992);
		aggroScores.entrySet().removeIf(entry -> entry.getValue() < 0.12);
	}

	@Nullable
	protected final Player selectRangedAggroTarget(LivingEntity boss, World world, Location center, double maxDistanceSquared, double rangedMinDistSq) {
		return getArenaTargets(world, center, maxDistanceSquared).stream()
			.filter(player -> !player.isDead() && player.getLocation().distanceSquared(center) > rangedMinDistSq)
			.max((p1, p2) -> Double.compare(
				aggroScores.getOrDefault(p1.getUniqueId(), 0.0),
				aggroScores.getOrDefault(p2.getUniqueId(), 0.0)
			))
			.filter(player -> aggroScores.getOrDefault(player.getUniqueId(), 0.0) >= 2.0)
			.orElse(null);
	}

	private double getArmorValue(Player player) {
		AttributeInstance armorAttribute = player.getAttribute(Attribute.GENERIC_ARMOR);
		return armorAttribute != null ? armorAttribute.getValue() : 0.0;
	}

	protected final int tickCooldown(int value) {
		return value > 0 ? value - 1 : 0;
	}

	protected final boolean isArenaTarget(Player player, World world, Location center, double maxDistanceSquared) {
		if (!getGame().getTeamController().contains(player)) return false;
		if (!player.getWorld().equals(world)) return false;
		return player.getLocation().distanceSquared(center) <= maxDistanceSquared;
	}

	protected final boolean teleportNearArenaTarget(LivingEntity entity, Player target, double radius, boolean snapToGround) {
		if (entity == null || target == null) return false;
		if (!entity.isValid() || entity.isDead()) return false;
		if (!target.isOnline() || target.isDead()) return false;
		if (!target.getWorld().equals(entity.getWorld())) return false;

		Location destination = target.getLocation().clone().add(
			ThreadLocalRandom.current().nextDouble(-radius, radius),
			0.0,
			ThreadLocalRandom.current().nextDouble(-radius, radius)
		);

		if (snapToGround) {
			Utility.snapToHighestY(destination, true);
		}

		return entity.teleport(destination);
	}

	protected void dealBossDamage(LivingEntity boss, Player player, double amount) {
		player.damage(amount, boss);
	}

	@Nullable
	protected final Player resolveAttacker(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player player) {
			return player;
		}
		if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
			return shooter;
		}
		return null;
	}

	protected final void initBossBar(String name, BarColor color, BarStyle style) {
		disposeBossBar();
		bossBar = Bukkit.createBossBar(name, color, style);
		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			bossBar.addPlayer(player);
		}
		bossBar.setVisible(true);
	}

	protected final void refreshBossBar(String name, double currentHealth, double maxHealth) {
		if (bossBar == null) return;
		double progress = Math.max(0.0, Math.min(1.0, currentHealth / Math.max(1.0, maxHealth)));
		bossBar.setProgress(progress);
		bossBar.setTitle(name + " - " + (int) Math.ceil(Math.max(0.0, currentHealth)) + " HP");
	}

	protected final void disposeBossBar() {
		if (bossBar == null) return;
		bossBar.removeAll();
		bossBar.setVisible(false);
		bossBar = null;
	}
}
