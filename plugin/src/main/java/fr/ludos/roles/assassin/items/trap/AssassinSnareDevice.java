package fr.ludos.roles.assassin.items.trap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.util.Vector;

import fr.ludos.core.game.Game;
import fr.ludos.core.item.BranchItem;
import fr.ludos.core.item.BranchItemInterface;
import fr.ludos.core.item.ItemSlot;
import fr.ludos.core.item.SpecialItemInterface;
import fr.ludos.core.role.Role;
import fr.ludos.roles.assassin.AssassinRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class AssassinSnareDevice extends BranchItem<AssassinSnare> {
	private final static String ID = "trapperSnareGrimoire";

	// private final static Map<UUID, TrapperSnareDevice> cachedItems = new HashMap<>();


	public static AssassinSnareDevice fromItemStack(Map<String, AssassinSnare> branchMap, @Nullable AssassinSnare defaultBranch, ItemStack stack, Game game) throws IllegalArgumentException {
		final UUID itemId = SpecialItemInterface.getSpecialItemId(stack, ID, game);
		if (itemId == null) return null;

		// TrapperSnareDevice cached = cachedItems.get(itemId);
		// if (cached != null) return cached;

		final Player owner = SpecialItemInterface.getSpecialItemOwner(stack, game);
		if (owner == null) return null;
		final String branchKey = BranchItemInterface.branchFromItemStack(stack, game);
		if (branchKey == null) return null;

		AssassinSnare branch = branchMap.getOrDefault(branchKey, defaultBranch);

		AssassinSnareDevice device = new AssassinSnareDevice(branchMap, branch, stack, owner, game);
		// cachedItems.put(itemId, device);

		return device;
	}
	public static AssassinSnareDevice createItem(Map<String, AssassinSnare> branchMap, @Nullable AssassinSnare defaultBranch, Player owner, Game game) {
		final AssassinSnareDevice device = new AssassinSnareDevice(branchMap, defaultBranch, new ItemStack(Material.ENCHANTED_BOOK), owner, game);
		final UUID itemId = device.initializeItem();

		// cachedItems.put(itemId, device);

		return device;
	}

	protected AssassinSnareDevice(Map<String, AssassinSnare> branchMap, @Nullable AssassinSnare defaultBranch, ItemStack stack, Player owner, Game game) {
		super(branchMap, defaultBranch, stack, owner, game);
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


	public static class Events extends BranchItem.Events<AssassinSnareDevice, AssassinSnare> {
		private BukkitTask trapTask = null;
		public final Map<Player, Map<AssassinSnare, ArrayList<AssassinTrap>>> traps = new HashMap<>();

		public Events(Game game) {
			super(Arrays.asList(AssassinSnareDeviceBranches.values()), game, new Events.Info(ItemSlot.HOTBAR_2));
		}

		@Override
		protected void onItemStart() {
			super.onItemStart();

			trapTask = new BukkitRunnable() {
				@Override
				public void run() {
					var game = getGame();
					if (game == null) return;

					for (var playerTrapEntries : traps.entrySet()) {
						Player player = playerTrapEntries.getKey();

						for (var branchTrapEntries : playerTrapEntries.getValue().entrySet()) {
							AssassinSnare branch = branchTrapEntries.getKey();
							@SuppressWarnings("unchecked")
							ArrayList<AssassinTrap> branchTraps = ((ArrayList<AssassinTrap>) branchTrapEntries.getValue().clone());
							if (branch.getLimit() > 0 && branchTraps.size() >= branch.getLimit()) {
								continue;
							}

							for (AssassinTrap trap : branchTraps) {
								Vector range = trap.getRange();
								Collection<LivingEntity> targets = trap.getLocation().getNearbyLivingEntities(range.getX(), range.getY(), range.getZ());
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
			super.onItemStop();

			traps.clear();

			if (trapTask != null) {
				trapTask.cancel();
				trapTask = null;
			}
		}

		private ArrayList<AssassinTrap> getTraps(Player player, AssassinSnare branch) {
			return traps.computeIfAbsent(player, k -> new HashMap<>())
				.computeIfAbsent(branch, k -> new ArrayList<>());
		}
		private void addTrap(Player player, AssassinSnare branch, AssassinTrap trap) {
			getTraps(player, branch).add(trap);
		}
		private void removeTrap(Player player, AssassinSnare branch, AssassinTrap trap) {
			ArrayList<AssassinTrap> playerTraps = getTraps(player, branch);
			if (playerTraps != null) {
				playerTraps.remove(trap);
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			if (! isPlayerValid(player)) return;

			AssassinSnareDevice snareDevice = getItem(player.getInventory().getItemInMainHand());
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

					AssassinSnare branch = snareDevice.getBranch();

					getTraps(player, branch).add(branch.createTrap(player, clickedBlock, face));
				}
				default -> player.sendMessage("You must click on a block to place a trap."); // TODO: Translate
			}
		}

		@EventHandler
		public void onItemDrop(PlayerDropItemEvent event) {
			if (! isPlayerValid(event.getPlayer())) return;

			if (event.getItemDrop().getItemStack().getType() == Material.ARROW) {
				event.setCancelled(true);
			}
		}


		@Override
		@Nullable
		public AssassinSnareDevice getItem(ItemStack stack) {
			return AssassinSnareDevice.fromItemStack(getBranches(), getDefaultBranch(), stack, game);
		}

		@Override
		public AssassinSnareDevice createItem(Player owner) {
			return AssassinSnareDevice.createItem(getBranches(), getDefaultBranch(), owner, game);
		}
		@Override
		protected Boolean isPlayerValidInternal(OfflinePlayer owner) {
			return Role.isPlayerRole(owner, AssassinRole.id);
		}
	}
}