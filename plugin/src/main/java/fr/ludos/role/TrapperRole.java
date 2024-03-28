package fr.ludos.role;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Main;
import fr.ludos.role.Role;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.potion.Potion;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;



// import org.bukkit.Bukkit;
// import org.bukkit.plugin.PluginManager;

public class TrapperRole extends Role {

	public static final String id = "trapper";
	private BukkitTask task;

	public static List<String> weapons = new ArrayList<>(
		List.of("WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD")

	);

	public TrapperRole(Builder builder) {
		super(builder);

		task = new BukkitRunnable() {

            @Override
            public void run() {
				for (Player player : Role.getPlayersOfRole(id)) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 0, true, false));
				}
            }

        }.runTaskTimer(Main.getInstance(), 0, 20);
	}

	@Override
	public void stop() {
		super.stop();

		task.cancel();
	}


	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		// Entity entity = event.getEntity();
		// if (! (entity instanceof Player)) {
		// 	return;
		// }
		// Player player = (Player) entity;


		// ItemStack currentItem = player.getInventory().getItemInMainHand();
		// if (event.getDamager() instanceof Player && weapons.contains(currentItem.getType().toString())){
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

		if (event.isSneaking()) {
			event.getPlayer().addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(Integer.MAX_VALUE, 1));
		} else {
			event.getPlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}


	public static class Builder extends Role.Builder {
		@Override
		public String getId() {
			return id;
		}

		@Override
		public Role build(String gameId) {
			return new TrapperRole(this);
		}
	}
}

