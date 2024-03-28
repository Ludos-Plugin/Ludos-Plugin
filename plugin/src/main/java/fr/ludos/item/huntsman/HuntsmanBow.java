package fr.ludos.item.huntsman;

import fr.ludos.Main;
import fr.ludos.item.BranchItem;
import fr.ludos.item.LevelItem;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class HuntsmanBow extends BranchItem<HuntsmanBowBranches> {

	public HuntsmanBow(ItemStack stack) throws IllegalArgumentException {
		super(stack);
	}


	public HuntsmanBow(Player owner) {
		this(owner, HuntsmanBowBranches.FLAME);
	}
	protected HuntsmanBow(Player owner, int[] levels) {
		this(owner, HuntsmanBowBranches.FLAME, levels);
	}

	protected HuntsmanBow(Player owner, HuntsmanBowBranches branch) {
		this(owner, branch, new int[HuntsmanBowBranches.values.length]);
	}

	protected HuntsmanBow(Player owner, HuntsmanBowBranches branch, int[] levels) {
		this(owner, branch, levels, new double[levels.length]);
	}

	protected HuntsmanBow(Player owner, HuntsmanBowBranches branch, int[] levels, double[] xps) {
		super(new ItemStack(Material.BOW), owner, branch, levels, xps);
	}

	@Override
	public HuntsmanBowBranches convertToBranch(int level) {
		return HuntsmanBowBranches.findByKey(level);
	}

	@Override
	protected HuntsmanBowBranches[] getBranches() {
		return HuntsmanBowBranches.values;
	}


	@Override
	public String getId() {
		return "manhunt_huntsman_bow";
	}

	@Override
	protected String getName() {
		return "Stolen Bow"; // TODO: Translate
	}


	@Nullable
	public static HuntsmanBow getItem(ItemStack stack) {
		try {
			HuntsmanBow bow = new HuntsmanBow(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static HuntsmanBow createItem(Player owner, int[] levels) {
		return new HuntsmanBow(owner, levels);
	}




	public static class Events extends BranchItem.Events<HuntsmanBow, HuntsmanBowBranches> {

		public static final String ARROW_TYPE = "arrow_type";
		public final NamespacedKey arrowTypeKey = new NamespacedKey(Main.getInstance(), ARROW_TYPE);

		public static final String ARROW_LEVEL = "arrow_level";
		public final NamespacedKey arrowLevelKey = new NamespacedKey(Main.getInstance(), ARROW_LEVEL);


		public Events() {
			super(HuntsmanRole.id);
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			LivingEntity entity = event.getEntity();
			if (! (entity instanceof Player player)) {
				return;
			}

			HuntsmanBow bow = getItem(event.getBow());
			if (bow == null) {
				return;
			}

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);

			PersistentDataContainer container = arrowProjectile.getPersistentDataContainer();
			HuntsmanBowBranches branch = bow.getBranch();

			int level = bow.getCurrentBranchLevel();

			container.set(arrowTypeKey, PersistentDataType.INTEGER, branch.index());
			container.set(arrowLevelKey, PersistentDataType.INTEGER, level);
			branch.processShotArrow(arrowProjectile, player, level);

			updateArrowCount(player);
		}

		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			Projectile arrowProjectile = event.getEntity();
			if (! (arrowProjectile instanceof Arrow arrow)) {
				return;
			}

			ProjectileSource source = arrowProjectile.getShooter();
			if (! (source instanceof Player player)) {
				return;
			}

			HuntsmanBow bow = HuntsmanBow.findIn(player.getInventory(), HuntsmanBow::getItem);
			if (bow == null) {
				return;
			}


			Entity hitEntity = event.getHitEntity();
			if (hitEntity != null) {
				if (hitEntity instanceof LivingEntity livingEntity) {
					double xp = livingEntity.isDead() ? 1.25d : 1d;
					xp *= livingEntity.getLocation().distance(player.getLocation());
					bow.addXp(xp);
				}

				Player shooterPlayer = player;
				shooterPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 50, 2));
			}


			PersistentDataContainer container = arrow.getPersistentDataContainer();
			if (container.has(arrowTypeKey, PersistentDataType.INTEGER) && container.has(arrowLevelKey, PersistentDataType.INTEGER)) {
				HuntsmanBowBranches.values[container.get(arrowTypeKey, PersistentDataType.INTEGER)]
					.processLandedArrow(arrow, player, container.get(arrowLevelKey, PersistentDataType.INTEGER), hitEntity);
			}

		}

		@EventHandler
		public void onItemDrop(PlayerDropItemEvent event) {
			if (! Role.isPlayerRole(event.getPlayer(), roleId)) {
				return;
			}

			if (event.getItemDrop().getItemStack().getType() == Material.ARROW) {
				event.setCancelled(true);
			}
		}

		@Override
		public void updateItemInInventory(Player player) {
			super.updateItemInInventory(player);
			updateArrowCount(player);
		}

		private void updateArrowCount(Player player) {
			player.getInventory().remove(Material.ARROW);
			player.getInventory().addItem(new ItemStack(Material.ARROW));
		}

		@Override
		protected HuntsmanBowBranches[] getBranches() {
			return HuntsmanBowBranches.values;
		}

		@Override
		@Nullable
		protected HuntsmanBow getItem(ItemStack stack) {
			return HuntsmanBow.getItem(stack);
		}

		@Override
		protected HuntsmanBow createItem(Player owner, int[] levels) {
			return HuntsmanBow.createItem(owner, levels);
		}

	}
}
