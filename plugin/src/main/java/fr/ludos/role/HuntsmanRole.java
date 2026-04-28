package fr.ludos.role;

import java.util.LinkedHashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.huntsman.HuntsmanArrow;
import fr.ludos.item.huntsman.HuntsmanBow;
import fr.ludos.item.huntsman.HuntsmanCrossbow;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;


public class HuntsmanRole extends Role {
	public static final String id = "huntsman";


	public HuntsmanRole(Builder builder, Game game) {
		super(builder, game);
	}


	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put("bow", new HuntsmanBow.Events(game));
					put("crossbow", new HuntsmanCrossbow.Events(game));
					put("arrow", new HuntsmanArrow.Events(game));
				}};
		}
	}


	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getHitEntity() == null) return;

		Projectile arrowProjectile = event.getEntity();
		if (! (arrowProjectile instanceof Arrow arrow)) return;

		ProjectileSource source = arrow.getShooter();
		if (! (source instanceof Player player)) return;

		if (! isPlayerValid(player)) return;

		player.addPotionEffect(PotionEffectType.SPEED.createEffect((int)(20 * 2.5), 2));
	}

	@Override
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return Role.isPlayerRole(player, id);
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

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Huntsman");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("");
		}
	}
}