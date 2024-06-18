package fr.ludos.item.trapper;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.Main;
import fr.ludos.game.Game;
import fr.ludos.item.BranchItem;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;

public class TrapperSnareDevice extends BranchItem<TrapperSnareDeviceBranches> {

	public TrapperSnareDevice(ItemStack stack) throws IllegalArgumentException {
		super(stack);
	}

	public TrapperSnareDevice(Player owner) {
		this(owner, TrapperSnareDeviceBranches.REVEALING);
	}

	protected TrapperSnareDevice(Player owner, TrapperSnareDeviceBranches branch) {
		super(new ItemStack(Material.ENCHANTED_BOOK), owner, branch);
	}



    @Override
    public String getId(){
        return "trapper_snare";
    }


    @Override
    protected String getName() {
        return "Snare Grimoire";
    }



    public void throwObject(Player player, Material material) {
        Item item = player.getWorld().dropItem(player.getEyeLocation(), new ItemStack(material));
        item.setVelocity(player.getLocation().getDirection().multiply(2));
    }

    public void trapGlowing(String name, int duration) {
        final Player targetPlayer = Bukkit.getPlayer(name);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            return;
        }

        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration * 20, 1));
    }

	@Nullable
	public static TrapperSnareDevice getItem(ItemStack stack) {
		try {
			TrapperSnareDevice snareDevice = new TrapperSnareDevice(stack);
			return snareDevice;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public static TrapperSnareDevice createItem(Player owner) {
		return new TrapperSnareDevice(owner);
	}

	public static class Events extends SpecialItem.Events<TrapperSnareDevice> {

		public Events() {
			super(TrapperRole.id);

			updateAllInventories();

			new BukkitRunnable() {
				@Override
				public void run() {
					var game = Game.getCurrent();
					if (game == null) {
						return;
					}

					for (TrapperTrap trap : TrapperTrap.traps) {
						Set<Player> targetedPlayers = game.getTeamController().getEnemies(trap.getOwner());
						for (Player targetedPlayer : targetedPlayers) {

							if (targetedPlayer.getLocation().distance(trap.getLocation()) < trap.getRadius() ) {
								trap.process(targetedPlayer);
								TrapperTrap.traps.remove(trap);
							}

						}
					}
				}
			}.runTaskTimer(Main.getInstance(), 0, 1);
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
            TrapperSnareDevice snareDevice = getItem(player.getInventory().getItemInMainHand());
            if (snareDevice == null) {
                return;
            }

            if (player.hasCooldown(snareDevice.getStack().getType())) {
                return;
            }

            Action action = event.getAction();
			if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
				snareDevice.cycleBranch();
			}
			else if (action == Action.RIGHT_CLICK_BLOCK) {
				Block block = event.getClickedBlock().getRelative(event.getBlockFace());
				snareDevice.getBranch().createTrap(player, block);
			}
			else {
				player.sendMessage("You must click on a block to place a trap."); // TODO: Translate
			}

			player.setCooldown(snareDevice.getStack().getType(), 5);
		}

		@EventHandler
		public void onItemDrop(PlayerDropItemEvent event) {
			if (! Role.isPlayerRole(event.getPlayer(), roleId)) {
				return;
			}

			if (event.getItemDrop().getItemStack().getType() == Material.ARROW) {
				event.setCancelled(true);
			}
		}


		@Override
		@Nullable
		protected TrapperSnareDevice getItem(ItemStack stack) {
			return TrapperSnareDevice.getItem(stack);
		}

		@Override
		protected TrapperSnareDevice createItem(Player owner) {
			return TrapperSnareDevice.createItem(owner);
		}

	}

	@Override
	public TrapperSnareDeviceBranches convertToBranch(int level) {
		return TrapperSnareDeviceBranches.findByKey(level);
	}

	@Override
	protected TrapperSnareDeviceBranches[] getBranches() {
		return TrapperSnareDeviceBranches.values;
	}
}