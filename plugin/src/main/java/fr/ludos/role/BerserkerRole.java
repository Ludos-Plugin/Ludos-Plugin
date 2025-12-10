package fr.ludos.role;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Ludos;
import fr.ludos.game.Game;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.berserker.BerserkerAxe;
import fr.ludos.item.berserker.BerserkerRageBrew;


public class BerserkerRole extends Role {
	public static final String id = "berserker";

	private static final Set<UUID> ragingPlayers = new HashSet<>();
	private BukkitTask particleTask;

	public BerserkerRole(Builder builder, Game game) {
		super(builder, game);
	}

	public static boolean isRaging(Player player) {
		return ragingPlayers.contains(player.getUniqueId());
	}

	public static void setRage(Player player, boolean active) {
		if (active) {
			ragingPlayers.add(player.getUniqueId());
		} else {
			ragingPlayers.remove(player.getUniqueId());
		}
	}

	public static void clearRage() {
		ragingPlayers.clear();
	}

	@Override
	protected LinkedHashMap<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
		return new LinkedHashMap<>() {{
			put("axe", new BerserkerAxe.Events(game));
			put("rage_brew", new BerserkerRageBrew.Events(game));
		}};
	}

	@Override
	protected void onStart() {
		particleTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Role.getPlayersOfRole(id)) {
					if (player == null || !player.isOnline()) continue;
					spawnRageParticles(player);
				}
			}
		}.runTaskTimer(getGame().getPlugin(), 0, 10);
	}

	@Override
	protected void onStop() {
		if (particleTask != null) {
			particleTask.cancel();
			particleTask = null;
		}
		clearRage();
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player player)) return;
		if (!Role.isPlayerRole(player, id)) return;
		if (!BerserkerAxe.isHoldingBerserkerAxe(player, getGame())) return;

		double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if (maxHealth <= 0) return;

		double ratio = player.getHealth() / maxHealth;
		double multiplier = 1.0;

		if (isRaging(player) || ratio < 0.2) {
			multiplier = 1.35;
		} else if (ratio < 0.4) {
			multiplier = 1.20;
		} else if (ratio < 0.7) {
			multiplier = 1.10;
		}

		double finalDamage = event.getDamage() * multiplier;
		event.setDamage(finalDamage);

		double healAmount = finalDamage * 0.10;
		player.setHealth(Math.min(maxHealth, player.getHealth() + healAmount));
	}

	private void spawnRageParticles(Player player) {
		double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if (maxHealth <= 0) return;

		double ratio = player.getHealth() / maxHealth;
		Color color = null;

		if (isRaging(player) || ratio < 0.2) {
			color = Color.fromRGB(15, 15, 15);
		} else if (ratio < 0.4) {
			color = Color.fromRGB(90, 0, 0);
		} else if (ratio < 0.7) {
			color = Color.fromRGB(180, 0, 0);
		}

		if (color == null) return;

		player.getWorld().spawnParticle(
			Particle.REDSTONE,
			player.getLocation().add(0, 1, 0),
			6,
			0.4,
			0.8,
			0.4,
			new Particle.DustOptions(color, 1.25f)
		);
	}


	public static class Builder extends Role.Builder {

		public Builder(Ludos plugin) {
			super(plugin);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Berserker")
				.color(NamedTextColor.DARK_RED);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Dual axes, rage scaling damage, lifesteal and a rage brew.")
				.color(NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false);
		}

		@Override
		public Role build(Game game) {
			return new BerserkerRole(this, game);
		}
	}
}
