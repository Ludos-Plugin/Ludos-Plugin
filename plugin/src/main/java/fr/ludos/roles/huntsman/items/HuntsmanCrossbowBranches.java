package fr.ludos.roles.huntsman.items;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.core.item.SpecialItemInterface;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public enum HuntsmanCrossbowBranches implements HuntsmanCrossbowBranch {
	FLAME (
		Component.text("Igniting")
			.color(NamedTextColor.RED)
			.decorate(TextDecoration.ITALIC),
		Component.text("Igniting Description"),
		200
	) {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			arrow.setFireTicks(10000);
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level >= 1) {
				Block firedBlock = getBlock(event);
				firedBlock.setType(Material.FIRE);
			}

			if (level >= 2) {
				arrow.getWorld().createExplosion(arrow.getLocation(), 2, true, false, null);
			}
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }
		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onDeselectBranch(SpecialItemInterface item) { }
		@Override
		public void onSelectBranch(SpecialItemInterface item) { }

		@Override
		public void onUnsetLevel(int level, SpecialItemInterface item) { }
		@Override
		public void onSetLevel(int level, SpecialItemInterface item) { }
	},


	REPULSING (
		Component.text("Repulsing")
			.color(NamedTextColor.GRAY)
			.decorate(TextDecoration.ITALIC),
		Component.text("Repulsing Description"),
		200
	) {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			arrow.setKnockbackStrength(level + 1);
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level > 1 && level == maxLevel()) {
				// spawn an AEC that applies levitation as a splash
				PotionEffect levitationEffect = PotionEffectType.LEVITATION.createEffect(10, 2);

				arrow.getWorld().createExplosion(arrow.getLocation(), 0, true, false, null);

				// produce a no-damage "explosion" by applying outward velocity to nearby mobs
				double pushRadius = 5.0;
				double pushStrength = 4.0;
				for (org.bukkit.entity.Entity e : arrow.getNearbyEntities(pushRadius, pushRadius, pushRadius)) {
					if (e == null || /* e.equals(player) ||  */e.equals(arrow)) continue;
					if (!(e instanceof org.bukkit.entity.LivingEntity livingEntity)) continue;

					livingEntity.addPotionEffect(levitationEffect);

					org.bukkit.util.Vector dir = e.getLocation().toVector().subtract(arrow.getLocation().toVector());
					if (dir.lengthSquared() == 0) {
						// pick a random small push if exactly on center
						dir = new org.bukkit.util.Vector((Math.random() - 0.5), 0.2, (Math.random() - 0.5));
					}
					dir.normalize().multiply(pushStrength);
					// give a little upward lift so mobs are pushed away nicely
					dir.setY(Math.max(dir.getY(), 0.4));
					e.setVelocity(dir);
				}
			}
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }
		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onDeselectBranch(SpecialItemInterface item) { }
		@Override
		public void onSelectBranch(SpecialItemInterface item) { }

		@Override
		public void onUnsetLevel(int level, SpecialItemInterface item) { }
		@Override
		public void onSetLevel(int level, SpecialItemInterface item) { }
	},


	SLOWNESS (
		Component.text("Impeding")
			.color(NamedTextColor.DARK_BLUE)
			.decorate(TextDecoration.ITALIC),
		Component.text("Impeding Description"),
		200
	) {
		@Override
		public void processShotArrow(Arrow arrow, HumanEntity player, int level, EntityShootBowEvent event) {
			PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 7, 2);
			arrow.addCustomEffect(slowEffect, true);

			if (level >= 1) {
				PotionEffect coldEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, 20 * 7, 2);
				arrow.addCustomEffect(coldEffect, true);
			}
		}

		@Override
		public void processLandedArrow(Arrow arrow, HumanEntity player, int level, ProjectileHitEvent event) {
			if (level >= 2) {
				Random random = new Random();

				Block webCenter = event.getEntity().getLocation().getBlock();
				int thickness = 2;

				for (int i = -thickness; i <= thickness; i++) {
				for (int j = -thickness; j <= thickness; j++) {
				for (int k = -thickness; k <= thickness; k++) {
					Block block = webCenter.getRelative(i, j, k);
					float distanceFromCenter = Math.max(Math.max(Math.abs(i), Math.abs(j)), Math.abs(k));
					float distanceSpawnFactor = distanceFromCenter / (thickness + 1);

					if (block.getType().isAir() && ! block.getType().isSolid() && random.nextFloat() > distanceSpawnFactor) {
						block.setType(Material.COBWEB);
					}
				}
				}
				}
			}
		}

		@Override
		public void onEquip(SpecialItemInterface item) { }
		@Override
		public void onUnequip(SpecialItemInterface item) { }

		@Override
		public void onDeselectBranch(SpecialItemInterface item) { }
		@Override
		public void onSelectBranch(SpecialItemInterface item) { }

		@Override
		public void onUnsetLevel(int level, SpecialItemInterface item) { }
		@Override
		public void onSetLevel(int level, SpecialItemInterface item) { }
	};



	private Component name;
	public Component getName() {
		return name;
	}

	private Component description;
	public Component getDescription() {
		return description;
	}

	private double xpThreshold;
	public double xpThreshold(Integer level) {
		return xpThreshold;
	}


	private HuntsmanCrossbowBranches(Component name, Component description, double xpThreshold) {
		this.name = name;
		this.xpThreshold = xpThreshold;
		this.description = description;
	}


	private static Block getBlock(ProjectileHitEvent event) {
		Block block;
		if (event.getHitBlock() != null) {
			block = event.getHitBlock().getRelative(event.getHitBlockFace());
		}
		else if (event.getHitEntity() != null ) {
			block = event.getHitEntity().getLocation().getBlock();
		}
		else {
			block = event.getEntity().getLocation().getBlock();
		}

		return block;
	}

	@Override
	public String id() {
		return name();
	}

	public int maxLevel() {
		return 2;
	}
}