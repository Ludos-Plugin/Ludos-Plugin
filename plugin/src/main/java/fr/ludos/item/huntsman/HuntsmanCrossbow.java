package fr.ludos.item.huntsman;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.BranchLevelItem;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HuntsmanCrossbow extends BranchLevelItem<HuntsmanCrossbowBranches> {

	public HuntsmanCrossbow(ItemStack stack) throws IllegalArgumentException {
		super(stack);
	}


	public HuntsmanCrossbow(Player owner) {
		this(owner, HuntsmanCrossbowBranches.FLAME);
	}
	protected HuntsmanCrossbow(Player owner, int[] levels) {
		this(owner, HuntsmanCrossbowBranches.FLAME, levels);
	}

	protected HuntsmanCrossbow(Player owner, HuntsmanCrossbowBranches branch) {
		this(owner, branch, new int[HuntsmanCrossbowBranches.values.length]);
	}

	protected HuntsmanCrossbow(Player owner, HuntsmanCrossbowBranches branch, int[] levels) {
		this(owner, branch, levels, new double[levels.length]);
	}

	protected HuntsmanCrossbow(Player owner, HuntsmanCrossbowBranches branch, int[] levels, double[] xps) {
		super(new ItemStack(Material.CROSSBOW), owner, branch, levels, xps);
	}

	@Override
	public HuntsmanCrossbowBranches convertToBranch(int level) {
		return HuntsmanCrossbowBranches.findByKey(level);
	}

	@Override
	protected HuntsmanCrossbowBranches[] getBranches() {
		return HuntsmanCrossbowBranches.values;
	}


	@Override
	public String getId() {
		return "manhuntHuntsmanCrossbow";
	}

	@Override
	protected String getName() {
		return "Cursed Crossbow " + getBranchAnnotation(); // TODO: Translate
	}

	@Override
	protected List<String> getLore() {
		List<String> lore = super.getLore();
		if (lore == null) {
			lore = new ArrayList<String>();
		}

		String hintFormatted = ChatColor.GRAY + "Press " + ChatColor.YELLOW + "Left Click (MB1) " + ChatColor.GRAY + "to Switch Mode";

		lore.add(hintFormatted);
		return lore;
	}


	@Nullable
	public static HuntsmanCrossbow getItem(ItemStack stack) {
		try {
			HuntsmanCrossbow bow = new HuntsmanCrossbow(stack);
			return bow;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static HuntsmanCrossbow createItem(Player owner, int[] levels) {
		return new HuntsmanCrossbow(owner, levels);
	}




	public static class Events extends BranchLevelItem.Events<HuntsmanCrossbow, HuntsmanCrossbowBranches> {

		public static final String ARROW_TYPE = "arrow_type";
		public final NamespacedKey arrowTypeKey = new NamespacedKey(Ludos.getInstance(), ARROW_TYPE);

		public static final String ARROW_LEVEL = "arrow_level";
		public final NamespacedKey arrowLevelKey = new NamespacedKey(Ludos.getInstance(), ARROW_LEVEL);

		// private BukkitTask saturationTask;


		public Events(Game game) {
			super(game);
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			LivingEntity entity = event.getEntity();
			if (! (entity instanceof HumanEntity player)) return;

			HuntsmanCrossbow crossbow = getItem(event.getBow());
			if (crossbow == null) return;

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);

			PersistentDataContainer container = arrowProjectile.getPersistentDataContainer();
			HuntsmanCrossbowBranches branch = crossbow.getBranch();

			int level = crossbow.getCurrentBranchLevel();

			if (! player.hasCooldown(Material.CROSSBOW))  {
				container.set(arrowTypeKey, PersistentDataType.INTEGER, branch.index());
				container.set(arrowLevelKey, PersistentDataType.INTEGER, level);
				arrowProjectile.setGravity(false);
				arrowProjectile.setDamage(4);
				branch.processShotArrow(arrowProjectile, player, level, event);

				player.setCooldown(Material.CROSSBOW, 200);
			}
		}

		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			Projectile arrowProjectile = event.getEntity();
			if (! (arrowProjectile instanceof Arrow arrow)) return;

			ProjectileSource source = arrowProjectile.getShooter();
			if (! (source instanceof Player player)) return;

			HuntsmanCrossbow crossbow = HuntsmanCrossbow.findIn(player.getInventory(), HuntsmanCrossbow::getItem);
			if (crossbow == null) return;


			Entity hitEntity = event.getHitEntity();
			if (hitEntity != null) {
				if (hitEntity instanceof LivingEntity livingEntity) {
					double xp = livingEntity.isDead() ? 1.25d : 1d;
					xp *= livingEntity.getLocation().distance(player.getLocation());
					crossbow.addXp(xp);
				}
			}


			PersistentDataContainer container = arrow.getPersistentDataContainer();
			if (container.has(arrowTypeKey, PersistentDataType.INTEGER) && container.has(arrowLevelKey, PersistentDataType.INTEGER)) {
				HuntsmanCrossbowBranches.values[container.get(arrowTypeKey, PersistentDataType.INTEGER)]
					.processLandedArrow(arrow, player, container.get(arrowLevelKey, PersistentDataType.INTEGER), event);
			}

		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) return;

			Player player = event.getPlayer();

			HuntsmanCrossbow crossbow = getItem(player.getInventory().getItemInMainHand());
			if (crossbow == null) return;

			if (! player.hasCooldown(crossbow.getStack().getType())) {
				crossbow.cycleBranch();

				player.setCooldown(crossbow.getStack().getType(), 5);
			}
		}

		@EventHandler
		public void onItemDrop(PlayerDropItemEvent event) {
			if (! canPlayerHaveItem(event.getPlayer())) return;

			if (event.getItemDrop().getItemStack().getType() == Material.ARROW) {
				event.setCancelled(true);
			}
		}


		@Override
		protected HuntsmanCrossbowBranches[] getBranches() {
			return HuntsmanCrossbowBranches.values;
		}


		@Override
		@Nullable
		protected HuntsmanCrossbow getItem(ItemStack stack) {
			return HuntsmanCrossbow.getItem(stack);
		}
		@Override
		protected HuntsmanCrossbow createItem(Player owner, int[] levels) {
			return HuntsmanCrossbow.createItem(owner, levels);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}
