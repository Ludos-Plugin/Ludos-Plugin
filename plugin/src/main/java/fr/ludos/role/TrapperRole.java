package fr.ludos.role;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.common.value.qual.ArrayLen;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import fr.ludos.Main;
import fr.ludos.game.Game;
import fr.ludos.item.trapper.TrapperSnareDevice;


public class TrapperRole extends Role {
	public static final String id = "trapper";

	private static final ArrayList<Player> sneakingPlayers = new ArrayList<>();
	private final TrapperSnareDevice.Events snareEvents;

	public TrapperRole(Builder builder) {
		super(builder);

		snareEvents = new TrapperSnareDevice.Events();

		new BukkitRunnable() {
            @Override
            public void run() {
				for (Player sneakingPlayer : sneakingPlayers) {
					sneakingPlayer.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(4, 0));
				}
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);
	}

	@Override
	public void stop() {
		super.stop();

		snareEvents.stop();
	}


	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		// Entity entity = event.getEntity();
		// if (! (entity instanceof Player)) {
		// 	return;
		// }
		// Player player = (Player) entity;


		// ItemStack currentItem = player.getInventory().getItemInMainHand();
		// if (event.getDamager() instanceof Player && weapons.contains(currentItem.getType())){
		// 	player.addPotionEffect(PotionEffectType.POISON.createEffect(4, 1));

		// 	ItemStack chestplate = player.getInventory().getChestplate();
		// 	chestplate.setDurability((short) (chestplate.getDurability() - 10));
		// 	player.getInventory().setChestplate(chestplate);

		// 	ItemStack leggings = player.getInventory().getLeggings();
		// 	leggings.setDurability((short) (leggings.getDurability() - 10));
		// 	player.getInventory().setLeggings(leggings);

		// 	ItemStack boots = player.getInventory().getBoots();
		// 	boots.setDurability((short) (boots.getDurability() - 10));
		// 	player.getInventory().setBoots(boots);

		// 	ItemStack helmet = player.getInventory().getHelmet();
		// 	helmet.setDurability((short) (helmet.getDurability() - 10));
		// 	player.getInventory().setHelmet(helmet);

		// }
	}

	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		if (! Role.isPlayerRole(event.getPlayer(), id)) {
			return;
		}

		if (event.isSneaking() && (event.getPlayer().getVelocity().getY() <= 0)) {
			// event.getPlayer().addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(1, 1));
			sneakingPlayers.add(event.getPlayer());
		} else {
			// event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
			sneakingPlayers.remove(event.getPlayer());
		}
	}


	public static class Builder extends Role.Builder {
		@Override
		public String getId() {
			return id;
		}

		@Override
		public Role build(Game.Builder builder) {
			return new TrapperRole(this);
		}
	}
}

