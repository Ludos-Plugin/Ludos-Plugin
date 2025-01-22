package fr.ludos.role;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.trapper.TrapperSnareDevice;


public class TrapperRole extends Role {
	public static final String id = "trapper";

	private static final ArrayList<Player> sneakingPlayers = new ArrayList<>();

	private BukkitTask invisibilityTask;


	public TrapperRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onStart() {
		invisibilityTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player sneakingPlayer : sneakingPlayers) {
					sneakingPlayer.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(4, 0));
				}
			}
		}.runTaskTimer(Ludos.getInstance(), 0, 1);
	}

	@Override
	protected void onStop() {
		sneakingPlayers.clear();

		invisibilityTask.cancel();
		invisibilityTask = null;
	}

	@Override
	protected Map<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
		return Map.of(
			"snare", new TrapperSnareDevice.Events(game)
		);
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
		public Role build(Game.Builder builder, Game game) {
			return new TrapperRole(this, game);
		}
	}
}

