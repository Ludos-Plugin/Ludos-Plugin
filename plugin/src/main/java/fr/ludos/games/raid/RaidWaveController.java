package fr.ludos.games.raid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.Utility;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.generator.OceanChunkGenerator;
import fr.ludos.core.item.Categories;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.wave.DefaultWaveLoadout;
import fr.ludos.core.wave.WaveController;
import fr.ludos.games.raid.monsters.GoldenKnightBoss;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Controller for {@link RaidGame} wave behavior.
 */
public final class RaidWaveController extends WaveController {
	/**
	 * Themes used for raid worlds.
	 */
	public enum WaveTheme {
		EARTH("Earth", () -> new WorldCreator("raid_earth_" + UUID.randomUUID())
			.environment(World.Environment.NORMAL)
			.type(WorldType.NORMAL)
		),
		WATER("Water", () -> new WorldCreator("raid_water_" + UUID.randomUUID())
			.environment(World.Environment.NORMAL)
			.type(WorldType.AMPLIFIED)
			.generator(new OceanChunkGenerator())
		),
		FIRE("Fire", () -> new WorldCreator("raid_fire_" + UUID.randomUUID())
			.environment(World.Environment.NETHER)
			.type(WorldType.NORMAL)
		);

		private final String display;
		public final String getDisplay() {
			return display;
		}

		private final Supplier<WorldCreator> worldCreatorBuilder;
		public final WorldCreator getWorldCreator() {
			return this.worldCreatorBuilder.get();
		}

		WaveTheme(String display, Supplier<WorldCreator> worldCreator) {
			this.display = display;
			this.worldCreatorBuilder = worldCreator;
		}

	}

	private static final int BOSS_START_WAVE = 1;
	private static final int BOSS_INTERVAL = 10;
	private static final int BASE_WAVE_POINTS = 200;
	private static final int MAX_WAVE_POINTS = 48000;
	private static final int MIN_MOBS_NON_BOSS_WAVE = 11;
	private static final int MAX_MOB_COUNT = 100;
	private static final int STEAK_REWARD_PER_BOSS_WAVE = 64;
	private static final int ENV_EFFECT_TICKS = 220;

