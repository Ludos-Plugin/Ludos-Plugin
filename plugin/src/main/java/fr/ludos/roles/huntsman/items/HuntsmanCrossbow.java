package fr.ludos.roles.huntsman.items;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

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
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelValue;
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Crossbow, for use by any Player with {@link HuntsmanRole}.
 */
public class HuntsmanCrossbow extends MultiLevelBranchItem<HuntsmanCrossbowBranch> {
	public static final String ID = "huntsman_crossbow";

	// private final static Map<UUID, HuntsmanCrossbow> cachedItems = new HashMap<>();


	public static HuntsmanCrossbow fromItemStack(Map<String, HuntsmanCrossbowBranch> branchMap, HuntsmanCrossbowBranch defaultBranch, ItemStack stack, Game game) throws IllegalArgumentException {
		final UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HuntsmanCrossbow cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		final Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		final String branchKey = BranchItemInterface.branchFromItemStack(stack, game);
		if (branchKey == null) return null;
		final Map<String, LevelValue> levels = MultiLevelBranchItem.levelsFromItemStack(stack, ID, game);
		if (levels == null) return null;

		final HuntsmanCrossbowBranch branch = branchMap.getOrDefault(branchKey, defaultBranch);

		final HuntsmanCrossbow crossbow = new HuntsmanCrossbow(branchMap, branch, levels, stack, owner, game);
		// cachedItems.put(itemId, crossbow);

		return crossbow;
	}
	public static HuntsmanCrossbow createItem(Map<String, HuntsmanCrossbowBranch> branchMap, HuntsmanCrossbowBranch defaultBranch, Map<String, LevelValue> levels, Player owner, Game game) {
		final HuntsmanCrossbow crossbow = new HuntsmanCrossbow(branchMap, defaultBranch, levels, new ItemStack(Material.CROSSBOW), owner, game);
		final UUID itemId = crossbow.initializeItem();

		// cachedItems.put(itemId, crossbow);

		return crossbow;
	}


	protected HuntsmanCrossbow(Map<String, HuntsmanCrossbowBranch> branches, HuntsmanCrossbowBranch branch, Map<String, LevelValue> levels, ItemStack stack, Player owner, Game game) {
		super(branches, branch, levels, stack, owner, game);
	}


	@Override
	public String getTypeId() {
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

		lore.add(BranchItemInterface.getCycleBranchAnnotation("key.attack"));
		return lore;
	}

	/**
	 * Events for the {@link HuntsmanCrossbow}.
	 */
	public static class Events extends MultiLevelBranchItem.Events<HuntsmanCrossbow, HuntsmanCrossbowBranch> {
		private static final List<HuntsmanCrossbowBranch> BRANCHES = Arrays.asList(HuntsmanCrossbowBranches.values());

		public static final String ARROW_TYPE = "arrow_type";
		public final NamespacedKey arrowTypeKey = new NamespacedKey(Ludos.NAMESPACE, ARROW_TYPE);

		public static final String ARROW_LEVEL = "arrow_level";
		public final NamespacedKey arrowLevelKey = new NamespacedKey(Ludos.NAMESPACE, ARROW_LEVEL);

		// private BukkitTask saturationTask;


		public Events(Game game) {
			super(BRANCHES, game, new Events.Info(ItemSlot.HOTBAR_2));
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
			container.set(arrowTypeKey, PersistentDataType.STRING, branch.id());
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
			if (container.has(arrowTypeKey, PersistentDataType.STRING) && container.has(arrowLevelKey, PersistentDataType.INTEGER)) {
				String branchKey = container.get(arrowTypeKey, PersistentDataType.STRING);
				int levelIdx = container.get(arrowLevelKey, PersistentDataType.INTEGER);

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
		@Nullable
		public HuntsmanCrossbow getItem(ItemStack stack) {
			return HuntsmanCrossbow.fromItemStack(getBranches(), getDefaultBranch(), stack, game);
		}
		@Override
		public HuntsmanCrossbow createItem(Player owner, Map<String, LevelValue> levels) {
			return HuntsmanCrossbow.createItem(getBranches(), getDefaultBranch(), levels, owner, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HuntsmanRole.ID);
		}
	}
}
