package fr.ludos.item.trapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import fr.ludos.item.SpecialItem;
import fr.ludos.item.BranchItem;
import fr.ludos.game.Game;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;

public class TrapperSnareDevice extends BranchItem<TrapperSnareDeviceBranches> {

	public TrapperSnareDevice(ItemStack stack, Game game) throws IllegalArgumentException {
		super(stack, game);
	}

	public TrapperSnareDevice(Player owner, Game game) {
		this(owner, TrapperSnareDeviceBranches.REVEALING, game);
	}

	protected TrapperSnareDevice(Player owner, TrapperSnareDeviceBranches branch, Game game) {
		super(new ItemStack(Material.ENCHANTED_BOOK), owner, branch, game);
	}


	@Override
	public String getId(){
		return "trapperSnareGrimoire";
	}


	@Override
	protected Component getName() {
		return
			Component.text("Snare Grimoire ")
			.append(getBranchAnnotation())
			.decoration(TextDecoration.ITALIC, false);
	}



	public void throwObject(Player player, Material material) {
		Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(material));
		item.setVelocity(player.getLocation().getDirection().multiply(2));
	}

	@Nullable
	public static TrapperSnareDevice getItem(ItemStack stack, Game game) {
		try {
			TrapperSnareDevice snareDevice = new TrapperSnareDevice(stack, game);
			return snareDevice;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	public static TrapperSnareDevice createItem(Player owner, Game game) {
		return new TrapperSnareDevice(owner, game);
	}

	@Override
	public TrapperSnareDeviceBranches convertToBranch(int level) {
		return TrapperSnareDeviceBranches.findByKey(level);
	}
	@Override
	protected TrapperSnareDeviceBranches[] getBranches() {
		return TrapperSnareDeviceBranches.values;
	}



	public static class Events extends SpecialItem.Events<TrapperSnareDevice> {
		private BukkitTask trapTask = null;
		public final Map<Player, Map<TrapperSnareDeviceBranches, ArrayList<TrapperTrap>>> traps = new HashMap<>();

		public Events(Game game) {
			super(game);
		}

		@Override
		protected void onStart() {
			trapTask = new BukkitRunnable() {
				@Override
				public void run() {
					var game = Game.getCurrent();
					if (game == null) return;

					for (var playerTrapEntries : traps.entrySet()) {
						Player player = playerTrapEntries.getKey();
						Set<Player> targetedPlayers = game.getTeamController().getEnemies(playerTrapEntries.getKey());

						for (var branchTrapEntries : playerTrapEntries.getValue().entrySet()) {
							TrapperSnareDeviceBranches branch = branchTrapEntries.getKey();
							ArrayList<TrapperTrap> branchTraps = branchTrapEntries.getValue();
							if (branch.getLimit() > 0 && branchTraps.size() >= branch.getLimit()) {
								continue;
							}

							for (TrapperTrap trap : branchTraps) {
								for (Player targetedPlayer : targetedPlayers) {
									if (targetedPlayer.isDead()) continue;
									if (targetedPlayer.getGameMode() == GameMode.SPECTATOR || targetedPlayer.getGameMode() == GameMode.CREATIVE) continue;

									if (trap.canTriggerEffect(targetedPlayer)) {
										trap.triggerEffect(targetedPlayer);
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
		protected void onStop() {
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
			if (snareDevice == null) {
				return;
			}


			if (! snareDevice.refreshUseCooldown()) {
				return;
			}
			event.setCancelled(true);


			Action action = event.getAction();
			switch (action) {
				case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> snareDevice.cycleBranch();
				case RIGHT_CLICK_BLOCK -> {
					Block clickedBlock = event.getClickedBlock();
					if (clickedBlock == null) break;

					TrapperSnareDeviceBranches branch = snareDevice.getBranch();

					if (player.hasCooldown(branch.getType())) break;

					getTraps(player, branch).add(branch.createTrap(player, clickedBlock, event.getBlockFace()));
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
			return TrapperSnareDevice.getItem(stack, game);
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