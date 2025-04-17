package fr.ludos.role;

import java.util.Map;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import fr.ludos.Ludos;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanCrossbow;
import fr.ludos.item.huntsman.HuntsmanArrow;
import fr.ludos.game.Game;


public class HuntsmanRole extends Role {
	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder, Game game) {
		super(builder, game);
	}


	@Override
	protected Map<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return Map.of(
					"bow", new HuntsmanBow.Events(game),
					"crossbow", new HuntsmanCrossbow.Events(game),
					"arrow", new HuntsmanArrow.Events(game)
				);
		}
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