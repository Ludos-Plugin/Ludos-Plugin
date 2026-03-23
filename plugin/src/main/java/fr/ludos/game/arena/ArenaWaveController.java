package fr.ludos.game.arena;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.game.arena.monster.DarkKnightBoss;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public final class ArenaWaveController extends GameEvents {
	private static final int PREP_TICKS = 20 * 10;
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

		if (
			event.getFrom().getX() == event.getTo().getX() &&
			event.getFrom().getY() == event.getTo().getY() &&
			event.getFrom().getZ() == event.getTo().getZ()
		) return;

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

		for (Player player : arena.getArenaPlayers()) {
			arena.resetArenaPlayerState(player);
			player.getInventory().clear();
			player.getInventory().addItem(Ludos.createGuidebook());
		}

		arena.teleportArenaPlayersForRound();
		startPreparationCountdown();
	}

	private void startPreparationCountdown() {
		if (preparationTask != null) {
			preparationTask.cancel();
			preparationTask = null;
		}

		preparationTask = new BukkitRunnable() {
			private int ticksLeft = PREP_TICKS;

			@Override
			public void run() {
				int secondsLeft = Math.max(0, ticksLeft / 20);
				for (Player player : arena.getArenaPlayers()) {
					player.showTitle(Title.title(
						Component.text("Wave " + wave).color(NamedTextColor.WHITE),
						Component.text("Begins in " + secondsLeft + "s").color(NamedTextColor.WHITE),
						Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(750), Duration.ofMillis(100))
					));
				}

				if (ticksLeft <= 0) {
					cancel();
					preparationTask = null;
					startWaveCombat();
					return;
				}

				ticksLeft -= 20;
			}
		}.runTaskTimer(arena.getPlugin(), 0L, 20L);
	}

	private void startWaveCombat() {
		preparationPhase = false;

		for (Player player : arena.getArenaPlayers()) {
			arena.applyArenaCombatLoadout(player);
		}

		spawnWaveContent();

		if (waveCheckTask != null) {
			waveCheckTask.cancel();
			waveCheckTask = null;
		}
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
		EntityType type;
		int roll = ThreadLocalRandom.current().nextInt(100);
		if (roll < 20) type = EntityType.ZOMBIE;
		else if (roll < 35) type = EntityType.HUSK;
		else if (roll < 50) type = EntityType.DROWNED;
		else if (roll < 65) type = EntityType.SKELETON;
		else if (roll < 78) type = EntityType.STRAY;
		else if (roll < 88) type = EntityType.SPIDER;
		else if (roll < 95) type = EntityType.WITCH;
		else type = EntityType.VINDICATOR;

		Entity entity = location.getWorld().spawnEntity(location, type);
		if (!(entity instanceof LivingEntity living)) {
			entity.remove();
			return null;
		}

		return living;
	}

	private void configureWaveMob(LivingEntity mob) {
		List<Player> alivePlayers = getAlivePlayers();
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

		if (mob.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
			double hp = 18.0 + (wave * 2.25);
			mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
			mob.setHealth(hp);
		}

		if (mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
			double damage = 3.5 + Math.min(12.0, wave * 0.42);
			mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
		}

		if (mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
			double speed = 0.25 + Math.min(0.13, wave * 0.004);
			mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		}

		if (mob.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null) {
			double kbRes = Math.min(0.65, wave * 0.015);
			mob.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(kbRes);
		}

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

		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setHelmet(createArmor(tier, ArmorSlot.HELMET));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setChestplate(createArmor(tier, ArmorSlot.CHESTPLATE));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setLeggings(createArmor(tier, ArmorSlot.LEGGINGS));
		if (ThreadLocalRandom.current().nextDouble() < armorChance) equipment.setBoots(createArmor(tier, ArmorSlot.BOOTS));

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

	private enum ArmorSlot {
		HELMET,
		CHESTPLATE,
		LEGGINGS,
		BOOTS
	}

	private ItemStack createArmor(int tier, ArmorSlot slot) {
		Material material;
		switch (tier) {
			case 0 -> material = switch (slot) {
				case HELMET -> Material.LEATHER_HELMET;
				case CHESTPLATE -> Material.LEATHER_CHESTPLATE;
				case LEGGINGS -> Material.LEATHER_LEGGINGS;
				case BOOTS -> Material.LEATHER_BOOTS;
			};
			case 1 -> material = switch (slot) {
				case HELMET -> Material.CHAINMAIL_HELMET;
				case CHESTPLATE -> Material.CHAINMAIL_CHESTPLATE;
				case LEGGINGS -> Material.CHAINMAIL_LEGGINGS;
				case BOOTS -> Material.CHAINMAIL_BOOTS;
			};
			case 2 -> material = switch (slot) {
				case HELMET -> Material.IRON_HELMET;
				case CHESTPLATE -> Material.IRON_CHESTPLATE;
				case LEGGINGS -> Material.IRON_LEGGINGS;
				case BOOTS -> Material.IRON_BOOTS;
			};
			case 3 -> material = switch (slot) {
				case HELMET -> Material.DIAMOND_HELMET;
				case CHESTPLATE -> Material.DIAMOND_CHESTPLATE;
				case LEGGINGS -> Material.DIAMOND_LEGGINGS;
				case BOOTS -> Material.DIAMOND_BOOTS;
			};
			default -> material = switch (slot) {
				case HELMET -> Material.NETHERITE_HELMET;
				case CHESTPLATE -> Material.NETHERITE_CHESTPLATE;
				case LEGGINGS -> Material.NETHERITE_LEGGINGS;
				case BOOTS -> Material.NETHERITE_BOOTS;
			};
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
		Material[] options;
		switch (tier) {
			case 0 -> options = new Material[] { Material.WOODEN_SWORD, Material.STONE_SWORD, Material.WOODEN_AXE };
			case 1 -> options = new Material[] { Material.STONE_SWORD, Material.IRON_SWORD, Material.STONE_AXE };
			case 2 -> options = new Material[] { Material.IRON_SWORD, Material.IRON_AXE, Material.DIAMOND_SWORD };
			case 3 -> options = new Material[] { Material.DIAMOND_SWORD, Material.DIAMOND_AXE };
			default -> options = new Material[] { Material.NETHERITE_SWORD, Material.NETHERITE_AXE };
		}

		if (options.length == 0) return null;

		Material picked = options[ThreadLocalRandom.current().nextInt(options.length)];
		ItemStack weapon = new ItemStack(picked);

		int sharpness = Math.min(5, 1 + wave / 10);
		if (ThreadLocalRandom.current().nextDouble() < Math.min(0.95, 0.2 + wave * 0.03)) {
			weapon.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, sharpness);
		}

		return weapon;
	}

	private void evaluateWaveState() {
		List<Player> alivePlayers = getAlivePlayers();
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
			if (waveCheckTask != null) {
				waveCheckTask.cancel();
				waveCheckTask = null;
			}
			Bukkit.broadcast(Component.text("Wave " + wave + " cleared").color(NamedTextColor.GREEN));
			Bukkit.getScheduler().runTaskLater(arena.getPlugin(), this::startNextWave, 40L);
		}
	}

	private boolean isAliveCombatPlayer(Player player) {
		return player.isOnline() && !player.isDead() && player.getGameMode() == GameMode.SURVIVAL;
	}

	private List<Player> getAlivePlayers() {
		return arena.getArenaPlayers().stream()
			.filter(this::isAliveCombatPlayer)
			.toList();
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
			if (entity != null) {
				entity.remove();
			}
		}
		aliveWaveMonsters.clear();

		if (currentBoss != null) {
			currentBoss.despawn();
			currentBoss = null;
		}
	}

	private void cancelTasks() {
		if (preparationTask != null) {
			preparationTask.cancel();
			preparationTask = null;
		}
		if (waveCheckTask != null) {
			waveCheckTask.cancel();
			waveCheckTask = null;
		}
	}
}
