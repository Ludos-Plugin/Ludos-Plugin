package fr.ludos.item.trapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
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
	protected String getName() {
		return "Snare Grimoire " + getBranchAnnotation();
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

		public final ArrayList<TrapperTrap> traps = new ArrayList<>();

		public Events(Game game) {
			super(game);

			new BukkitRunnable() {
				@Override
				public void run() {
					var game = Game.getCurrent();
					if (game == null) return;

					Set<TrapperTrap> trapsToRemove = new HashSet<>();
					for (TrapperTrap trap : traps) {
						Set<Player> targetedPlayers = game.getTeamController().getEnemies(trap.getOwner());
						for (Player targetedPlayer : targetedPlayers) {
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
		public void stop() {
			super.stop();

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


			if (! snareDevice.refreshUseCooldown()) {
				return;
			}
			event.setCancelled(true);


			Action action = event.getAction();
			switch (action) {
				case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> snareDevice.cycleBranch();
				case RIGHT_CLICK_BLOCK -> {
					Block clickedBlock = event.getClickedBlock();
					if (clickedBlock != null) {
						Block block = clickedBlock.getRelative(event.getBlockFace());
						traps.add(snareDevice.getBranch().createTrap(player, block));
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