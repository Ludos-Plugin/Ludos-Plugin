package fr.ludos.roles.huntsman;

import java.util.LinkedHashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.role.Role;
import fr.ludos.roles.huntsman.items.HuntsmanArrow;
import fr.ludos.roles.huntsman.items.HuntsmanBow;
import fr.ludos.roles.huntsman.items.HuntsmanCrossbow;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Implementation of the Huntsman {@link Role}.
 */
public class HuntsmanRole extends Role {
	public static final String ID = "huntsman";


	public HuntsmanRole(Builder builder, Game game) {
		super(builder, game);
	}


	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put(HuntsmanBow.ID, new HuntsmanBow.Events(game));
					put(HuntsmanCrossbow.ID, new HuntsmanCrossbow.Events(game));
					put(HuntsmanArrow.ID, new HuntsmanArrow.Events(game));
				}};
		}
	}


	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Entity hit = event.getHitEntity();
		if (hit == null) return;

		Projectile arrowProjectile = event.getEntity();
		if (! (arrowProjectile instanceof Arrow arrow)) return;

		ProjectileSource source = arrow.getShooter();
		if (! (source instanceof Player player)) return;

		if (! isPlayerValid(player)) return;

		player.addPotionEffect(PotionEffectType.SPEED.createEffect((int)(20 * 2.5), 2));


		playHitPing(player);
	}


	public @NotNull BukkitTask playHitPing(Player player) {
		return new BukkitRunnable() {
			private int progress;
			@Override
			public void run() {
				switch (progress) {
					case 0:
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.5f);
						break;
					case 2:
						player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.05f, 1.2f);
						cancel();
						return;
					default:
						break;
				}
				progress += 1;
			}
		}.runTaskTimer(getPlugin(), 0, 1);
	}

	@Override
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return getBuilder().getManager().isPlayerRole(player, ID);
	}

	/**
	 * Builder for the {@link HuntsmanRole}.
	 */
	public static class Builder extends Role.Builder {

		@Override
		public String getId() {
			return ID;
		}

		public Builder(Ludos ludos) {
			super(ludos.getRoleManager(), ludos);
		}


		@Override
		public HuntsmanRole build(Game game) {
			return new HuntsmanRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Huntsman")
				.color(NamedTextColor.DARK_PURPLE);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Most agile at range, the Huntsman's vision is keen.\n" +
				"Able to Shoot down his targets, wielding a Bow and special Crossbow."
			);
		}
	}
}