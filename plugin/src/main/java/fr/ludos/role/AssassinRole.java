package fr.ludos.role;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Ludos;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.assassin.AssassinDagger;
import fr.ludos.item.assassin.AssassinBoots;
import fr.ludos.item.assassin.TeleportScroll;
import fr.ludos.item.trapper.TrapperSnareDevice;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;


public class AssassinRole extends Role {
	public static final String id = "assassin";

	private static final int INVISIBILITY_DURATION = 300;
	private static final long STATIONARY_DURATION_MS = 3000;

	private final Map<UUID, Long> lastMoveTime = new HashMap<>();
	private BukkitTask stealthTask;


	public AssassinRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onRoleStart() {
		stealthTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
			long now = System.currentTimeMillis();
			for (Player player : Role.getPlayersOfRole(AssassinRole.id)) {
				if (!player.isOnline()) continue;
				long last = lastMoveTime.getOrDefault(player.getUniqueId(), now);
				if (now - last >= STATIONARY_DURATION_MS && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION, 0, false, false));
				}
			}
		}, 0L, 20L);
	}

	@Override
	protected void onRoleStop() {
		if (stealthTask != null) {
			stealthTask.cancel();
			stealthTask = null;
		}
		lastMoveTime.clear();
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		switch (builder.getId()) {
			default:
				return new LinkedHashMap<>() {{
					put("dagger", new AssassinDagger.Events(game));
					put("boots", new AssassinBoots.Events(game));
					put("teleport_scroll", new TeleportScroll.Events(game));
					put("snare", new TrapperSnareDevice.Events(game) {
						@Override
						protected Boolean canPlayerHaveItem(HumanEntity owner) {
							return Role.isPlayerRole(owner, AssassinRole.id);
						}
					});
				}};
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!Role.isPlayerRole(player, AssassinRole.id)) return;

		// Ignorer les rotations (regarder autour sans bouger)
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
			event.getFrom().getBlockY() == event.getTo().getBlockY() &&
			event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

		lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());

		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}

	@EventHandler
	public void onInvisibleDaggerHit(EntityDamageByEntityEvent event) {
		if (! (event.getDamager() instanceof Player player)) return;
		if (! Role.isPlayerRole(player, AssassinRole.id)) return;

		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

		event.setDamage(event.getDamage() * 2.5);
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
		public AssassinRole build(Game game) {
			return new AssassinRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Assassin");
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Se camoufle pour surprendre ses ennemis et les éliminer");
		}
	}
}
