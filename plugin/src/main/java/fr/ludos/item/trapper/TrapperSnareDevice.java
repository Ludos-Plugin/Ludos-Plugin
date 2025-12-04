package fr.ludos.item.trapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TrapperSnareDevice extends BranchItem<TrapperSnareDeviceBranches> {
	private final static String ID = "trapperSnareGrimoire";


	public static TrapperSnareDevice fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;
		Integer branchIndex = BranchItem.branchFromItemStack(stack, game);
		if (branchIndex == null) return null;

		return new TrapperSnareDevice(stack, owner, TrapperSnareDeviceBranches.findByKey(branchIndex), game);
	}
	public static TrapperSnareDevice createItem(Player owner, Game game) {
		TrapperSnareDevice device = new TrapperSnareDevice(new ItemStack(Material.ENCHANTED_BOOK), owner, TrapperSnareDeviceBranches.REVEALING, game);
		device.initializeItem();

		return device;
	}

	protected TrapperSnareDevice(ItemStack stack, Player owner, TrapperSnareDeviceBranches branch, Game game) {
		super(stack, owner, branch, game);
	}


	@Override
	public String getId() {
		return ID;
	}

	@Override
	public Component getName() {
		return
			Component.text("Snare Grimoire ")
			.append(getBranchAnnotation())
			.decoration(TextDecoration.ITALIC, false);
	}


	@Override
	public TrapperSnareDeviceBranches convertToBranch(int level) {
		return TrapperSnareDeviceBranches.findByKey(level);
	}
	@Override
	protected TrapperSnareDeviceBranches[] getBranches() {
		return TrapperSnareDeviceBranches.values;
	}


	public static class Events extends BranchItem.Events<TrapperSnareDevice, TrapperSnareDeviceBranches> {

		public final ArrayList<TrapperTrap> traps = new ArrayList<>();

		public Events(Game game) {
			super(game, 1);

			new BukkitRunnable() {
				@Override
				public void run() {
					var game = Game.getCurrent();
					if (game == null) return;

					Set<TrapperTrap> trapsToRemove = new HashSet<>();
					for (TrapperTrap trap : traps) {
						Set<Player> targetedPlayers = game.getTeamController().getEnemies(trap.getOwner());
						for (Player targetedPlayer : targetedPlayers) {
							if (targetedPlayer.isDead()) continue;
							if (targetedPlayer.getGameMode() == GameMode.SPECTATOR || targetedPlayer.getGameMode() == GameMode.CREATIVE) continue;

							if (trap.getType().executeEffect(targetedPlayer, trap)) {
								trapsToRemove.add(trap);
							}
						}
					}
					traps.removeAll(trapsToRemove);
				}
			}.runTaskTimer(game.getPlugin(), 0, 1);
		}

		@Override
		protected void onStop() {
			traps.clear();
		}


		public void loop(int radius, TrapperTrap innerTrap, Material material) {
			for (int x = -radius; x <= radius; x++) {
				for (int y = -radius; y <= radius; y++) {
					for (int z = -radius; z <= radius; z++) {
						if (innerTrap.getLocation().getBlock().getRelative(x, y, z).getType().isAir()) {
							innerTrap.getLocation().getBlock().getRelative(x, y, z).setType(material);
						}
					}
				}
			}
		}

		@EventHandler
		public void onPlayerInteract(PlayerInteractEvent event) {
			Player player = event.getPlayer();
			TrapperSnareDevice snareDevice = getItem(player.getInventory().getItemInMainHand(), game);
			if (snareDevice == null) {
				return;
			}


			if (! snareDevice.refreshUseCooldown()) return;
			event.setCancelled(true);


			Action action = event.getAction();
			switch (action) {
				case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> snareDevice.cycleBranch();
				case RIGHT_CLICK_BLOCK -> {
					Block clickedBlock = event.getClickedBlock();
					if (clickedBlock != null) {
						traps.add(snareDevice.getBranch().createTrap(player, clickedBlock, event.getBlockFace()));
					} else {
						player.sendMessage("You must click on a block to place a trap."); // TODO: Translate
					}
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

		@Override
		protected TrapperSnareDeviceBranches[] getBranches() {
			return TrapperSnareDeviceBranches.values;
		}
	}
}