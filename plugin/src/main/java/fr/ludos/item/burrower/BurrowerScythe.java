package fr.ludos.item.burrower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.ItemUtilities;
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
	private static final int WALL_DISTANCE_DEFAULT = 2;
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


	public void castEarthWall(@Nullable Block anchor) {
		Player owner = getOwner();
		if (owner == null || owner.getWorld() == null) return;

		Block baseBlock;
		if (anchor == null) {
			Vector forward = owner.getFacing().getDirection().multiply(WALL_DISTANCE_DEFAULT);
			Block potentialAnchor = owner.getLocation().getBlock().getRelative(forward.getBlockX(), -1, forward.getBlockZ());
			while (potentialAnchor.getType().isAir()) {
				potentialAnchor = potentialAnchor.getRelative(BlockFace.DOWN);
			}

			baseBlock = potentialAnchor;
		}
		else {
			baseBlock = anchor;
		}
		World world = baseBlock.getWorld();

		int halfWidth = WALL_WIDTH / 2;
		for (List<Block> immutableColumn : Utility.getAllBlockColumns(
			baseBlock, owner.getFacing(),
			Pair.of(-halfWidth, halfWidth),
			Pair.of(-(WALL_HEIGHT - 1), 0),
			Pair.of(0, 0)
		)) {
			List<Block> column = new ArrayList<>(immutableColumn);
			Collections.reverse(column);

			new BukkitRunnable() {
				int currentHeight = 0;
				int maxHeight = WALL_HEIGHT;

				@Override
				public void run() {
					for (int i = 0; i < column.size(); i++) {
						Block block = column.get(i);

						Block aboveBlock = block.getRelative(BlockFace.UP);
						if (aboveBlock.getType().isCollidable()) {
							cancel();
							return;
						}

						BlockData aboveBlockData = aboveBlock.getBlockData().clone();
						BlockData blockData = block.getBlockData().clone();
						aboveBlock.setBlockData(blockData, false);
						block.setBlockData(aboveBlockData, false);

						world.playEffect(aboveBlock.getLocation(), org.bukkit.Effect.STEP_SOUND, aboveBlock.getType());

						column.set(i, aboveBlock);
					}

					currentHeight++;
					if (currentHeight >= maxHeight) {
						cancel();
					}
				}
			}.runTaskTimer(getGame().getPlugin(), 0, 3);
		}
	}

	public static class Events extends LevelItem.Events<BurrowerScythe, BurrowerScytheLevels> {
		private final Set<UUID> slashingPlayers = new HashSet<>();

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
			if (!(event.getEntity() instanceof LivingEntity primaryTarget)) return;

			BurrowerScythe scythe = getItem(attacker.getInventory().getItemInMainHand(), game);
			if (scythe == null) return;

			if (game.getGameTeamController().areAllies(attacker, primaryTarget)) return;

			if (event.getCause() == DamageCause.ENTITY_ATTACK) {
				ItemUtilities.doSweepAttack(attacker, primaryTarget, event.getDamage());
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (!action.isRightClick()) return;

			Player player = event.getPlayer();
			BurrowerScythe scythe = getItem(player.getInventory().getItemInMainHand(), game);
			if (scythe == null) return;

			if (player.hasCooldown(scythe.getStack().getType())) return;
			player.setCooldown(scythe.getStack().getType(), WALL_COOLDOWN_TICKS);

			event.setCancelled(true);

			scythe.castEarthWall(event.getClickedBlock());
		}


		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, BurrowerRole.id);
		}
	}
}