	private static final List<WaveUnit> EARTH_WAVE_UNITS = List.of(
		new WaveUnit(EntityType.ZOMBIE, 12, 30, false),
		new WaveUnit(EntityType.HUSK, 13, 24, false),
		new WaveUnit(EntityType.DROWNED, 16, 18, false),
		new WaveUnit(EntityType.SKELETON, 14, 22, false),
		new WaveUnit(EntityType.STRAY, 17, 14, false),
		new WaveUnit(EntityType.SPIDER, 12, 18, false),
		new WaveUnit(EntityType.CAVE_SPIDER, 18, 12, false),
		new WaveUnit(EntityType.CREEPER, 22, 15, false),
		new WaveUnit(EntityType.ENDERMAN, 38, 9, false),
		new WaveUnit(EntityType.SLIME, 20, 14, false),
		new WaveUnit(EntityType.PILLAGER, 26, 20, false),
		new WaveUnit(EntityType.VINDICATOR, 34, 14, false),
		new WaveUnit(EntityType.WITCH, 42, 10, false),
		new WaveUnit(EntityType.EVOKER, 88, 6, false),
		new WaveUnit(EntityType.RAVAGER, 140, 3, false),
		new WaveUnit(EntityType.VEX, 52, 6, false),
		new WaveUnit(EntityType.HOGLIN, 62, 5, false),
		new WaveUnit(EntityType.ZOGLIN, 84, 4, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	private static final List<WaveUnit> WATER_WAVE_UNITS = List.of(
		new WaveUnit(EntityType.DROWNED, 12, 30, false),
		new WaveUnit(EntityType.GUARDIAN, 22, 18, false),
		new WaveUnit(EntityType.ELDER_GUARDIAN, 170, 2, false),
		new WaveUnit(EntityType.SLIME, 16, 16, false),
		new WaveUnit(EntityType.MAGMA_CUBE, 28, 10, false),
		new WaveUnit(EntityType.CAVE_SPIDER, 18, 12, false),
		new WaveUnit(EntityType.SPIDER, 12, 14, false),
		new WaveUnit(EntityType.WITCH, 46, 8, false),
		new WaveUnit(EntityType.VEX, 56, 7, false),
		new WaveUnit(EntityType.ENDERMAN, 42, 8, false),
		new WaveUnit(EntityType.RAVAGER, 160, 2, false),
		new WaveUnit(EntityType.HOGLIN, 62, 5, false),
		new WaveUnit(EntityType.ZOGLIN, 84, 4, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	private static final List<WaveUnit> FIRE_WAVE_UNITS = List.of(
		new WaveUnit(EntityType.BLAZE, 20, 24, false),
		new WaveUnit(EntityType.MAGMA_CUBE, 18, 20, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 42, 12, false),
		new WaveUnit(EntityType.PIGLIN_BRUTE, 72, 8, false),
		new WaveUnit(EntityType.HOGLIN, 52, 10, false),
		new WaveUnit(EntityType.ZOGLIN, 64, 8, false),
		new WaveUnit(EntityType.GHAST, 96, 5, false),
		new WaveUnit(EntityType.VEX, 48, 8, false),
		new WaveUnit(EntityType.EVOKER, 88, 5, false),
		new WaveUnit(EntityType.RAVAGER, 145, 3, false),
		new WaveUnit(EntityType.ENDERMAN, 40, 9, false),
		new WaveUnit(EntityType.WITHER_SKELETON, 900, 1, true)
	);

	private static final Map<WaveTheme, List<WaveUnit>> THEME_WAVE_UNITS = Map.of(
		WaveTheme.EARTH, EARTH_WAVE_UNITS,
		WaveTheme.WATER, WATER_WAVE_UNITS,
		WaveTheme.FIRE, FIRE_WAVE_UNITS
	);

	private final RaidGame game;

	public WaveTheme getCurrentWaveTheme() {
		int waveIndex = getCurrentWave();
		if (waveIndex == 0) {
			return WaveTheme.EARTH;
		}

		int idx = Math.floorMod(waveIndex - 2, 3);
		return idx == 0 ? WaveTheme.EARTH : idx == 1 ? WaveTheme.WATER : WaveTheme.FIRE;
	}

	private final Set<Monster> aliveWaveMonsters = new HashSet<>();
	private RaidMonsterBoss<? extends Monster> currentBoss;

	private int bossesDefeated = 0;
	private boolean bossWaveActive = false;


	protected RaidWaveController(RaidGame game, int maxWaves) {
		super(game, maxWaves, new DefaultWaveLoadout(game));
		this.game = game;
	}

	@Override
	protected void onStart() {
		super.onStart();

		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			applyLoadout(player);
		}

		game.getWorldManager()
			.mutateLobby(lobby -> lobby
				.clear(ClearMode.STATE)
				.showOnStart(Component.text("Wave starting"))
				.thenDont(getGame()::start)
				.then(this::startWave)
			);
	}

	@Override
	protected void onStop() {
		super.onStop();

		despawnAllEnemies();
		getGame().stop();
	}

	@Override
	protected void nextWave() {
		for (Player player : getGame().getTeamController().getOnlinePlayers()) {
			Utility.resetPlayerState(player);
			player.setGameMode(GameMode.SURVIVAL);
		}

		GameTeamController teamController = getGame().getTeamController();
		teamController.stop();
		game.getWorldManager().transfer((builder) -> builder
			.of(getCurrentWaveTheme().getWorldCreator())
		);
	}

	@Override
	public void startWave() {
		getGame().getTeamController().start();

		applyThemePlayerEffects();

		spawnWaveContent();
	}

	@Override
	protected void evaluateWaveState() {
		Set<Player> alivePlayers = game.getTeamController().getAlivePlayers();
		if (alivePlayers.isEmpty()) {
			Bukkit.broadcast(Component.text("Raid failed: all players are down").color(NamedTextColor.RED));

			scheduleReturn();
			return;
		}

		applyThemePlayerEffects();
		// retargetAliveMonsters(alivePlayers);

		if (aliveWaveMonsters.isEmpty()) return;

		aliveWaveMonsters.removeIf(monster -> monster.isDead() || !monster.isValid());

		if (aliveWaveMonsters.isEmpty()) {
			if (bossWaveActive) {
				onBossWaveCleared();
			}

			Bukkit.broadcast(Component.text("Wave " + getCurrentWaveNumber() + " cleared").color(NamedTextColor.GREEN));

			scheduleNextWave();
		}
	}

	private void spawnWaveContent() {
		aliveWaveMonsters.clear();
		bossWaveActive = shouldSpawnBossWave();

		if (bossWaveActive) {
			spawnBossWave();
			return;
		}

		int pointsBudget = computeWavePointsBudget();
		List<WaveUnit> roster = composeWaveRoster(pointsBudget, getCurrentWaveTheme());
		Location center = getGame().getWorldManager().getWorld().getSpawnLocation();

		for (WaveUnit unit : roster) {
			Location spawn = Utility.snapToHighestY(center.clone().add(
				ThreadLocalRandom.current().nextDouble(-18, 18),
				0,
				ThreadLocalRandom.current().nextDouble(-18, 18)
			), true);

			Monster mob = spawnRegularMob(spawn, unit.type());
			if (mob == null) continue;

			configureWaveMob(mob, unit);

			aliveWaveMonsters.add(mob);
		}

		Bukkit.broadcast(Component.text(
			"Wave " + getCurrentWaveNumber() + " spawned: " + aliveWaveMonsters.size() + " monsters (" + pointsBudget + " pts)"
		).color(NamedTextColor.DARK_RED));
	}

	private void spawnBossWave() {
		Location center = getGame().getWorldManager().getWorld().getSpawnLocation();

		WaveTheme currentTheme = getCurrentWaveTheme();

		if (currentTheme != WaveTheme.WATER) {
			center.add(0, 1.0, 0);
		}

		grantBossWaveCombatSupplies();
		if (currentTheme == WaveTheme.WATER) {
			grantWaterBossMobilityKit();
		}

		despawnBoss();
		currentBoss = createBossForTheme(currentTheme);
		currentBoss.spawn(center);

		Monster bossEntity = currentBoss.getEntity();
		if (bossEntity != null) {
			aliveWaveMonsters.add(bossEntity);
		}

		Bukkit.broadcast(Component.text(
			"Boss Wave (" + currentTheme.getDisplay() + "): " + mapThemeToBossTitle(currentTheme)
		).color(NamedTextColor.DARK_PURPLE));

		center.getWorld().strikeLightningEffect(center);
		center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
	}

	private RaidMonsterBoss<? extends Monster> createBossForTheme(WaveTheme theme) {
		return new GoldenKnightBoss(game, mapThemeToBossElement(theme));
	}

	private void grantWaterBossMobilityKit() {
		ItemStack mobilityTrident = new ItemStack(Material.TRIDENT);
		mobilityTrident.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.RIPTIDE, 2);
		mobilityTrident.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 3);

		for (Player player : game.getTeamController().getOnlinePlayers()) {
			player.getInventory().addItem(mobilityTrident.clone());
		}

		Bukkit.broadcast(Component.text(
			"Water boss kit: mobility trident granted"
		).color(NamedTextColor.AQUA));
	}

	private void grantBossWaveCombatSupplies() {
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated + 2));

