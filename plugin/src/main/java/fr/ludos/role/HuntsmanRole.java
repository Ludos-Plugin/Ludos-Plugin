package fr.ludos.role;

import java.util.Map;

import org.bukkit.Material;
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

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanCrossbow;


public class HuntsmanRole extends Role {
	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onStart() {
		for (Player huntsman : Role.getPlayersOfRole(id)) {
			updateArrowCount(huntsman);
		}
	}

	@Override
	protected Map<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return Map.of(
					"bow", new HuntsmanBow.Events(game),
					"crossbow", new HuntsmanCrossbow.Events(game)
				);
		}
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

		public Builder(Ludos plugin) {
			super(plugin);
		}


		@Override
		public HuntsmanRole build(Game game) {
			return new HuntsmanRole(this, game);
		}
	}
}