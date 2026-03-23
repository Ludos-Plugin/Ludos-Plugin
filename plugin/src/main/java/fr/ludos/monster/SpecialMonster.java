package fr.ludos.game.arena.monster;

import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
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

		TEntity created = createEntity(location);
		this.entity = created;

		onSpawn(created);

		tickTask = game.getPlugin().getServer().getScheduler().runTaskTimer(game.getPlugin(), () -> {
			if (!isAlive()) {
				disposeTask();
				return;
			}
			onTick(created);
		}, 1L, 1L);
	}

	public final void despawn() {
		disposeTask();

		if (entity != null) {
			onDespawn(entity);
			entity.remove();
			entity = null;
		}
	}

	public final void onDeath() {
		disposeTask();
		if (entity != null) {
			onMonsterDeath(entity);
		}
	}

	protected final void setMaxHealth(LivingEntity livingEntity, double maxHealth) {
		AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if (maxHealthAttr != null) {
			maxHealthAttr.setBaseValue(maxHealth);
		}
		livingEntity.setHealth(Math.min(maxHealth, livingEntity.getHealth()));
	}

	private void disposeTask() {
		if (tickTask != null) {
			tickTask.cancel();
			tickTask = null;
		}
	}

	protected abstract TEntity createEntity(Location location);

	protected void onSpawn(TEntity entity) { }

	protected void onTick(TEntity entity) { }

	protected void onDespawn(TEntity entity) { }

	protected void onMonsterDeath(TEntity entity) { }
}
