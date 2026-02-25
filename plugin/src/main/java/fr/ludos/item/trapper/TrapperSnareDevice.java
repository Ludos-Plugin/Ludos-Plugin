package fr.ludos.item.trapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.burrower.BurrowerPick;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TrapperSnareDevice extends BranchItem<TrapperSnareDeviceBranches> {
	private final static String ID = "trapperSnareGrimoire";

	// private final static Map<UUID, TrapperSnareDevice> cachedItems = new HashMap<>();


	public static TrapperSnareDevice fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		UUID itemId = SpecialItem.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperSnareDevice cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		Player owner = SpecialItem.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		Integer branchIndex = BranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;

		TrapperSnareDevice device = new TrapperSnareDevice(stack, owner, TrapperSnareDeviceBranches.values()[branchIndex], game);
		// cachedItems.put(itemId, device);

		return device;
	}
	public static TrapperSnareDevice createItem(Player owner, Game game) {
		TrapperSnareDevice device = new TrapperSnareDevice(new ItemStack(Material.ENCHANTED_BOOK), owner, TrapperSnareDeviceBranches.REVEALING, game);
		UUID itemId = device.initializeItem();

		// cachedItems.put(itemId, device);

		return device;
	}

	protected TrapperSnareDevice(ItemStack stack, Player owner, TrapperSnareDeviceBranches branch, Game game) {
		super(TrapperSnareDeviceBranches.class, stack, owner, branch, game);
	}


	@Override
	public String getTypeId() {
		return ID;
	}

	@Override
	public Component getName() {
		return
			Component.text("Snare Grimoire ")
			.append(getBranchAnnotation())
			.decoration(TextDecoration.ITALIC, false);
	}

	public void throwObject(Player player, Material material) {
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(material));
		item.setVelocity(player.getLocation().getDirection().multiply(2));
	}


	public static class Events extends BranchItem.Events<TrapperSnareDevice, TrapperSnareDeviceBranches> {
		private BukkitTask trapTask = null;
		public final Map<Player, Map<TrapperSnareDeviceBranches, ArrayList<TrapperTrap>>> traps = new HashMap<>();

		public Events(Game game) {
			super(game, 1);
		}

		@Override
		protected void onItemStart() {
			trapTask = new BukkitRunnable() {
				@Override
				public void run() {
					var game = Game.getCurrent();
					if (game == null) return;

					for (var playerTrapEntries : traps.entrySet()) {
						Player player = playerTrapEntries.getKey();
						Set<LivingEntity> targets = game.getGameTeamController().getEnemies(player);

						for (var branchTrapEntries : playerTrapEntries.getValue().entrySet()) {
							TrapperSnareDeviceBranches branch = branchTrapEntries.getKey();
							ArrayList<TrapperTrap> branchTraps = branchTrapEntries.getValue();
							if (branch.getLimit() > 0 && branchTraps.size() >= branch.getLimit()) {
								continue;
							}

							for (TrapperTrap trap : branchTraps) {
								for (LivingEntity target : targets) {
									if (target.isDead()) continue;
									if (target instanceof Player playerTarget && (playerTarget.getGameMode() == GameMode.SPECTATOR || playerTarget.getGameMode() == GameMode.CREATIVE)) continue;

									if (trap.canTriggerEffect(target)) {
										trap.triggerEffect(target);
										removeTrap(player, branch, trap);
									}
								}
							}
						}
					}
				}
			}.runTaskTimer(game.getPlugin(), 0, 1);
		}

		@Override
		protected void onItemStop() {
			traps.clear();

			if (trapTask != null) {
				trapTask.cancel();
				trapTask = null;
			}
		}

		private ArrayList<TrapperTrap> getTraps(Player player, TrapperSnareDeviceBranches branch) {
			return traps.computeIfAbsent(player, k -> new HashMap<>())
				.computeIfAbsent(branch, k -> new ArrayList<>());
		}
		private void addTrap(Player player, TrapperSnareDeviceBranches branch, TrapperTrap trap) {
			getTraps(player, branch).add(trap);
		}
		private void removeTrap(Player player, TrapperSnareDeviceBranches branch, TrapperTrap trap) {
			ArrayList<TrapperTrap> playerTraps = getTraps(player, branch);
			if (playerTraps != null) {
				playerTraps.remove(trap);
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			TrapperSnareDevice snareDevice = getItem(player.getInventory().getItemInMainHand(), game);
			if (snareDevice == null) return;


			if (! snareDevice.refreshUseCooldown()) return;
			event.setCancelled(true);


			Action action = event.getAction();
			switch (action) {
				case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
					snareDevice.cycleBranch();
					snareDevice.refreshUseCooldown();
				}
				case RIGHT_CLICK_BLOCK -> {
					Block clickedBlock = event.getClickedBlock();
					if (clickedBlock == null) break;

					BlockFace face = event.getBlockFace();
					if (face == BlockFace.SELF) break;

					if (clickedBlock.getRelative(face).getType() != Material.AIR) {
						player.sendMessage("You must click on an empty block to place a trap."); // TODO: Translate
						break;
					}

					TrapperSnareDeviceBranches branch = snareDevice.getBranch();

					getTraps(player, branch).add(branch.createTrap(player, clickedBlock, face));
				}
				default -> player.sendMessage("You must click on a block to place a trap."); // TODO: Translate
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
		@Nullable
		protected TrapperSnareDevice getItem(ItemStack stack, Game game) {
			return TrapperSnareDevice.fromItemStack(stack, game);
		}

		@Override
		protected TrapperSnareDevice createItem(Player owner, Game game) {
			return TrapperSnareDevice.createItem(owner, game);
		}
		@Override
		protected Boolean canPlayerHaveItem(HumanEntity owner) {
			return Role.isPlayerRole(owner, TrapperRole.id);
		}
	}
}