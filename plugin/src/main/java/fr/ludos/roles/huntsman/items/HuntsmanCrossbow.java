package fr.ludos.roles.huntsman.items;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.MultiLevelBranchItem;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Crossbow, for use by any Player with {@link HuntsmanRole}.
 */
public class HuntsmanCrossbow extends MultiLevelBranchItem<HuntsmanCrossbow, HuntsmanCrossbowBranch> {
	public static final String ID = "huntsman_crossbow";



	protected HuntsmanCrossbow(ItemData<HuntsmanCrossbowBranch> info, Events events) {
		super(info, events);
	}


	@Override
	public Component getName() {
		return
			Component.text("Cursed Crossbow ")
			.append(getBranchAnnotation())
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(BranchItemInterface.getCycleBranchAnnotation("key.attack"));
		return lore;
	}

	/**
	 * Events for the {@link HuntsmanCrossbow}.
	 */
	public static class Events extends MultiLevelBranchItem.Events<HuntsmanCrossbow, HuntsmanCrossbowBranch> {
		private static final List<HuntsmanCrossbowBranch> BRANCHES = Arrays.asList(HuntsmanCrossbowBranches.values());

		public static final String ARROW_TYPE_STRING = "arrow_type";
		public static final NamespacedKey ARROW_TYPE = new NamespacedKey(Ludos.NAMESPACE, ARROW_TYPE_STRING);

		public static final String ARROW_LEVEL_STRING = "arrow_level";
		public static final NamespacedKey ARROW_LEVEL = new NamespacedKey(Ludos.NAMESPACE, ARROW_LEVEL_STRING);

		// private BukkitTask saturationTask;


		public Events(Game game) {
			super(BRANCHES, game, new Events.Info(ItemSlot.HOTBAR_2));
		}

		@Override
		public String getTypeId() {
			return ID;
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof Player player)) return;
			if (! isPlayerValid(player)) return;

			if (player.hasCooldown(Material.CROSSBOW)) return;

			HuntsmanCrossbow crossbow = getItem(event.getBow());
			if (crossbow == null) return;

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			HuntsmanCrossbowBranch branch = crossbow.getBranch();
			PersistentDataContainer container = arrowProjectile.getPersistentDataContainer();
			int level = crossbow.levelState().level();

			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			arrowProjectile.setGravity(false);
			arrowProjectile.setDamage(4);
			container.set(ARROW_TYPE, PersistentDataType.STRING, branch.id());
			container.set(ARROW_LEVEL, PersistentDataType.INTEGER, level);

			branch.processShotArrow(arrowProjectile, player, level, event);
			player.setCooldown(Material.CROSSBOW, 200);
		}

		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			Projectile arrowProjectile = event.getEntity();
			if (! (arrowProjectile instanceof Arrow arrow)) return;

			ProjectileSource source = arrowProjectile.getShooter();
			if (! (source instanceof Player player)) return;

			if (! isPlayerValid(player)) return;

			HuntsmanCrossbow crossbow = HuntsmanCrossbow.findIn(player.getInventory(), this::getItem);
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
			if (container.has(ARROW_TYPE, PersistentDataType.STRING) && container.has(ARROW_LEVEL, PersistentDataType.INTEGER)) {
				String branchKey = container.get(ARROW_TYPE, PersistentDataType.STRING);
				int levelIdx = container.get(ARROW_LEVEL, PersistentDataType.INTEGER);

				container.remove(ARROW_TYPE);
				container.remove(ARROW_LEVEL);
				arrowProjectile.setGravity(true);

				HuntsmanCrossbowBranch branch = getBranches().get(branchKey);
				if (branch != null) {
					branch.processLandedArrow(arrow, player, levelIdx, event);
				}
			}

		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			Action action = event.getAction();
			if (! action.isLeftClick()) return;

			HuntsmanCrossbow crossbow = getItem(player.getInventory().getItemInMainHand());
			if (crossbow == null) return;

			if (! crossbow.refreshUseCooldown()) return;
			event.setCancelled(true);

			crossbow.cycleBranch();
		}

		@Override
		protected HuntsmanCrossbow getItemInternal(ItemData<HuntsmanCrossbowBranch> info) {
			return new HuntsmanCrossbow(info, this);
		}

		@Override
		protected HuntsmanCrossbow createItemInternal(MultiLevelData levels, BranchData<HuntsmanCrossbowBranch> data, Player owner) {
			return new HuntsmanCrossbow(new ItemData<>(levels, data, new SpecialItem.ItemData(new ItemStack(Material.CROSSBOW), owner)), this);
		}


		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HuntsmanRole.ID);
		}
	}
}
