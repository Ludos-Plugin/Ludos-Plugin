package fr.ludos.role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.item.ItemUtilities;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.berserker.BerserkerAxe;
import fr.ludos.item.berserker.BerserkerRageBrew;


public class BerserkerRole extends Role {
	public static final String ID = "berserker";

	private static final int AXE_COOLDOWN_TICKS = 32;
	private static final int RAGE_COOLDOWN_TICKS = 16;

	private final Set<UUID> ragingPlayers = new HashSet<>();
	private BukkitTask particleTask;


	public BerserkerRole(Builder builder, Game game) {
		super(builder, game);
	}

	public boolean isRaging(Player player) {
		return ragingPlayers.contains(player.getUniqueId());
	}

	public void setRage(Player player, boolean active) {
		if (active) {
			ragingPlayers.add(player.getUniqueId());
		} else {
			ragingPlayers.remove(player.getUniqueId());
		}
	}

	public int calculateCooldown(Player player) {
		return isRaging(player) ? RAGE_COOLDOWN_TICKS : AXE_COOLDOWN_TICKS;
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		LinkedHashMap<String, GameEvents> map = new LinkedHashMap<>();
		map.put("axe", new BerserkerAxe.Events(game, this));
		map.put("rage_brew", new BerserkerRageBrew.Events(game, this));
		return map;
	}

	@Override
	protected void onRoleStart() {
		particleTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Role.getPlayersOfRole(ID)) {
					if (player == null || !player.isOnline()) continue;
					spawnRageParticles(player);
				}
			}
		}.runTaskTimer(getGame().getPlugin(), 0, 10);
	}

	@Override
	protected void onRoleStop() {
		if (particleTask != null) {
			particleTask.cancel();
			particleTask = null;
		}
		ragingPlayers.clear();
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player player)) return;
		if (!(event.getEntity() instanceof LivingEntity target)) return;
		if (!Role.isPlayerRole(player, ID)) return;

		if (!isRaging(player)) return;

		double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if (maxHealth <= 0) return;

		double healAmount = event.getFinalDamage() * 0.25;
		player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
		spawnLifestealParticles(player, target);
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.8f);
	}

	@EventHandler
	public void onOffHandAttack(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;

		Action action = event.getAction();
		if (!action.isRightClick()) return;

		if (!Role.isPlayerRole(event.getPlayer(), ID)) return;

		Player player = event.getPlayer();

		BerserkerAxe offHandAxe = BerserkerAxe.getItem(player.getInventory().getItemInOffHand(), getGame());
		if (offHandAxe == null) return;

		player.swingOffHand();

		Entity target = player.getTargetEntity(4);
		if (!(target instanceof LivingEntity livingTarget)) return;

		Material offHandAxeMaterial = offHandAxe.getStack().getType();
		if (player.getCooldown(offHandAxeMaterial) > 0) return;
		player.setCooldown(offHandAxeMaterial, calculateCooldown(player));

		BerserkerAxe mainHandAxe = BerserkerAxe.getItem(player.getInventory().getItemInMainHand(), getGame());
		if (mainHandAxe != null) {
			Material mainHandAxeMaterial = mainHandAxe.getStack().getType();
			player.setCooldown(mainHandAxeMaterial, calculateCooldown(player));
		}

		AttributeModifier damageModifier = new AttributeModifier("berserker_offhand_damage", 4.0, AttributeModifier.Operation.ADD_NUMBER);
		AttributeInstance attackDamageAttribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		if (attackDamageAttribute == null) return;
		attackDamageAttribute.addModifier(damageModifier);

		player.attack(target);
		event.setCancelled(true);

		attackDamageAttribute.removeModifier(damageModifier);
	}

	private void spawnLifestealParticles(Player player, LivingEntity target) {
		new BukkitRunnable() {
			double progress = 0.0;
			final double increment = 1.0 / 10;

			public void run() {
				if (progress >= 1.0) {
					this.cancel();
					return;
				}

				Location start = target.getLocation();
				Location end = player.getLocation();

				// Linear interpolation (lerp) between start and end
				double x = start.getX() + (end.getX() - start.getX()) * progress;
				double y = start.getY() + (end.getY() - start.getY()) * progress;
				double z = start.getZ() + (end.getZ() - start.getZ()) * progress;

				Location particleLoc = new Location(start.getWorld(), x, y, z);
				player.getWorld().spawnParticle(
					Particle.REDSTONE,
					particleLoc,
					6, 0.3, 0.3, 0.3,
					new Particle.DustOptions(Color.RED, 1.0f)
				);

				progress += increment;
			}
		}.runTaskTimer(getPlugin(), 0, 1); // Run every tick for 10 ticks
	}

	private void spawnRageParticles(Player player) {
		if (!isRaging(player)) return;

		player.getWorld().spawnParticle(
			Particle.VILLAGER_ANGRY,
			player.getLocation().add(0, 1, 0),
			4, 0.4, 0.4, 0.4
		);
	}


	public static class Builder extends Role.Builder {

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Berserker")
				.color(NamedTextColor.DARK_RED);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Double haches et potion de rage (vampirisme, vitesse, force). Prenez des dégâts pour monter de niveau (Lv.1: 20 | Lv.2: 45 | Lv.3: 80 | Lv.4: 130 | Lv.5: 190) et améliorer vos haches.")
				.color(NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false);
		}

		@Override
		public Role build(Game game) {
			return new BerserkerRole(this, game);
		}
	}
}
