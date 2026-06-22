package fr.ludos.roles.huntsman.items;

import java.util.List;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.LevelItem;
import fr.ludos.core.item.MultiLevelBranchItem;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.role.Role;
import fr.ludos.roles.huntsman.HuntsmanRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;


public class HuntsmanCrossbow extends MultiLevelBranchItem<HuntsmanCrossbowBranches> {
	public static final String ID = "manhuntHuntsmanCrossbow";

	// private final static Map<UUID, HuntsmanCrossbow> cachedItems = new HashMap<>();


	public static HuntsmanCrossbow fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// HuntsmanCrossbow cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		Integer branchIndex = MultiLevelBranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;
		LevelItem.LevelState[] levels = MultiLevelBranchItem.levelsFromItemStack(stack, ID, game);
		if (levels == null) return null;

		HuntsmanCrossbow crossbow = new HuntsmanCrossbow(stack, owner, HuntsmanCrossbowBranches.values()[branchIndex], levels, game);
		// cachedItems.put(itemId, crossbow);

		return crossbow;
	}
	public static HuntsmanCrossbow createItem(Player owner, LevelItem.LevelState[] levels, Game game) {
		HuntsmanCrossbow crossbow = new HuntsmanCrossbow(new ItemStack(Material.CROSSBOW), owner, HuntsmanCrossbowBranches.FLAME, levels, game);
		UUID itemId = crossbow.initializeItem();

		// cachedItems.put(itemId, crossbow);

		return crossbow;
	}


	protected HuntsmanCrossbow(ItemStack stack, Player owner, HuntsmanCrossbowBranches branch, LevelItem.LevelState[] levels, Game game) {
		super(HuntsmanCrossbowBranches.class, stack, owner, branch, levels, game);
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

		lore.add(getCycleBranchAnnotation("key.attack"));
		return lore;
	}


	public static class Events extends MultiLevelBranchItem.Events<HuntsmanCrossbow, HuntsmanCrossbowBranches> {
		public static final String ARROW_TYPE = "arrow_type";
		public final NamespacedKey arrowTypeKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ARROW_TYPE);

		public static final String ARROW_LEVEL = "arrow_level";
		public final NamespacedKey arrowLevelKey = new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), ARROW_LEVEL);

		// private BukkitTask saturationTask;


		public Events(Game game) {
			super(game, ItemSlot.HOTBAR_2);
		}


		@EventHandler
		public void onShootArrow(EntityShootBowEvent event) {
			if (! (event.getEntity() instanceof Player player)) return;
			if (! isPlayerValid(player)) return;

			if (player.hasCooldown(Material.CROSSBOW)) return;

			HuntsmanCrossbow crossbow = getItem(event.getBow());
			if (crossbow == null) return;

			Arrow arrowProjectile = (Arrow) event.getProjectile();
			HuntsmanCrossbowBranches branch = crossbow.getBranch();
			PersistentDataContainer container = arrowProjectile.getPersistentDataContainer();
			int level = crossbow.getLevelState().getLevel();

			arrowProjectile.setPickupStatus(PickupStatus.DISALLOWED);
			arrowProjectile.setGravity(false);
			arrowProjectile.setDamage(4);
			container.set(arrowTypeKey, PersistentDataType.INTEGER, branch.ordinal());
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
			if (container.has(arrowTypeKey, PersistentDataType.INTEGER) && container.has(arrowLevelKey, PersistentDataType.INTEGER)) {
				getBranches()[container.get(arrowTypeKey, PersistentDataType.INTEGER)]
					.processLandedArrow(arrow, player, container.get(arrowLevelKey, PersistentDataType.INTEGER), event);
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
		protected HuntsmanCrossbowBranches[] getBranches() {
			return HuntsmanCrossbowBranches.values();
		}


		@Override
		@Nullable
		public HuntsmanCrossbow getItem(ItemStack stack) {
			return HuntsmanCrossbow.fromItemStack(stack, game);
		}
		@Override
		public HuntsmanCrossbow createItem(Player owner, LevelItem.LevelState[] levels) {
			return HuntsmanCrossbow.createItem(owner, levels, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, HuntsmanRole.id);
		}
	}
}
