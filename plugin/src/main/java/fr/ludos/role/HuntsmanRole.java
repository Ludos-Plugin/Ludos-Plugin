package fr.ludos.role;

import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

// import fr.ludos.item.huntsman.crossbow.HuntsmanStBow;
// import fr.ludos.item.huntsman.spear.HuntsmanSpear;
import fr.ludos.game.Game;
import fr.ludos.item.huntsman.HuntsmanBow;
// import fr.ludos.command.SetBowLevelCommand;
import fr.ludos.item.huntsman.HuntsmanCrossbow;
// import fr.ludos.item.huntsman.HuntsmanLevelSelector;


public class HuntsmanRole extends Role {
	public static final String id = "huntsman";

	private final HuntsmanBow.Events bowEvents;
	private final HuntsmanCrossbow.Events crossbowEvents;


	public HuntsmanRole(Builder builder) {
		super(builder);

		bowEvents = new HuntsmanBow.Events();
		crossbowEvents = new HuntsmanCrossbow.Events();

		for (Player huntsman : Role.getPlayersOfRole(id)) {
			updateArrowCount(huntsman);
		}
	}

	@Override
	public void stop() {
		super.stop();
		bowEvents.stop();
		crossbowEvents.stop();
	}


	@EventHandler
	public void onShootArrow(EntityShootBowEvent event) {
		if (! (event.getEntity() instanceof HumanEntity player)) return;
		if (! Role.isPlayerRole(player, id)) return;

		updateArrowCount(player);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getHitEntity() == null) return;

		Projectile arrowProjectile = event.getEntity();
		if (! (arrowProjectile instanceof Arrow arrow)) return;

		ProjectileSource source = arrow.getShooter();
		if (! (source instanceof HumanEntity player)) return;

		if (! Role.isPlayerRole(player, id)) return;

		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int)(20 * 2.5), 2));
	}

	private void updateArrowCount(HumanEntity player) {
		player.getInventory().remove(Material.ARROW);
		player.getInventory().addItem(new ItemStack(Material.ARROW));
	}


	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return id;
		}

		@Override
		public HuntsmanRole build(Game.Builder builder) {
			return new HuntsmanRole(this);
		}
	}
}