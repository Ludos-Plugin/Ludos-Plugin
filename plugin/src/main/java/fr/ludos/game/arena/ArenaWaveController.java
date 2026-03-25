package fr.ludos.game.arena;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.Utility;
import fr.ludos.game.arena.monster.GoldenKnightBoss;
import fr.ludos.item.Categories;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ArenaWaveController extends ArenaGame {
	private static final int BOSS_START_WAVE = 1;
	private static final int BOSS_INTERVAL = 10;
	private static final int BASE_WAVE_POINTS = 200;
	private static final int MAX_WAVE_POINTS = 48000;
	private static final int MIN_MOBS_NON_BOSS_WAVE = 11;
	private static final int MAX_MOB_COUNT = 100;
	private static final int STEAK_REWARD_PER_BOSS_WAVE = 64;

	private static final List<WaveUnit> WAVE_UNITS = List.of(
		new WaveUnit(EntityType.ZOMBIE, 12, 30, false),
		new WaveUnit(EntityType.HUSK, 13, 24, false),
		new WaveUnit(EntityType.SKELETON, 14, 22, false),
		new WaveUnit(EntityType.STRAY, 17, 14, false),
		new WaveUnit(EntityType.SPIDER, 12, 18, false),
		new WaveUnit(EntityType.CAVE_SPIDER, 18, 12, false),
		new WaveUnit(EntityType.CREEPER, 22, 15, false),
		new WaveUnit(EntityType.ENDERMAN, 38, 9, false),
		new WaveUnit(EntityType.BLAZE, 44, 8, false),
		new WaveUnit(EntityType.MAGMA_CUBE, 36, 9, false),
		new WaveUnit(EntityType.SLIME, 20, 14, false),
		new WaveUnit(EntityType.PILLAGER, 26, 20, false),
		new WaveUnit(EntityType.VINDICATOR, 34, 14, false),
		new WaveUnit(EntityType.WITCH, 42, 10, false),
		new WaveUnit(EntityType.PIGLIN_BRUTE, 92, 4, false),
		new WaveUnit(EntityType.EVOKER, 88, 6, false),
		new WaveUnit(EntityType.VEX, 52, 6, false),
		new WaveUnit(EntityType.RAVAGER, 140, 3, false),
		new WaveUnit(EntityType.HOGLIN, 62, 5, false),
		new WaveUnit(EntityType.ZOGLIN, 84, 4, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	private final Set<UUID> aliveWaveMonsters = new HashSet<>();
	private GoldenKnightBoss currentBoss;
	private int wave = 0;
	private int bossesDefeated = 0;
	private boolean bossWaveActive = false;
	private boolean preparationPhase = false;
	private BukkitTask preparationTask;
	private BukkitTask waveCheckTask;

	protected ArenaWaveController(Builder builder) {
		super(builder);
	}

	@Override
	protected void onGameStart() {
		startNextWave();
	}

	@Override
	protected void onGameStop() {
		preparationTask = Utility.cancelTask(preparationTask);
		waveCheckTask = Utility.cancelTask(waveCheckTask);
		despawnAllMonsters();
		getGameAreaController().resetBorder();
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		return isArenaPlayer(player);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!isArenaPlayer(event.getPlayer())) return;
		if (!preparationPhase) return;

		if (isSamePosition(event.getFrom(), event.getTo())) return;

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!isArenaPlayer(player)) return;
		if (!preparationPhase) return;

		event.setCancelled(true);
	}


	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!isArenaPlayer(player)) return;
		if (!preparationPhase) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		// Entity entity = event.getEntity();
		// event.getDrops().clear();

		// if (aliveWaveMonsters.remove(entity.getUniqueId())) {
		// 	event.getDrops().clear();
		// 	event.setDroppedExp(0);
		// }
		// if (currentBoss != null && currentBoss.getEntity() != null && currentBoss.getEntity().getUniqueId().equals(entity.getUniqueId())) {
		// 	currentBoss.onDeath();
		// 	currentBoss = null;
		// }
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!isArenaPlayer(event.getPlayer())) return;
		Bukkit.getScheduler().runTask(getPlugin(), this::evaluateWaveState);
	}

	private void startNextWave() {
		if (wave >= getConfiguredRounds()) {
			Bukkit.broadcast(Component.text("Arena Waves complete!").color(NamedTextColor.GOLD));
			Game.stopCurrentGame();

			return;
		}

		wave++;

		startWavePreparation();
	}

	private void startWavePreparation() {
		preparationPhase = true;
		teleportArenaPlayersForRound();

		for (Player player : getArenaPlayers()) {
			Game.joinAnyPlayer(player, null);
			player.getInventory().addItem(Ludos.createGuidebook());
		}

		preparationTask = startPreparationCountdownTask(
			"Wave",
			() -> wave,
			"Wave starts in",
			this::startWaveCombat
		);
	}

	private void startWaveCombat() {
		preparationPhase = false;
		preparationTask = Utility.cancelTask(preparationTask);

		for (Player player : getArenaPlayers()) {
			applyArenaCombatLoadout(player);
		}

		spawnWaveContent();
		waveCheckTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), this::evaluateWaveState, 0L, 10L);
	}

	private void spawnWaveContent() {
		aliveWaveMonsters.clear();
		bossWaveActive = shouldSpawnBossWave();

		if (bossWaveActive) {
			spawnBossWave();
			return;
		}

		int pointsBudget = computeWavePointsBudget();
		List<WaveUnit> roster = composeWaveRoster(pointsBudget);
		Location center = getGameAreaController().getCenter();

		for (WaveUnit unit : roster) {
			Location spawn = center.clone().add(
				ThreadLocalRandom.current().nextDouble(-18, 18),
				0,
				ThreadLocalRandom.current().nextDouble(-18, 18)
			);
			moveToHighestGround(spawn);

			LivingEntity mob = spawnRegularMob(spawn, unit.type());

			if (mob == null) continue;

			configureWaveMob(mob, unit);

			aliveWaveMonsters.add(mob.getUniqueId());
		}

		Bukkit.broadcast(Component.text(
			"Wave " + wave + " spawned: " + aliveWaveMonsters.size() + " monsters (" + pointsBudget + " pts)"
		).color(NamedTextColor.DARK_RED));
	}

	private void spawnBossWave() {
		Location center = getGameAreaController().getCenter().clone();
		moveToHighestGround(center);
		center.add(0, 1.0, 0);

		grantBossWaveCombatSupplies();

		currentBoss = new GoldenKnightBoss(this);

		currentBoss.spawn(center);

		if (currentBoss.getEntity() != null) {
			aliveWaveMonsters.add(currentBoss.getEntity().getUniqueId());
		}

		Bukkit.broadcast(Component.text("Boss Wave: The Golden Knight emerges").color(NamedTextColor.DARK_PURPLE));

		center.getWorld().strikeLightningEffect(center);
		center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
	}

	private void grantBossWaveCombatSupplies() {
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated + 1));

		for (Player player : getArenaPlayers()) {
			if (!player.isOnline()) continue;
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		Bukkit.broadcast(Component.text(
			"Boss combat supplies delivered: 64 steaks + " + goldenApples + " golden apples"
		).color(NamedTextColor.GOLD));
	}

	private LivingEntity spawnRegularMob(Location location, EntityType type) {
		Entity entity = location.getWorld().spawnEntity(location, type);
		if (!(entity instanceof LivingEntity living)) {
			entity.remove();
			return null;
		}

		return living;
	}

	private void configureWaveMob(LivingEntity mob, WaveUnit unit) {
		List<Player> alivePlayers = getAliveArenaPlayers();
		if (mob instanceof Monster monster && !alivePlayers.isEmpty()) {
			monster.setTarget(alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size())));
		}

		mob.setRemoveWhenFarAway(false);
		mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));

		if (mob instanceof Zombie zombie) {
			zombie.setShouldBurnInDay(false);
		}

		if (mob instanceof AbstractSkeleton skeleton) {
			skeleton.setShouldBurnInDay(false);
		}

		double hp = 18.0 + (wave * 2.25);
		if (unit.bossEcho()) hp *= 3.4;

		setBaseValue(mob, Attribute.GENERIC_MAX_HEALTH, hp);
		if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
			mob.setHealth(hp);
		}

		double damage = 3.5 + Math.min(12.0, wave * 0.42);
		if (unit.bossEcho()) damage *= 2.2;
		setBaseValue(mob, Attribute.GENERIC_ATTACK_DAMAGE, damage);

		double speed = 0.25 + Math.min(0.13, wave * 0.004);
		if (unit.bossEcho()) speed += 0.05;
		setBaseValue(mob, Attribute.GENERIC_MOVEMENT_SPEED, speed);

		double kbRes = Math.min(0.65, wave * 0.015);
		setBaseValue(mob, Attribute.GENERIC_KNOCKBACK_RESISTANCE, kbRes);

		applyScaledEquipment(mob);

		if (unit.bossEcho()) {
			mob.customName(Component.text("Echo of Dark Knight").color(NamedTextColor.DARK_PURPLE));
			mob.setCustomNameVisible(true);
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2, true, false));
			mob.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, true, false));
		}

		if (wave >= 10) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, Math.min(2, wave / 15), true, false));
		}

		mob.getWorld().spawnParticle(Particle.SMOKE_LARGE, mob.getLocation(), 12, 0.3, 0.4, 0.3, 0.01);
		mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 0.7f);
	}

	private void applyScaledEquipment(LivingEntity mob) {
		EntityEquipment equipment = mob.getEquipment();
		if (equipment == null) return;

		double armorChance = Math.min(0.95, 0.2 + wave * 0.03);
		double weaponChance = Math.min(0.95, 0.25 + wave * 0.035);
		int tier = Math.min(4, Math.max(0, (wave - 1) / 5));

		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setHelmet(createArmor(tier, Categories.Group.HELMETS));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setChestplate(createArmor(tier, Categories.Group.CHESTPLATES));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setLeggings(createArmor(tier, Categories.Group.LEGGINGS));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setBoots(createArmor(tier, Categories.Group.BOOTS));

		if (ThreadLocalRandom.current().nextDouble() < weaponChance) {
			ItemStack weapon = createWeapon(tier);
			if (weapon != null) equipment.setItemInMainHand(weapon);
		}

		equipment.setHelmetDropChance(0f);
		equipment.setChestplateDropChance(0f);
		equipment.setLeggingsDropChance(0f);
		equipment.setBootsDropChance(0f);
		equipment.setItemInMainHandDropChance(0f);
	}

	private ItemStack createArmor(int tier, Categories.Group armorGroup) {
		String suffix = switch (armorGroup) {
			case HELMETS -> "HELMET";
			case CHESTPLATES -> "CHESTPLATE";
			case LEGGINGS -> "LEGGINGS";
			case BOOTS -> "BOOTS";
			default -> throw new IllegalArgumentException("Unsupported armor group: " + armorGroup);
		};

		String prefix = switch (Math.min(4, Math.max(0, tier))) {
			case 0 -> "LEATHER";
			case 1 -> "CHAINMAIL";
			case 2 -> "IRON";
			case 3 -> "DIAMOND";
			default -> "NETHERITE";
		};

		Material material = Material.matchMaterial(prefix + '_' + suffix);
		if (material == null || !Categories.is(armorGroup, material)) {
			material = Categories.get(armorGroup).stream().findFirst().orElse(Material.LEATHER_HELMET);
		}

		ItemStack item = new ItemStack(material);
		int protection = Math.min(4, 1 + wave / 8);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.9, 0.15 + wave * 0.03)) {
			item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, protection);
		}
		return item;
	}

	@Nullable
	private ItemStack createWeapon(int tier) {
		Categories.Group weaponGroup = ThreadLocalRandom.current().nextBoolean()
			? Categories.Group.SWORDS
			: Categories.Group.AXES;

		Material picked = resolveTierWeaponMaterial(tier, weaponGroup);

		if (picked == null) return null;

		ItemStack weapon = new ItemStack(picked);

		int sharpness = Math.min(5, 1 + wave / 10);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.95, 0.2 + wave * 0.03)) {
			weapon.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, sharpness);
		}

		return weapon;
	}

	@Nullable
	private Material resolveTierWeaponMaterial(int tier, Categories.Group weaponGroup) {
		String suffix = switch (weaponGroup) {
			case SWORDS -> "SWORD";
			case AXES -> "AXE";
			default -> throw new IllegalArgumentException("Unsupported weapon group: " + weaponGroup);
		};

		String prefix = switch (Math.min(4, Math.max(0, tier))) {
			case 0 -> ThreadLocalRandom.current().nextBoolean() ? "WOODEN" : "STONE";
			case 1 -> ThreadLocalRandom.current().nextBoolean() ? "STONE" : "IRON";
			case 2 -> ThreadLocalRandom.current().nextBoolean() ? "IRON" : "DIAMOND";
			case 3 -> "DIAMOND";
			default -> "NETHERITE";
		};

		Material material = Material.matchMaterial(prefix + '_' + suffix);
		if (material == null || !Categories.is(weaponGroup, material)) {
			return Categories.get(weaponGroup).stream().findFirst().orElse(null);
		}

		return material;
	}

	private void evaluateWaveState() {
		List<Player> alivePlayers = getAliveArenaPlayers();
		if (alivePlayers.isEmpty()) {
			Bukkit.broadcast(Component.text("Arena failed: all players are down").color(NamedTextColor.RED));
			Game.stopCurrentGame();
			return;
		}
		retargetAliveMonsters(alivePlayers);
		aliveWaveMonsters.removeIf(id -> {
			Entity entity = Bukkit.getEntity(id);
			return !(entity instanceof LivingEntity living) || living.isDead() || !living.isValid();
		});
		if (aliveWaveMonsters.isEmpty()) {
			waveCheckTask = Utility.cancelTask(waveCheckTask);

			if (bossWaveActive && currentBoss != null) {
				onBossWaveCleared();
				currentBoss = null;
				bossWaveActive = false;
			}

			Bukkit.broadcast(Component.text("Wave " + wave + " cleared").color(NamedTextColor.GREEN));
			Bukkit.getScheduler().runTaskLater(getPlugin(), this::startNextWave, 40L);
		}
	}

	private int computeWavePointsBudget() {
		double budget = BASE_WAVE_POINTS * Math.pow(2, Math.max(0, wave - 1));
		return (int) Math.min(MAX_WAVE_POINTS, budget);
	}

	private List<WaveUnit> composeWaveRoster(int pointsBudget) {
		List<WaveUnit> roster = new ArrayList<>();
		int remaining = pointsBudget;
		int guard = 0;

		while (remaining >= 12 && guard++ < 2000 && roster.size() < MAX_MOB_COUNT) {
			WaveUnit picked = pickWeightedUnit(remaining);
			if (picked == null) break;
			roster.add(picked);
			remaining -= picked.cost();
		}

		while (roster.size() < MIN_MOBS_NON_BOSS_WAVE && roster.size() < MAX_MOB_COUNT) {
			WaveUnit filler = pickWeightedUnit(20);
			if (filler == null) {
				filler = new WaveUnit(EntityType.ZOMBIE, 12, 1, false);
			}
			roster.add(filler);
		}

		return roster;
	}

	@Nullable
	private WaveUnit pickWeightedUnit(int remainingPoints) {
		List<WaveUnit> eligible = WAVE_UNITS.stream()
			.filter(unit -> unit.cost() <= remainingPoints)
			.filter(unit -> !unit.bossEcho() || bossesDefeated > 0)
			.collect(java.util.stream.Collectors.toList());

		if (eligible.isEmpty()) return null;

		int totalWeight = eligible.stream().mapToInt(WaveUnit::weight).sum();
		int roll = ThreadLocalRandom.current().nextInt(totalWeight);
		int current = 0;

		for (WaveUnit unit : eligible) {
			current += unit.weight();
			if (roll < current) return unit;
		}

		return eligible.get(eligible.size() - 1);
	}

	private void onBossWaveCleared() {
		bossesDefeated++;
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated));

		for (Player player : getArenaPlayers()) {
			if (!player.isOnline()) continue;
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		Bukkit.broadcast(Component.text(
			"Boss defeated! Rewards: 64 steaks + " + goldenApples + " golden apples per player"
		).color(NamedTextColor.GOLD));
	}

	private void retargetAliveMonsters(List<Player> alivePlayers) {
		if (alivePlayers.isEmpty()) return;

		for (UUID id : aliveWaveMonsters) {
			Entity entity = Bukkit.getEntity(id);

			if (!(entity instanceof Monster monster)) continue;

			if (monster.isDead() || !monster.isValid()) continue;

			Player target = alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size()));
			monster.setTarget(target);
		}
	}

	private boolean shouldSpawnBossWave() {
		if (wave < BOSS_START_WAVE) return false;
		return ((wave - BOSS_START_WAVE) % BOSS_INTERVAL) == 0;
	}

	private void despawnAllMonsters() {
		for (UUID id : aliveWaveMonsters) {
			Entity entity = Bukkit.getEntity(id);
			if (entity != null) entity.remove();
		}
		aliveWaveMonsters.clear();
		if (currentBoss != null) {
			currentBoss.despawn();
			currentBoss = null;
		}
	}

	private void setBaseValue(LivingEntity entity, Attribute attribute, double value) {
		AttributeInstance instance = entity.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	private record WaveUnit(EntityType type, int cost, int weight, boolean bossEcho) { }
}
