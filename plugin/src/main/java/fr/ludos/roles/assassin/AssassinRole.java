package fr.ludos.roles.assassin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.core.Ludos;
import fr.ludos.core.Utility;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.valueEntry.IntegerConfigEntry;
import fr.ludos.core.role.Role;
import fr.ludos.roles.assassin.items.AssassinBoots;
import fr.ludos.roles.assassin.items.AssassinDagger;
import fr.ludos.roles.assassin.items.SmokeBomb;
import fr.ludos.roles.assassin.items.TeleportScroll;
import fr.ludos.roles.assassin.items.trap.AssassinSnareDevice;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * Implementation of the Assassin {@link Role}.
 */
public class AssassinRole extends Role {
	public static final String ID = "assassin";

	private static final int INVISIBILITY_DURATION = 300;
	private static final int STEALTH_TASK_PERIOD = 5;
	private static final long CAMOUFLAGE_DISPLAY_THRESHOLD_MS = 500;
	private static final ItemStack EMPTY_EQUIPMENT = new ItemStack(Material.AIR);

	private final Map<UUID, Long> lastMoveTime = new HashMap<>();
	private final Map<UUID, Long> forcedInvisibilityEnd = new HashMap<>();
	private final Map<UUID, BossBar> stealthBars = new HashMap<>();
	private final Set<UUID> invisiblePlayers = new HashSet<>();
	private BukkitTask stealthTask;
	private BukkitTask visibilityTask;


	public AssassinRole(Builder builder, Game game) {
		super(builder, game);
	}

	@Override
	public Builder getBuilder() {
		return (Builder) super.getBuilder();
	}

	/**
	 * Fetches the value of one of the {@link Builder}'s config entries, for the Group of this Role's Game.
	 * @param entry The config entry to fetch, from {@link #getBuilder()}
	 * @return The value configured for the Group, or the global / default value
	 */
	public int config(IntegerConfigEntry entry) {
		return entry.getRoleConfig(game().group(), getBuilder());
	}

	private List<Player> getOnlineAssassins() {
		return game().group().getOnlinePlayers().stream()
			.filter((player) -> getBuilder().getManager().isPlayerRole(player, ID))
			.collect(Collectors.toUnmodifiableList());
	}

