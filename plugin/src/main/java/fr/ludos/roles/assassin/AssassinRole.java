package fr.ludos.roles.assassin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.core.Ludos;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.role.Role;
import fr.ludos.roles.assassin.items.AssassinBoots;
import fr.ludos.roles.assassin.items.AssassinDagger;
import fr.ludos.roles.assassin.items.TeleportScroll;
import fr.ludos.roles.assassin.items.trap.AssassinSnareDevice;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Implementation of the Assassin {@link Role}.
 */
public class AssassinRole extends Role {
	public static final String ID = "assassin";

	private static final int INVISIBILITY_DURATION = 300;
	private static final long STATIONARY_DURATION_MS = 3000;

	private final Map<UUID, Long> lastMoveTime = new HashMap<>();
	private BukkitTask stealthTask;


	public AssassinRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	protected void onRoleStart() {
		super.onRoleStart();

		stealthTask = Bukkit.getScheduler().runTaskTimer(getPlugin(), () -> {
			long now = System.currentTimeMillis();
			List<Player> players = getGame().getGroup().getOnlinePlayers().stream()
				.filter((player) -> getBuilder().getManager().isPlayerRole(player, ID))
				.collect(Collectors.toUnmodifiableList());
			for (Player player : players) {
				if (!player.isOnline()) continue;
				long last = lastMoveTime.getOrDefault(player.getUniqueId(), now);
				if (now - last >= STATIONARY_DURATION_MS && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION, 0, false, false));
					player.setArrowsStuck(0);
				}
			}
		}, 0L, 20L);
	}

	@Override
	protected void onRoleStop() {
		super.onRoleStop();

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
					put(AssassinDagger.ID, new AssassinDagger.Events(game));
					put(AssassinBoots.ID, new AssassinBoots.Events(game));
					put(TeleportScroll.ID, new TeleportScroll.Events(game));
					put(AssassinSnareDevice.ID, new AssassinSnareDevice.Events(game));
				}};
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!getBuilder().getManager().isPlayerRole(player, AssassinRole.ID)) return;

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
		if (! getBuilder().getManager().isPlayerRole(player, AssassinRole.ID)) return;

		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

		event.setDamage(event.getDamage() * 2.5);
	}

	@Override
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return getBuilder().getManager().isPlayerRole(player, ID);
	}

	/**
	 * Builder for the {@link AssassinRole}.
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
		public AssassinRole build(Game game) {
			return new AssassinRole(this, game);
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Assassin")
				.color(NamedTextColor.DARK_BLUE);
		}

		@Override
		public TextComponent getDescription() {
			return Component.text("Hidden and concealed, the Assassin prowls, stalking for its next Target.\n" +
				"He uses trickery and traps to weaken his enemies."
			);
		}
	}
}
