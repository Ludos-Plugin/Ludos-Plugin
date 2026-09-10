package fr.ludos.games.raid;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.Utility;
import fr.ludos.core.item.Categories;
import fr.ludos.core.item.Category;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.wave.DefaultWaveLoadout;
import fr.ludos.core.wave.WaveController;
import fr.ludos.games.raid.monsters.RaidMonsterBoss;
import fr.ludos.games.raid.theme.EarthWaveTheme;
import fr.ludos.games.raid.theme.HellWaveTheme;
import fr.ludos.games.raid.theme.OceanWaveTheme;
import fr.ludos.games.raid.theme.RaidWaveTheme;
import fr.ludos.games.raid.theme.RaidWaveTheme.WaveUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Controller for {@link RaidGame} wave behavior.
 */
public final class RaidWaveController extends WaveController {
	private static final int BOSS_START_WAVE = 5;
	private static final int BOSS_INTERVAL = 5;
	private static final int BASE_WAVE_POINTS = 200;
	private static final int MAX_WAVE_POINTS = 48000;
	private static final int LAST_FEW_MOBS_COUNT = 5;
	private static final int STEAK_REWARD_PER_BOSS_WAVE = 64;

	private final RaidGame game;

	private final EarthWaveTheme plains;
	private final OceanWaveTheme ocean;
	private final HellWaveTheme hell;

	public RaidWaveTheme getCurrentWaveTheme() {
		int waveIndex = getCurrentWave();
		if (waveIndex == 0) {
			return plains;
		}

		int idx = Math.floorMod(waveIndex - 2, 3);
		return idx == 0 ? plains : idx == 1 ? ocean : hell;
	}

	private final Set<Monster> aliveWaveMonsters = new HashSet<>();
	private RaidMonsterBoss<? extends Monster> currentBoss;

	private int bossesDefeated = 0;
	private boolean bossWaveActive = false;

	public int bossesDefeated() {
		return this.bossesDefeated;
	}


	protected RaidWaveController(RaidGame game, int maxWaves) {
		super(game, maxWaves, new DefaultWaveLoadout(game));
		this.game = game;

		plains = new EarthWaveTheme(game);
		ocean = new OceanWaveTheme(game);
		hell = new HellWaveTheme(game);
	}

	@Override
	protected void onStart() {
		super.onStart();

		for (Player player : game().teamController().getOnlinePlayers()) {
			applyLoadout(player);
		}

		game.worldManager()
			.mutateLobby(lobby -> lobby
				.clear(ClearMode.STATE)
				.showOnStart(Component.text("Wave starting"))
				.thenDont(game()::start)
				.then(this::startWave)
			);
	}

	@Override
	protected void onStop() {
		despawnAllEnemies();

		super.onStop();
		game().stop();
	}

	@Override
	protected void nextWave() {
		for (Player player : game().teamController().getOnlinePlayers()) {
			Utility.resetPlayerState(player);
			player.setGameMode(GameMode.SURVIVAL);
		}

		game.worldManager().transfer((builder) -> builder
			.of(getCurrentWaveTheme().getWorldCreator())
			.config(getCurrentWaveTheme().getWorldConfig())
		);
	}

	@Override
	public void startWave() {
		game().teamController().placeAllPlayers();

		Set<Player> alivePlayers = game.teamController().getAlivePlayers();
		getCurrentWaveTheme().applyPlayerEffects(alivePlayers);

		spawnWaveContent();
	}