	@Override
	protected void onRoleStart() {
		super.onRoleStart();

		stealthTask = Bukkit.getScheduler().runTaskTimer(plugin(), () -> {
			long now = System.currentTimeMillis();
			long stealthDelay = config(getBuilder().stealthDelay) * 1000L;

			for (Player player : getOnlineAssassins()) {
				if (! Utility.IS_ENTITY_ALIVE.test(player)) {
					// La réapparition ne compte pas comme un mouvement : le camouflage repart de zéro
					lastMoveTime.put(player.getUniqueId(), now);
					hideStealthBar(player);
					continue;
				}

				long stationary = now - lastMoveTime.computeIfAbsent(player.getUniqueId(), k -> now);
				if (stationary >= stealthDelay) {
					if (! player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
						player.setArrowsStuck(0);
					}
					// Rafraîchie tant que l'Assassin reste immobile : elle ne s'écoule qu'en se déplaçant accroupi
					player.addPotionEffect(createInvisibility(INVISIBILITY_DURATION));
				}

				updateStealthBar(player, stationary, stealthDelay);
			}
		}, 0L, STEALTH_TASK_PERIOD);

		// L'invisibilité ne masque pas l'équipement : on le cache aux ennemis à chaque tick,
		// car le serveur renvoie le vrai équipement dès qu'il change
		visibilityTask = Bukkit.getScheduler().runTaskTimer(plugin(), () -> {
			for (Player player : getOnlineAssassins()) {
				if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					if (invisiblePlayers.add(player.getUniqueId())) {
						player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.6f, 1.4f);
					}
					hideEquipment(player);
				} else if (invisiblePlayers.remove(player.getUniqueId())) {
					if (! player.isDead()) {
						player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.4f, 0.8f);
					}
					showEquipment(player);
				}
			}
		}, 0L, 1L);
	}

	@Override
	protected void onRoleStop() {
		super.onRoleStop();

		if (stealthTask != null) {
			stealthTask.cancel();
			stealthTask = null;
		}
		if (visibilityTask != null) {
			visibilityTask.cancel();
			visibilityTask = null;
		}
		for (UUID uuid : invisiblePlayers) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) continue;

			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			showEquipment(player);
		}
		for (var entry : stealthBars.entrySet()) {
			Player player = Bukkit.getPlayer(entry.getKey());
			if (player != null) player.hideBossBar(entry.getValue());
		}
		invisiblePlayers.clear();
		stealthBars.clear();
		forcedInvisibilityEnd.clear();
		lastMoveTime.clear();
	}

	/**
	 * Makes the Assassin invisible right away, and keeps them invisible while moving for the given duration.
	 * @param player The Assassin to hide
	 * @param durationTicks How long moving does not break the invisibility
	 */
	public void grantInvisibility(Player player, int durationTicks) {
		forcedInvisibilityEnd.put(player.getUniqueId(), System.currentTimeMillis() + durationTicks * 50L);
		player.addPotionEffect(createInvisibility(Math.max(durationTicks, INVISIBILITY_DURATION)));
		player.setArrowsStuck(0);
	}

	private boolean hasForcedInvisibility(Player player) {
		Long end = forcedInvisibilityEnd.get(player.getUniqueId());
		return end != null && System.currentTimeMillis() < end;
	}

	private static PotionEffect createInvisibility(int durationTicks) {
		return new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false);
	}

	private void updateStealthBar(Player player, long stationary, long stealthDelay) {
		BossBar bar = stealthBars.computeIfAbsent(player.getUniqueId(), k ->
			BossBar.bossBar(Component.empty(), 0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
		);

		PotionEffect invisibility = player.getPotionEffect(PotionEffectType.INVISIBILITY);
		if (invisibility != null) {
			bar.name(Component.text("Invisible").color(NamedTextColor.AQUA))
				.color(BossBar.Color.BLUE)
				.progress(Math.min(1f, (float) invisibility.getDuration() / INVISIBILITY_DURATION));
		} else if (stationary >= CAMOUFLAGE_DISPLAY_THRESHOLD_MS) {
			bar.name(Component.text("Camouflage…").color(NamedTextColor.GRAY))
				.color(BossBar.Color.WHITE)
				.progress(stealthDelay > 0 ? Math.min(1f, (float) stationary / stealthDelay) : 1f);
		} else {
			player.hideBossBar(bar);
			return;
		}
		player.showBossBar(bar);
	}

	private void hideStealthBar(Player player) {
		BossBar bar = stealthBars.get(player.getUniqueId());
		if (bar != null) player.hideBossBar(bar);
	}

	private void hideEquipment(Player player) {
		GameTeamController teamController = game().teamController();
		for (Player viewer : player.getWorld().getPlayers()) {
			if (viewer.equals(player)) continue;
			if (teamController != null && teamController.arePlayersAllies(viewer, player)) continue;

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				viewer.sendEquipmentChange(player, slot, EMPTY_EQUIPMENT);
			}
		}
	}

	private void showEquipment(Player player) {
		EntityEquipment equipment = player.getEquipment();
		for (Player viewer : player.getWorld().getPlayers()) {
			if (viewer.equals(player)) continue;

			for (EquipmentSlot slot : EquipmentSlot.values()) {
				viewer.sendEquipmentChange(player, slot, equipment.getItem(slot));
			}
		}
	}

	@Override
	protected LinkedHashMap<String, GameEvents> createGameEvents(Role.Builder builder, Game game) {
		LinkedHashMap<String, GameEvents> map = new LinkedHashMap<>();
		map.put(AssassinDagger.ID, new AssassinDagger.Events(game, this));
		map.put(AssassinBoots.ID, new AssassinBoots.Events(game));
		map.put(TeleportScroll.ID, new TeleportScroll.Events(game, this));
		map.put(AssassinSnareDevice.ID, new AssassinSnareDevice.Events(game, this));
		map.put(SmokeBomb.ID, new SmokeBomb.Events(game, this));
		return map;
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

		if (player.isSneaking() || hasForcedInvisibility(player)) return;

		if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInvisibleDaggerHit(EntityDamageByEntityEvent event) {
		if (! (event.getDamager() instanceof Player player)) return;
		if (! getBuilder().getManager().isPlayerRole(player, AssassinRole.ID)) return;

		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

		event.setDamage(event.getDamage() * config(getBuilder().ambushDamage) / 100.0);

		// Frapper révèle l'Assassin : il doit de nouveau rester immobile pour redevenir invisible
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		forcedInvisibilityEnd.remove(player.getUniqueId());
		lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());
	}

	@Override
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return getBuilder().getManager().isPlayerRole(player, ID);
	}

	/**
	 * Builder for the {@link AssassinRole}.
	 */
	public static class Builder extends Role.Builder {
		public final IntegerConfigEntry stealthDelay = entry("Stealth delay (seconds)", "stealth_delay", 3, Material.CLOCK);
		public final IntegerConfigEntry ambushDamage = entry("Invisible hit damage (%)", "ambush_damage", 250, Material.GLASS);
		public final IntegerConfigEntry daggerDamage = entry("Dagger damage (%)", "dagger_damage", 150, Material.IRON_SWORD);
		public final IntegerConfigEntry trapLimit = entry("Traps per type (0 = unlimited)", "trap_limit", 3, Material.TRIPWIRE_HOOK);
		public final IntegerConfigEntry teleportCooldown = entry("Teleport Scroll cooldown (seconds)", "teleport_cooldown", 30, Material.PAPER);
		public final IntegerConfigEntry smokeCooldown = entry("Smoke Bomb cooldown (seconds)", "smoke_cooldown", 30, Material.FIREWORK_STAR);
		public final IntegerConfigEntry smokeRadius = entry("Smoke Bomb radius (blocks)", "smoke_radius", 5, Material.CAMPFIRE);
		public final IntegerConfigEntry smokeDuration = entry("Smoke Bomb duration (seconds)", "smoke_duration", 4, Material.INK_SAC);

		public final ConfigNodeMap configMap = getConfigMap(List.of(
			stealthDelay,
			ambushDamage,
			daggerDamage,
			trapLimit,
			teleportCooldown,
			smokeCooldown,
			smokeRadius,
			smokeDuration
		));

		private static IntegerConfigEntry entry(String name, String key, int defaultValue, Material material) {
			return new IntegerConfigEntry(Component.text(name), key, defaultValue, true) {
				@Override
				public AbstractItemBuilder<?> createItem(Player player) {
					return new ItemBuilder(material);
				}
			};
		}

		@Override
		public String getId() {
			return ID;
		}

		public Builder(Ludos ludos) {
			super(ludos.roleManager(), ludos);
		}


		@Override
		public AssassinRole build(Game game) {
			return new AssassinRole(this, game);
		}

		@Override
		public ConfigNodeCollection getConfig() {
			return configMap;
		}

		@Override
		public AbstractItemBuilder<?> createItem(Player player) {
			return new ItemBuilder(Material.STONE_SWORD);
		}

		@Override
		public TextComponent displayName() {
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
