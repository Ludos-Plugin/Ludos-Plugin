package fr.ludos.game.arena.monster;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.arena.ArenaGame;

public abstract class SpecialMonster<TEntity extends LivingEntity> {
	private final String typeId;
	private final ArenaGame game;
	private final UUID id;

	@Nullable
	private TEntity entity;

	@Nullable
	private BukkitTask tickTask;

	protected SpecialMonster(String typeId, ArenaGame game) {
		this.typeId = typeId;
		this.game = game;
		this.id = UUID.randomUUID();
	}

	public String getTypeId() {
		return typeId;
	}

	public ArenaGame getGame() {
		return game;
	}

	public UUID getId() {
		return id;
	}

	@Nullable
	public TEntity getEntity() {
		return entity;
	}

	public boolean isAlive() {
		return entity != null && entity.isValid() && !entity.isDead();
	}

	public final void spawn(Location location) {
		if (isAlive()) return;
		TEntity spawned = spawnMonsterEntity(location);
		this.entity = spawned;

		onMonsterSpawn(spawned);

		tickTask = game.getPlugin().getServer().getScheduler().runTaskTimer(game.getPlugin(), () -> {
			if (!isAlive()) {
				disposeTask();
				return;
			}
			onMonsterTick(spawned);
		}, 1L, 1L);
	}

	public final void despawn() {
		disposeTask();
		TEntity current = entity;
		entity = null;

		if (current != null) {
			onMonsterDespawn(current);
			if (current.isValid() && !current.isDead()) {
				current.remove();
			}
		}
	}

	public final void onDeath() {
		disposeTask();
		TEntity current = entity;
		entity = null;

		if (current != null) {
			onMonsterDeath(current);
		}
	}

	protected final void setMaxHealth(LivingEntity livingEntity, double maxHealth) {
		AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (maxHealthAttr != null) {
			maxHealthAttr.setBaseValue(maxHealth);
		}
		livingEntity.setHealth(Math.min(maxHealth, livingEntity.getHealth()));
	}

	protected final void setAttributeBase(LivingEntity livingEntity, Attribute attribute, double value) {
		AttributeInstance instance = livingEntity.getAttribute(attribute);
		if (instance != null) {
			instance.setBaseValue(value);
		}
	}

	protected final int tickCooldown(int value) {
		return value > 0 ? value - 1 : 0;
	}

	protected final boolean isArenaTarget(Player player, World world, Location center, double maxDistanceSquared) {
		if (!game.isArenaPlayer(player)) return false;
		if (!player.getWorld().equals(world)) return false;
		return player.getLocation().distanceSquared(center) <= maxDistanceSquared;
	}

	private void disposeTask() {
		if (tickTask != null) {
			tickTask.cancel();
			tickTask = null;
		}
	}

	protected abstract TEntity spawnMonsterEntity(Location location);

	protected void onMonsterSpawn(TEntity entity) { }

	protected abstract void onMonsterTick(TEntity entity);

	protected void onMonsterDespawn(TEntity entity) { }

	protected void onMonsterDeath(TEntity entity) { }
}
