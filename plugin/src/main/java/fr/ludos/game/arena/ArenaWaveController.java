package fr.ludos.game.arena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
import org.bukkit.util.Vector;

import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.game.arena.monster.DarkKnightBoss;
import fr.ludos.item.Categories;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ArenaWaveController extends GameEvents {
	private static final int BOSS_START_WAVE = 1;
	private static final int BOSS_INTERVAL = 10;
	private static final int BASE_MOB_COUNT = 10;
	private static final int MOBS_PER_WAVE = 3;
	private static final int MAX_MOB_COUNT = 100;

	private final ArenaGame arena;
	private final Set<UUID> aliveWaveMonsters = new HashSet<>();

	private DarkKnightBoss currentBoss;

	private int wave = 0;
	private boolean preparationPhase = false;

	private BukkitTask preparationTask;
	private BukkitTask waveCheckTask;

	public ArenaWaveController(ArenaGame game) {
		super(game);
		this.arena = game;
	}

	@Override
	protected void onStart() {
		startNextWave();
	}

	@Override
	protected void onStop() {
		cancelTasks();
		despawnAllMonsters();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!preparationPhase) return;
		if (!arena.isArenaPlayer(event.getPlayer())) return;
		if (event.getTo() == null) return;
		if (ArenaGame.isSamePosition(event.getFrom(), event.getTo())) return;

		event.setTo(event.getFrom());
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (!preparationPhase) return;
		if (!(event.getEntity() instanceof Player player)) return;
		if (!arena.isArenaPlayer(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!arena.isArenaPlayer(player)) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (aliveWaveMonsters.remove(entity.getUniqueId())) {
			event.getDrops().clear();
			event.setDroppedExp(0);
		}

		if (currentBoss != null && currentBoss.getEntity() != null && currentBoss.getEntity().getUniqueId().equals(entity.getUniqueId())) {
			currentBoss.onDeath();
			currentBoss = null;
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (!arena.isArenaPlayer(event.getPlayer())) return;
		Bukkit.getScheduler().runTask(arena.getPlugin(), this::evaluateWaveState);
	}

	private void startNextWave() {
		if (wave >= arena.getConfiguredRounds()) {
			Bukkit.broadcast(Component.text("Arena Waves complete!").color(NamedTextColor.GOLD));
			Game.stopCurrentGame();
			return;
		}

		wave++;
		preparationPhase = true;
		arena.prepareArenaPlayers();

		arena.teleportArenaPlayersForRound();
		startPreparationCountdown();
	}

	private void startPreparationCountdown() {
		preparationTask = arena.startPreparationCountdownTask(
			"Wave",
			() -> wave,
			"Begins in",
			this::startWaveCombat
		);
	}

	private void startWaveCombat() {
		preparationPhase = false;
		arena.applyCombatLoadoutToArenaPlayers();

		spawnWaveContent();

		waveCheckTask = ArenaGame.cancelTask(waveCheckTask);
		waveCheckTask = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(), this::evaluateWaveState, 0L, 10L);
	}

	private void spawnWaveContent() {
		aliveWaveMonsters.clear();

		if (shouldSpawnBossWave()) {
			spawnBossWave();
			return;
		}

		int amount = Math.min(MAX_MOB_COUNT, BASE_MOB_COUNT + (wave * MOBS_PER_WAVE));
		Location center = arena.getGameAreaController().getCenter();

		for (int i = 0; i < amount; i++) {
			Location spawn = center.clone().add(
				ThreadLocalRandom.current().nextDouble(-18, 18),
				0,
				ThreadLocalRandom.current().nextDouble(-18, 18)
			);
			arena.moveToHighestGround(spawn);
			LivingEntity mob = spawnRegularMob(spawn);
			if (mob == null) continue;

			configureWaveMob(mob);
			aliveWaveMonsters.add(mob.getUniqueId());
		}

		Bukkit.broadcast(Component.text("Wave " + wave + " spawned: " + aliveWaveMonsters.size() + " monsters").color(NamedTextColor.DARK_RED));
	}

	private void spawnBossWave() {
		Location center = arena.getGameAreaController().getCenter().clone();
		arena.moveToHighestGround(center);
		center.add(0, 1.0, 0);

		currentBoss = new DarkKnightBoss(arena);
		currentBoss.spawn(center);

		if (currentBoss.getEntity() != null) {
			aliveWaveMonsters.add(currentBoss.getEntity().getUniqueId());
		}

		Bukkit.broadcast(Component.text("Boss Wave: The Dark Knight emerges").color(NamedTextColor.DARK_PURPLE));
		center.getWorld().strikeLightningEffect(center);
		center.getWorld().playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
	}

	private LivingEntity spawnRegularMob(Location location) {
		Entity entity = location.getWorld().spawnEntity(location, pickMobType());
		if (!(entity instanceof LivingEntity living)) {
			entity.remove();
			return null;
		}

		return living;
	}

	private EntityType pickMobType() {
		List<EntityType> differentsEnemies = List.of(
			EntityType.ZOMBIE,
			EntityType.HUSK,
			EntityType.HOGLIN,
			EntityType.SKELETON,
			EntityType.STRAY,
			EntityType.SPIDER,
			EntityType.WITCH,
			EntityType.VINDICATOR
		);

		int roll = ThreadLocalRandom.current().nextInt(differentsEnemies.size());

		EntityType type = differentsEnemies.get(roll);
		return type.isAlive() ? type : EntityType.ZOMBIE;
	}

	private void configureWaveMob(LivingEntity mob) {
		List<Player> alivePlayers = arena.getAliveArenaPlayers();
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
		setBaseValue(mob, Attribute.GENERIC_MAX_HEALTH, hp);
		if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
			mob.setHealth(hp);
		}

		double damage = 3.5 + Math.min(12.0, wave * 0.42);
		setBaseValue(mob, Attribute.GENERIC_ATTACK_DAMAGE, damage);

		double speed = 0.25 + Math.min(0.13, wave * 0.004);
		setBaseValue(mob, Attribute.GENERIC_MOVEMENT_SPEED, speed);

		double kbRes = Math.min(0.65, wave * 0.015);
		setBaseValue(mob, Attribute.GENERIC_KNOCKBACK_RESISTANCE, kbRes);

		applyScaledEquipment(mob);

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
		List<Player> alivePlayers = arena.getAliveArenaPlayers();
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
			waveCheckTask = ArenaGame.cancelTask(waveCheckTask);
			Bukkit.broadcast(Component.text("Wave " + wave + " cleared").color(NamedTextColor.GREEN));
			Bukkit.getScheduler().runTaskLater(arena.getPlugin(), this::startNextWave, 40L);
		}
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

	private void cancelTasks() {
		preparationTask = ArenaGame.cancelTask(preparationTask);
		waveCheckTask = ArenaGame.cancelTask(waveCheckTask);
	}

	private void setBaseValue(LivingEntity entity, Attribute attribute, double value) {
		AttributeInstance instance = entity.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}
}