		for (Player player : game.getTeamController().getOnlinePlayers()) {
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		Bukkit.broadcast(Component.text(
			"Boss combat supplies delivered: 64 steaks + " + goldenApples + " golden apples"
		).color(NamedTextColor.GOLD));
	}

	private Monster spawnRegularMob(Location location, EntityType type) {
		Entity entity = location.getWorld().spawnEntity(location, type);
		if (! (entity instanceof Monster monster)) {
			entity.remove();
			return null;
		}

		return monster;
	}

	private void configureWaveMob(LivingEntity mob, WaveUnit unit) {
		List<Player> alivePlayers = game.getTeamController().getAlivePlayersStream().toList();
		if (mob instanceof Monster monster && !alivePlayers.isEmpty()) {
			monster.setTarget(alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size())));
		}

		WaveTheme currentTheme = getCurrentWaveTheme();

		mob.setRemoveWhenFarAway(false);
		if (currentTheme == WaveTheme.FIRE) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
		}
		if (currentTheme == WaveTheme.WATER) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 0, true, false));
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, true, false));
		}

		if (mob instanceof Zombie zombie) {
			zombie.setShouldBurnInDay(false);
		}

		if (mob instanceof AbstractSkeleton skeleton) {
			skeleton.setShouldBurnInDay(false);
		}

		int currentWave = getCurrentWave();

		double hp = 18.0 + (currentWave * 2.25);
		if (unit.bossEcho()) hp *= 3.4;

		setBaseValue(mob, Attribute.GENERIC_MAX_HEALTH, hp);
		if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
			mob.setHealth(hp);
		}

		double damage = 3.5 + Math.min(12.0, currentWave * 0.42);
		if (unit.bossEcho()) damage *= 2.2;
		setBaseValue(mob, Attribute.GENERIC_ATTACK_DAMAGE, damage);

		double speed = 0.25 + Math.min(0.13, currentWave * 0.004);
		if (unit.bossEcho()) speed += 0.05;
		setBaseValue(mob, Attribute.GENERIC_MOVEMENT_SPEED, speed);

		double kbRes = Math.min(0.65, currentWave * 0.015);
		setBaseValue(mob, Attribute.GENERIC_KNOCKBACK_RESISTANCE, kbRes);

		applyScaledEquipment(mob);

		if (unit.bossEcho()) {
			mob.customName(Component.text("Echo of Dark Knight").color(NamedTextColor.DARK_PURPLE));
			mob.setCustomNameVisible(true);
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2, true, false));
			mob.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1, true, false));
		}

		if (currentWave >= 10) {
			mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, Math.min(2, currentWave / 15), true, false));
		}

		mob.getWorld().spawnParticle(Particle.SMOKE_LARGE, mob.getLocation(), 12, 0.3, 0.4, 0.3, 0.01);
		mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 0.7f);
	}

	private void applyScaledEquipment(LivingEntity mob) {
		EntityEquipment equipment = mob.getEquipment();
		if (equipment == null) return;

		int currentWave = getCurrentWave();

		double armorChance = Math.min(0.95, 0.2 + currentWave * 0.03);
		double weaponChance = Math.min(0.95, 0.25 + currentWave * 0.035);
		int tier = Math.min(4, Math.max(0, (currentWave - 1) / 5));

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

		int currentWave = getCurrentWave();

		ItemStack item = new ItemStack(material);

		int protection = Math.min(4, 1 + currentWave / 8);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.9, 0.15 + currentWave * 0.03)) {
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

		int currentWave = getCurrentWave();

		ItemStack weapon = new ItemStack(picked);

		int sharpness = Math.min(5, 1 + currentWave / 10);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.95, 0.2 + currentWave * 0.03)) {
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

	private int computeWavePointsBudget() {
		double budget = BASE_WAVE_POINTS * Math.pow(2, Math.max(0, getCurrentWave() - 1));
		return (int) Math.min(MAX_WAVE_POINTS, budget);
	}

	private List<WaveUnit> composeWaveRoster(int pointsBudget, WaveTheme theme) {
		List<WaveUnit> roster = new ArrayList<>();
		int remaining = pointsBudget;
		int guard = 0;

		while (remaining >= 12 && guard++ < 2000 && roster.size() < MAX_MOB_COUNT) {
			WaveUnit picked = pickWeightedUnit(remaining, theme);
			if (picked == null) break;
			roster.add(picked);
			remaining -= picked.cost();
		}

		while (roster.size() < MIN_MOBS_NON_BOSS_WAVE && roster.size() < MAX_MOB_COUNT) {
			WaveUnit filler = pickWeightedUnit(20, theme);
			if (filler == null) {
				filler = new WaveUnit(EntityType.ZOMBIE, 12, 1, false);
			}
			roster.add(filler);
		}

		return roster;
	}

	@Nullable
	private WaveUnit pickWeightedUnit(int remainingPoints, WaveTheme theme) {
		List<WaveUnit> pool = THEME_WAVE_UNITS.getOrDefault(theme, EARTH_WAVE_UNITS);
		List<WaveUnit> eligible = pool.stream()
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
		if (! bossWaveActive) return;
		bossWaveActive = false;

		bossesDefeated++;
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated));

		for (Player player : game.getTeamController().getOnlinePlayers()) {
			if (!player.isOnline()) continue;
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		Bukkit.broadcast(Component.text(
			"Boss defeated! Rewards: 64 steaks + " + goldenApples + " golden apples per player"
		).color(NamedTextColor.GOLD));

		despawnBoss();
	}

	// private void retargetAliveMonsters(Collection<Player> alivePlayers) {
	// 	if (alivePlayers.isEmpty()) return;

	// 	for (UUID id : aliveWaveMonsters) {
	// 		Entity entity = Bukkit.getEntity(id);
	// 		if (! (entity instanceof Monster monster)) continue;

	// 		if (monster.isDead() || !monster.isValid()) continue;

	// 		Player target = List.copyOf(alivePlayers).get(ThreadLocalRandom.current().nextInt(alivePlayers.size()));
	// 		monster.setTarget(target);
	// 	}
	// }

	private boolean shouldSpawnBossWave() {
		int wave = getCurrentWave();
		if (wave < BOSS_START_WAVE) return false;
		return ((wave - BOSS_START_WAVE) % BOSS_INTERVAL) == 0;
	}

	private void despawnMonsters() {
		for (Monster monster : aliveWaveMonsters) {
			monster.remove();
		}
		aliveWaveMonsters.clear();
	}
	private void despawnBoss() {
		if (currentBoss != null) {
			currentBoss.despawn();
		}
		currentBoss = null;
	}
	private void despawnAllEnemies() {
		despawnBoss();
		despawnMonsters();
	}

	private void setBaseValue(LivingEntity entity, Attribute attribute, double value) {
		AttributeInstance instance = entity.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	private void applyThemePlayerEffects(Player player) {
		WaveTheme currentTheme = getCurrentWaveTheme();

		if (currentTheme == WaveTheme.WATER) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, ENV_EFFECT_TICKS, 0, true, false, true));
			player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, ENV_EFFECT_TICKS, 0, true, false, true));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ENV_EFFECT_TICKS, 0, true, false, true));
		} else if (currentTheme == WaveTheme.FIRE) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, ENV_EFFECT_TICKS, 0, true, false, true));
		} else {
			player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		}
	}
	private void applyThemePlayerEffects() {
		for (Player player : game.getTeamController().getOnlinePlayers()) {
			applyThemePlayerEffects(player);
		}
	}

	private RaidMonsterBoss.Element mapThemeToBossElement(WaveTheme theme) {
		return switch (theme) {
			case WATER -> RaidMonsterBoss.Element.WATER;
			case FIRE -> RaidMonsterBoss.Element.FIRE;
			default -> RaidMonsterBoss.Element.EARTH;
		};
	}

	private String mapThemeToBossTitle(WaveTheme theme) {
		return switch (theme) {
			case WATER -> "Abyssal Serpent";
			case FIRE -> "Infernal Knight";
			default -> "Golden Knight";
		};
	}

	private record WaveUnit(EntityType type, int cost, int weight, boolean bossEcho) { }
}