	@Override
	protected void evaluateWaveState() {
		Set<Player> alivePlayers = game.teamController().getAlivePlayers();
		if (alivePlayers.isEmpty()) {
			game.group().sendMessage(Component.text("Raid failed: all players are down").color(NamedTextColor.RED));

			scheduleReturn();
			return;
		}

		retargetAliveMonsters(alivePlayers);

		if (aliveWaveMonsters.isEmpty()) return;

		aliveWaveMonsters.removeIf(monster -> monster.isDead() || !monster.isValid());

		PotionEffect glow = new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, true, false);
		if (aliveWaveMonsters.size() <= LAST_FEW_MOBS_COUNT) {
			for (Monster mob : aliveWaveMonsters) {
				mob.getWorld().spawnParticle(Particle.SMOKE_LARGE, mob.getLocation(), 12, 0.3, 0.4, 0.3, 0.01);
				mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 0.7f);
				mob.addPotionEffect(glow);
			}
		}

		if (aliveWaveMonsters.isEmpty()) {
			if (bossWaveActive) {
				onBossWaveCleared();
			}

			game.group().sendMessage(Component.text("Wave " + getCurrentWaveNumber() + " cleared").color(NamedTextColor.GREEN));

			completeCurrentWave();
		}
	}

	private void spawnWaveContent() {
		aliveWaveMonsters.clear();

		if (shouldSpawnBossWave()) {
			spawnBossWave();
			return;
		}

		spawnWaveMobs();
	}

	private void spawnWaveMobs() {
		int pointsBudget = computeWavePointsBudget();
		List<WaveUnit> roster = getCurrentWaveTheme().composeWaveRoster(pointsBudget, this);
		Location center = game().worldManager().getWorld().getSpawnLocation();

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

		PotionEffect glow = new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0, true, false);
		for (Monster mob : aliveWaveMonsters) {
			mob.getWorld().spawnParticle(Particle.SMOKE_LARGE, mob.getLocation(), 12, 0.3, 0.4, 0.3, 0.01);
			mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.5f, 0.7f);
			mob.addPotionEffect(glow);
		}

		game.group().sendMessage(Component.text(
			"Wave " + getCurrentWaveNumber() + " spawned: " + aliveWaveMonsters.size() + " monsters (" + pointsBudget + " pts)"
		).color(NamedTextColor.DARK_RED));
	}

	private void spawnBossWave() {
		RaidWaveTheme currentTheme = getCurrentWaveTheme();

		Location center = currentTheme.getSpawnLocation(game().worldManager().getWorld());

		grantBossWaveCombatSupplies();

		despawnBoss();
		currentBoss = currentTheme.createBoss(game);
		currentBoss.spawn(center);

		Monster bossEntity = currentBoss.entity();
		if (bossEntity != null) {
			aliveWaveMonsters.add(bossEntity);
		}

		game.group().sendMessage(
			Component.text("Boss Wave (")
				.append(currentTheme.displayName())
				.append(Component.text("): "))
				.append(currentBoss.displayName())
			.color(NamedTextColor.DARK_PURPLE)
		);

		center.getWorld().strikeLightningEffect(center);
		center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
	}

	private void grantBossWaveCombatSupplies() {
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated + 2));

		for (Player player : game.teamController().getOnlinePlayers()) {
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		game.group().sendMessage(Component.text(
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
		List<Player> alivePlayers = game.teamController().getAlivePlayersStream().toList();
		if (mob instanceof Monster monster && !alivePlayers.isEmpty()) {
			monster.setTarget(alivePlayers.get(ThreadLocalRandom.current().nextInt(alivePlayers.size())));
		}

		RaidWaveTheme currentTheme = getCurrentWaveTheme();

		mob.setRemoveWhenFarAway(true);
		currentTheme.applyMobEffects(mob);

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
			mob.customName(Component.text("Echo of the Past").color(NamedTextColor.DARK_PURPLE));
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

		if (ThreadLocalRandom.current().nextDouble() < armorChance) {
			equipment.setHelmet(createArmor(tier, Categories.ARMOR_TYPE.HELMETS));
			equipment.setHelmetDropChance(tier >= 3 ? 1.0f : 0.0f);
		}
		if (ThreadLocalRandom.current().nextDouble() < armorChance) {
			equipment.setChestplate(createArmor(tier, Categories.ARMOR_TYPE.CHESTPLATES));
			equipment.setHelmetDropChance(tier >= 3 ? 1.0f : 0.0f);
		}
		if (ThreadLocalRandom.current().nextDouble() < armorChance) {
			equipment.setLeggings(createArmor(tier, Categories.ARMOR_TYPE.LEGGINGS));
			equipment.setHelmetDropChance(tier >= 3 ? 1.0f : 0.0f);
		}
		if (ThreadLocalRandom.current().nextDouble() < armorChance) {
			equipment.setBoots(createArmor(tier, Categories.ARMOR_TYPE.BOOTS));
			equipment.setHelmetDropChance(tier >= 3 ? 1.0f : 0.0f);
		}

		if (
			! (mob instanceof Vindicator) &&
			! (mob instanceof Pillager) &&
			! (mob instanceof Skeleton) &&
			! (mob instanceof Husk) &&
			ThreadLocalRandom.current().nextDouble() < weaponChance
		) {
			equipment.setItemInMainHand(createWeapon(tier));
			equipment.setItemInMainHandDropChance(tier >= 3 ? 1.0f : 0.0f);
		}
	}

	private ItemStack createArmor(int tier, Categories.ARMOR_TYPE armorGroup) {
		String suffix = switch (armorGroup) {
			case HELMETS -> "HELMET";
			case CHESTPLATES -> "CHESTPLATE";
			case LEGGINGS -> "LEGGINGS";
			case BOOTS -> "BOOTS";
		};
		Category category = switch (armorGroup) {
			case HELMETS -> Categories.HELMETS;
			case CHESTPLATES -> Categories.CHESTPLATES;
			case LEGGINGS -> Categories.LEGGINGS;
			case BOOTS -> Categories.BOOTS;
		};

		String prefix = switch (Math.min(4, Math.max(0, tier))) {
			case 0 -> "LEATHER";
			case 1 -> "CHAINMAIL";
			case 2 -> "IRON";
			case 3 -> "DIAMOND";
			default -> "NETHERITE";
		};

		Material material = Material.matchMaterial(prefix + '_' + suffix);
		if (material == null || ! category.contains(material)) {
			material = category.stream().findFirst().orElse(Material.AIR);
		}

		int currentWave = getCurrentWave();

		ItemStack item = new ItemStack(material);

		int protection = Math.min(4, 1 + currentWave / 8);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.9, 0.15 + currentWave * 0.03)) {
			item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protection);
		}
		return item;
	}

	@Nullable
	private ItemStack createWeapon(int tier) {
		Categories.MELEE_WEAPON_TYPE weaponGroup = ThreadLocalRandom.current().nextBoolean()
			? Categories.MELEE_WEAPON_TYPE.SWORDS
			: Categories.MELEE_WEAPON_TYPE.SWORDS;

		Material picked = resolveTierWeaponMaterial(tier, weaponGroup);
		if (picked == null) return null;

		int currentWave = getCurrentWave();

		ItemStack weapon = new ItemStack(picked);

		int sharpness = Math.min(5, 1 + currentWave / 10);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.95, 0.2 + currentWave * 0.03)) {
			weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
		}

		return weapon;
	}

	@Nullable
	private Material resolveTierWeaponMaterial(int tier, Categories.MELEE_WEAPON_TYPE weaponGroup) {
		String suffix = switch(weaponGroup) {
			case SWORDS -> "SWORD";
			case AXES -> "AXE";
		};
		Category category = switch (weaponGroup) {
			case SWORDS -> Categories.SWORDS;
			case AXES -> Categories.AXES;
		};

		String prefix = switch (Math.min(4, Math.max(0, tier))) {
			case 0 -> ThreadLocalRandom.current().nextBoolean() ? "WOODEN" : "STONE";
			case 1 -> ThreadLocalRandom.current().nextBoolean() ? "STONE" : "IRON";
			case 2 -> ThreadLocalRandom.current().nextBoolean() ? "IRON" : "DIAMOND";
			case 3 -> "DIAMOND";
			default -> "NETHERITE";
		};

		Material material = Material.matchMaterial(prefix + '_' + suffix);
		if (material == null || ! category.contains(material)) {
			return category.stream().findFirst().orElse(null);
		}

		return material;
	}

	private int computeWavePointsBudget() {
		double budget = BASE_WAVE_POINTS * Math.pow(2, Math.max(0, getCurrentWave() - 1));
		return (int) Math.min(MAX_WAVE_POINTS, budget);
	}

	private void onBossWaveCleared() {
		if (! bossWaveActive) return;
		bossWaveActive = false;

		bossesDefeated++;
		int goldenApples = Math.min(8, Math.max(1, bossesDefeated));

		for (Player player : game.teamController().getOnlinePlayers()) {
			if (!player.isOnline()) continue;
			player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, STEAK_REWARD_PER_BOSS_WAVE));
			player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, goldenApples));
		}

		game.group().sendMessage(Component.text(
			"Boss defeated! Rewards: 64 steaks + " + goldenApples + " golden apples per player"
		).color(NamedTextColor.GOLD));

		despawnBoss();
	}

	private void retargetAliveMonsters(Collection<Player> alivePlayers) {
		if (alivePlayers.isEmpty()) return;

		for (Monster mob : aliveWaveMonsters) {
			if (mob.isDead() || !mob.isValid()) continue;

			Player target = List.copyOf(alivePlayers).get(ThreadLocalRandom.current().nextInt(alivePlayers.size()));
			mob.setTarget(target);
		}
	}

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
}
