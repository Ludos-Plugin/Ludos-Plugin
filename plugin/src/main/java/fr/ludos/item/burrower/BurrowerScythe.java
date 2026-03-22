package fr.ludos.item.burrower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.LevelItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BurrowerScythe extends LevelItem<BurrowerScytheLevels> {
	public static final String ID = "burrower_scythe";

	private static final double SCYTHE_RANGE = 5.0;
	private static final double SCYTHE_RANGE_SQUARED = SCYTHE_RANGE * SCYTHE_RANGE;
	private static final int SCYTHE_MAX_TARGETS = 5;
	private static final double SCYTHE_CONE_DOT = 0.35;
	private static final int WALL_COOLDOWN_TICKS = 20 * 8;
	private static final int WALL_DISTANCE = 2;
	private static final int WALL_WIDTH = 5;
	private static final int WALL_HEIGHT = 3;
	private static final int WALL_DURATION_TICKS = 20 * 4;
	private static final double LIGHTNING_PROC_CHANCE = 0.05;
	private static final double LIGHTNING_TRUE_DAMAGE = 3.0;

	// private final static Map<UUID, BurrowerPick> cachedItems = new HashMap<>();


	public static BurrowerScythe fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// BurrowerScythe cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		LevelState levelState = LevelItem.levelFromItemStack(stack, game);
		if (levelState == null) return null;

		BurrowerScythe burrowerScythe = new BurrowerScythe(stack, owner, levelState, game);
		// cachedItems.put(itemId, burrowerScythe);

		return burrowerScythe;
	}

	public static BurrowerScythe createItem(Player owner, LevelState level, Game game) {
		BurrowerScytheLevels lvl = BurrowerScytheLevels.values()[level.getLevel()];
		BurrowerScythe burrowerScythe = new BurrowerScythe(new ItemStack(lvl.getMaterial()), owner, level, game);
		UUID itemId = burrowerScythe.initializeItem();

		// cachedItems.put(itemId, burrowerScythe);

		return burrowerScythe;
	}

	protected BurrowerScythe(ItemStack stack, Player owner, LevelState level, Game game) {
		super(BurrowerScytheLevels.class, stack, owner, level, game);
	}

	@Override
	protected String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return Component.text("Burrower's Scythe")
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	private record BlockSnapshot(Block block, Material type, BlockData data) { }

	public Map<Block, BlockSnapshot> castEarthWall() {
		Player owner = getOwner();
		if (owner == null || owner.getWorld() == null) return Map.of();

		Vector forward = getForwardVector(owner);
		Vector right = getRightVector(forward);

		int forwardX = forward.getBlockX();
		int forwardZ = forward.getBlockZ();
		int rightX = right.getBlockX();
		int rightZ = right.getBlockZ();

		Block anchor = owner.getLocation().getBlock().getRelative(forwardX * WALL_DISTANCE, 0, forwardZ * WALL_DISTANCE);
		Map<Block, BlockSnapshot> replacedStates = new HashMap<>();

		int halfWidth = WALL_WIDTH / 2;
		for (int lateral = -halfWidth; lateral <= halfWidth; lateral++) {
			Block columnBase = anchor.getRelative(rightX * lateral, 0, rightZ * lateral);
			liftGroundColumn(columnBase, replacedStates);
		}

		return replacedStates;
	}

	private void liftGroundColumn(Block base, Map<Block, BlockSnapshot> replacedStates) {
		Material fallback = resolveWallMaterial(base);
		Material[] liftedMaterials = new Material[WALL_HEIGHT];

		List<Block> blocksSource = new ArrayList<>();
		List<Block> blocksDestination = new ArrayList<>();

		for (int layer = 0; layer < WALL_HEIGHT; layer++) {
			int sourceDepth = WALL_HEIGHT - 1 - layer;
			blocksSource.add(base.getRelative(BlockFace.DOWN, sourceDepth));
			blocksDestination.add(base.getRelative(BlockFace.UP, layer));

			snapshotBlock(replacedStates, blocksSource.get(layer));
			snapshotBlock(replacedStates, blocksDestination.get(layer));

			Material sourceType = blocksSource.get(layer).getType();
			if (sourceType == Material.AIR || sourceType == Material.CAVE_AIR || sourceType == Material.VOID_AIR) {
				sourceType = fallback;
			}

			liftedMaterials[layer] = sourceType;
		}

		for (int depth = 1; depth < WALL_HEIGHT; depth++) {
			snapshotBlock(replacedStates, blocksSource.get(depth));
			blocksSource.get(depth).setType(Material.AIR, false);
		}

		for (int layer = 0; layer < WALL_HEIGHT; layer++) {
			blocksDestination.get(layer).setType(liftedMaterials[layer], false);
		}
	}

	private void snapshotBlock(Map<Block, BlockSnapshot> replacedStates, Block block) {
		replacedStates.putIfAbsent(block, new BlockSnapshot(block, block.getType(), block.getBlockData().clone()));
	}

	private static void restoreSnapshots(Map<Block, BlockSnapshot> snapshots) {
		for (BlockSnapshot snapshot : snapshots.values()) {
			Block block = snapshot.block();

			if (!block.getChunk().isLoaded()) {
				block.getChunk().load();
			}

			block.setType(snapshot.type(), false);
			block.setBlockData(snapshot.data(), false);
		}
	}

	private Vector getForwardVector(Player player) {
		Vector direction = player.getLocation().getDirection().setY(0);
		if (direction.lengthSquared() <= 0.0001) {
			return new Vector(0, 0, 1);
		}

		direction.normalize();
		if (Math.abs(direction.getX()) >= Math.abs(direction.getZ())) {
			return new Vector(Math.signum(direction.getX()), 0, 0);
		}

		return new Vector(0, 0, Math.signum(direction.getZ()));
	}

	private Vector getRightVector(Vector forward) {
		return new Vector(-forward.getZ(), 0, forward.getX());
	}

	private Material resolveWallMaterial(Block base) {
		Material below = base.getRelative(BlockFace.DOWN).getType();

		if (below.isSolid() && below != Material.BEDROCK) return below;

		return Material.DIRT;
	}


	public static class Events extends LevelItem.Events<BurrowerScythe, BurrowerScytheLevels> {
		private final Set<UUID> slashingPlayers = new HashSet<>();
		private final Map<UUID, BukkitTask> activeWallTasks = new HashMap<>();
		private final Map<UUID, Map<Block, BlockSnapshot>> activeWallSnapshots = new HashMap<>();

		public Events(Game game) {
			super(game, 0);
		}

		@Override
		protected BurrowerScythe createItem(Player owner, LevelState level, Game game) {
			return BurrowerScythe.createItem(owner, level, game);
		}

		@Override
		@Nullable
		protected BurrowerScythe getItem(ItemStack stack, Game game) {
			return BurrowerScythe.fromItemStack(stack, game);
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Player attacker)) return;
			if (!(event.getEntity() instanceof Player primaryTarget)) return;

			BurrowerScythe scythe = getItem(attacker.getInventory().getItemInMainHand(), game);
			if (scythe == null) return;

			if (game.getGameTeamController().areAllies(attacker, primaryTarget)) {
				event.setCancelled(true);
				return;
			}

			tryLightningProc(primaryTarget);

			if (!slashingPlayers.add(attacker.getUniqueId())) return;

			try {
				List<Player> candidates = new ArrayList<>();
				for (org.bukkit.entity.Entity nearby : attacker.getNearbyEntities(SCYTHE_RANGE, SCYTHE_RANGE, SCYTHE_RANGE)) {
					if (!(nearby instanceof Player target)) continue;
					if (target.equals(attacker) || target.equals(primaryTarget)) continue;
					if (target.isDead()) continue;
					if (game.getGameTeamController().areAllies(attacker, target)) continue;

					Vector toTarget = target.getLocation().toVector().subtract(attacker.getLocation().toVector());
					double distanceSquared = toTarget.lengthSquared();
					if (distanceSquared > SCYTHE_RANGE_SQUARED || distanceSquared <= 0.0001) continue;

					Vector direction = attacker.getLocation().getDirection().normalize();
					double dot = direction.dot(toTarget.normalize());
					if (dot < SCYTHE_CONE_DOT) continue;

					candidates.add(target);
				}

				candidates.sort((a, b) -> {
					double da = a.getLocation().distanceSquared(attacker.getLocation());
					double db = b.getLocation().distanceSquared(attacker.getLocation());
					return Double.compare(da, db);
				});

				int extraHits = Math.max(0, SCYTHE_MAX_TARGETS - 1);
				for (int i = 0; i < Math.min(extraHits, candidates.size()); i++) {
					Player extraTarget = candidates.get(i);
					extraTarget.damage(event.getDamage(), attacker);
				}
			} finally {
				slashingPlayers.remove(attacker.getUniqueId());
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (!action.isRightClick()) return;

			Player player = event.getPlayer();
			BurrowerScythe scythe = getItem(player.getInventory().getItemInMainHand(), game);
			if (scythe == null) return;

			if (!scythe.refreshUseCooldown()) return;
			player.setCooldown(scythe.getStack().getType(), WALL_COOLDOWN_TICKS);

			UUID playerId = player.getUniqueId();
			BukkitTask previousTask = activeWallTasks.remove(playerId);
			if (previousTask != null && !previousTask.isCancelled()) {
				previousTask.cancel();
			}

			Map<Block, BlockSnapshot> previousSnapshots = activeWallSnapshots.remove(playerId);
			if (previousSnapshots != null && !previousSnapshots.isEmpty()) {
				restoreSnapshots(previousSnapshots);
			}

			Map<Block, BlockSnapshot> snapshots = scythe.castEarthWall();
			if (snapshots.isEmpty()) return;

			activeWallSnapshots.put(playerId, snapshots);
			BukkitTask task = game.getPlugin().getServer().getScheduler().runTaskLater(game.getPlugin(), () -> {
				Map<Block, BlockSnapshot> toRestore = activeWallSnapshots.remove(playerId);
				if (toRestore != null && !toRestore.isEmpty()) {
					restoreSnapshots(toRestore);
				}
				activeWallTasks.remove(playerId);
			}, WALL_DURATION_TICKS);
			activeWallTasks.put(playerId, task);
		}

		private void tryLightningProc(Player target) {
			if (ThreadLocalRandom.current().nextDouble() > LIGHTNING_PROC_CHANCE) return;

			target.getWorld().strikeLightningEffect(target.getLocation());
			double nextHealth = Math.max(0.0, target.getHealth() - LIGHTNING_TRUE_DAMAGE);
			target.setHealth(nextHealth);
		}


		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}
