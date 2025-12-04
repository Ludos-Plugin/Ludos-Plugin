package fr.ludos.item.huntsman;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.MultiLevelBranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


public class HuntsmanCrossbow extends MultiLevelBranchItem<HuntsmanCrossbowBranches> {
	public static final String ID = "manhuntHuntsmanCrossbow";


	public static HuntsmanCrossbow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;
		Integer branchIndex = MultiLevelBranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;
		Pair<int[], double[]> levels = MultiLevelBranchItem.levelsFromItemStack(stack, ID, game);
		if (levels == null) return null;

		return new HuntsmanCrossbow(stack, owner, HuntsmanCrossbowBranches.findByKey(branchIndex), levels.getLeft(), levels.getRight(), game);
	}
	public static HuntsmanCrossbow createItem(Player owner, int[] levels, Game game) {
		HuntsmanCrossbow crossbow = new HuntsmanCrossbow(new ItemStack(Material.CROSSBOW), owner, HuntsmanCrossbowBranches.findByKey(0), levels, new double[HuntsmanCrossbowBranches.values.length], game);
		crossbow.initializeItem();

		return crossbow;
	}


	protected HuntsmanCrossbow(ItemStack stack, Player owner, HuntsmanCrossbowBranches branch, int[] levels, double[] xps, Game game) {
		super(stack, owner, branch, levels, xps, game);
	}


	@Override
	public String getId() {
		return ID;
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

		lore.add(getCycleBranchAnnotation("key.use"));
		return lore;
	}


	@Override
	public HuntsmanCrossbowBranches convertToBranch(int level) {
		return HuntsmanCrossbowBranches.findByKey(level);
	}

	@Override
	protected HuntsmanCrossbowBranches[] getBranches() {
		return HuntsmanCrossbowBranches.values;
	}


	public static class Events extends MultiLevelBranchItem.Events<HuntsmanCrossbow, HuntsmanCrossbowBranches> {
		public static final String ARROW_TYPE = "arrow_type";
		public final NamespacedKey arrowTypeKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ARROW_TYPE);

		public static final String ARROW_LEVEL = "arrow_level";
		public final NamespacedKey arrowLevelKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ARROW_LEVEL);

		// private BukkitTask saturationTask;


		public Events(Game game) {
			super(game, 1);
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof HumanEntity player)) return;

			if (player.hasCooldown(Material.CROSSBOW)) return;

			HuntsmanCrossbow crossbow = getItem(event.getBow(), game);
			if (crossbow == null) return;

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			HuntsmanCrossbowBranches branch = crossbow.getBranch();
			PersistentDataContainer container = arrowProjectile.getPersistentDataContainer();
			int level = crossbow.getCurrentBranchLevel();

			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			arrowProjectile.setGravity(false);
			arrowProjectile.setDamage(4);
			container.set(arrowTypeKey, PersistentDataType.INTEGER, branch.index());
			container.set(arrowLevelKey, PersistentDataType.INTEGER, level);

			branch.processShotArrow(arrowProjectile, player, level, event);
			player.setCooldown(Material.CROSSBOW, 200);
		}

		@EventHandler
		public void onProjectileHit(ProjectileHitEvent event) {
			Projectile arrowProjectile = event.getEntity();
			if (! (arrowProjectile instanceof Arrow arrow)) return;

			ProjectileSource source = arrowProjectile.getShooter();
			if (! (source instanceof Player player)) return;

			HuntsmanCrossbow crossbow = HuntsmanCrossbow.findIn(player.getInventory(), (ItemStack stack) -> getItem(stack, game));
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
			if (! action.isLeftClick()) return;

			Player player = event.getPlayer();

			HuntsmanCrossbow crossbow = getItem(player.getInventory().getItemInMainHand(), game);
			if (crossbow == null) return;

			if (! crossbow.refreshUseCooldown()) return;
			event.setCancelled(true);

			crossbow.cycleBranch();
		}


		@Override
		protected HuntsmanCrossbowBranches[] getBranches() {
			return HuntsmanCrossbowBranches.values;
		}


		@Override
		@Nullable
		protected HuntsmanCrossbow getItem(ItemStack stack, Game game) {
			return HuntsmanCrossbow.fromItemStack(stack, game);
		}
		@Override
		protected HuntsmanCrossbow createItem(Player owner, int[] levels, Game game) {
			return HuntsmanCrossbow.createItem(owner, levels, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}
