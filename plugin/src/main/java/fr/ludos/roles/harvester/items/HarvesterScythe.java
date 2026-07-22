package fr.ludos.roles.harvester.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.ItemUtilities;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.item.level.LevelItem;
import fr.ludos.roles.harvester.HarvesterRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Implementation of the Huntsman Scythe, for use by any Player with {@link HarvesterRole}.
 */
public class HarvesterScythe extends LevelItem<HarvesterScythe, HarvesterScytheLevels> {
	public static final String ID = "harvester_scythe";

	private static final int WALL_COOLDOWN_TICKS = 20 * 8;
	private static final int WALL_DISTANCE_DEFAULT = 2;
	private static final int WALL_WIDTH = 5;
	private static final int WALL_HEIGHT = 3;

	HarvesterScythe(LevelItem.ItemData<HarvesterScytheLevels> info, Events events) {
		super(info, events);
	}

	@Override
	public Component getName() {
		return Component.text("Harvester's Scythe")
			.decoration(TextDecoration.ITALIC, false); // TODO: Translate
	}

	@Override
	public List<Component> getLore() {
		List<Component> lore = super.getLore();

		lore.add(Component.text("Ability: Build a wall of earth")
			.decoration(TextDecoration.ITALIC, false)
			.color(NamedTextColor.GRAY));
		lore.add(SpecialItemInterface.getActionAnnotation("key.use", Component.text("Build Wall")));
		return lore;
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

						for (Entity entity : aboveBlock.getWorld().getNearbyEntities(
							aboveBlock.getLocation().add(0.5, 0.5, 0.5), 0.5, 1.0, 0.5)) {
							if (entity.getLocation().getBlockX() == block.getX()
								&& entity.getLocation().getBlockZ() == block.getZ()
								&& entity.getLocation().getBlockY() == block.getY() + 1) {
								entity.teleport(entity.getLocation().add(0.0, 1.0, 0.0));
							}
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

	/**
	 * Events for the {@link HarvesterScythe}.
	 */
	public static class Events extends LevelItem.Events<HarvesterScythe, HarvesterScytheLevels> {
		private static final List<HarvesterScytheLevels> LEVELS = List.of(HarvesterScytheLevels.values());

		public Events(Game game) {
			super(game, new Events.Info(ItemSlot.HOTBAR_1));
		}

		@Override
		public String getTypeId() {
			return ID;
		}

		@Override
		public List<HarvesterScytheLevels> getLevels() {
			return LEVELS;
		}

		@Override
		protected HarvesterScythe getItemInternal(ItemData<HarvesterScytheLevels> info) {
			return new HarvesterScythe(info, this);
		}

		@Override
		protected HarvesterScythe createItemInternal(LevelData<HarvesterScytheLevels> data, Player owner) {
			HarvesterScytheLevels currentLevel = data.getCurrentLevelOr(HarvesterScytheLevels.WOODEN);
			return new HarvesterScythe(new ItemData<>(data, new SpecialItem.ItemData(new ItemStack(currentLevel.getMaterial()) , owner)), this);
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
			if (!(event.getDamager() instanceof Player attacker)) return;
			if (!(event.getEntity() instanceof LivingEntity primaryTarget)) return;

			HarvesterScythe scythe = getItem(attacker.getInventory().getItemInMainHand());
			if (scythe == null) return;

			if (game.getTeamController().areEntitiesAllies(attacker, primaryTarget)) return;

			if (event.getCause() == DamageCause.ENTITY_ATTACK) {
				int enchantmentLevel = scythe.getStack().getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
				ItemUtilities.doSweepAttack(attacker, primaryTarget, event.getDamage(), enchantmentLevel, 2.25);
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Action action = event.getAction();
			if (!action.isRightClick()) return;

			Player player = event.getPlayer();
			HarvesterScythe scythe = getItem(player.getInventory().getItemInMainHand());
			if (scythe == null) return;

			if (player.hasCooldown(scythe.getStack().getType())) return;
			player.setCooldown(scythe.getStack().getType(), WALL_COOLDOWN_TICKS);

			event.setCancelled(true);

			scythe.castEarthWall(event.getClickedBlock());
		}


		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return game.getLudos().getRoleManager().isPlayerRole(owner, HarvesterRole.ID);
		}
	}
}
