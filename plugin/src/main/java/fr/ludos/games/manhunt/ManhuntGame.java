package fr.ludos.games.manhunt;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.core.Ludos;
import fr.ludos.core.area.WorldBorderArea;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.config.ConfigOptionsMap;
import fr.ludos.core.config.valueOptions.MultipleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.NumberConfigOptions;
import fr.ludos.core.config.valueOptions.SingleGroupPlayerConfigOptions;
import fr.ludos.core.config.valueOptions.ValueConfigOptions;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.Lobby;
import fr.ludos.core.lobby.Lobby.ClearMode;
import fr.ludos.core.world.WorldManager;
import fr.ludos.games.manhunt.items.ManhuntCompass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;


/**
 * Implementation of the Manhunt {@link Game}.
 */
public class ManhuntGame extends Game {
	public static final String ID = "manhunt";

	private final Builder builder;
	public final Builder getBuilder() {
		return this.builder;
	}

	private final WorldManager worldManager;
	@Override
	public WorldManager getWorldManager() {
		return this.worldManager;
	}

	private final ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private final ManhuntCompass.Events compassEvents;
	private final ManhuntTimer timer;

	private Location lastPreyLocation = null;
	private BukkitTask actionBarTask;

	private BukkitTask saturationTask;


	protected ManhuntGame(Builder builder, Group group) {
		super(builder, group, Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		this.builder = builder;

		Location returnLocation;
		Player leader = group.getLeader().getPlayer();
		if (leader == null || ! leader.isOnline()) {
			Optional<Player> onlinePlayer = group.getOnlinePlayers().stream()
				.filter(p -> p.isOnline())
				.findFirst();
			returnLocation = onlinePlayer.isPresent()
				? onlinePlayer.get().getLocation()
				: Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
		} else {
			returnLocation = leader.getLocation();
		}

		this.worldManager = WorldManager.within(this, returnLocation)
			.of(builder.createWorldCreator())
			.withLobby(Lobby.within(this)
				.clear(ClearMode.ALL)
				.waitFor(group)
				.wait(Duration.ofSeconds(GroupConfigMap.START_DELAY.getGroupConfig(group)))
				.then(this::start)
			)
			.inArea(
				WorldBorderArea.within(this, WorldBorderArea.CONFIG.getGameConfig(group, builder))
			)
			.build();
		this.teamController = new ManhuntTeamController(
			this,
			builder.players.getGameConfig(group, builder),
			builder.prey.getGameConfig(group, builder)
		);

		timer = new ManhuntTimer(this, builder.revealPeriod.getGameConfig(group, builder));
		compassEvents = new ManhuntCompass.Events(this);
	}

	@Override
	protected void onGameStart() {
		super.onGameStart();

		World world = worldManager.getWorld();
		world.setTime(1000);

		compassEvents.start();
		timer.start();

		actionBarTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (lastPreyLocation == null) return;

				for (Player player : getGroup().getOnlinePlayers()) {
					player.sendActionBar(
						Component.text("Prey's location:")
						.append(Component.text(" X:" + lastPreyLocation.getBlockX()).color(NamedTextColor.RED))
						.append(Component.text(" Y:" + lastPreyLocation.getBlockY()).color(NamedTextColor.GREEN))
						.append(Component.text(" Z:" + lastPreyLocation.getBlockZ()).color(NamedTextColor.BLUE))
					);
				}
			}
		}.runTaskTimer(getPlugin(), 0, 1);

		saturationTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : getGroup().getOnlinePlayers()) {
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
			}
		}.runTaskTimer(getPlugin(), 400, 400);


		Bukkit.getServer().broadcast(Component.text("The Game of Manhunt started"));
	}

	@Override
	protected void onGameStop() {
		super.onGameStop();

		compassEvents.stop();
		timer.stop();

		for (Player player : getGroup().getOnlinePlayers()) {
			player.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
		}

		if (actionBarTask != null) {
			actionBarTask.cancel();
			actionBarTask = null;
		}

		if (saturationTask != null) {
			saturationTask.cancel();
			saturationTask = null;
		}

		Bukkit.getServer().broadcast(Component.text("The Game of Manhunt ended"));
	}

	@Override
	public void scheduleEndGame(int seconds, String text) {
		super.scheduleEndGame(seconds, text);
		if (timer != null) timer.stop();
	}


	public void revealPrey() {
		Set<Player> preys = teamController.getTeamOnlinePlayers(teamController.preyTeam);
		if (preys.isEmpty()) return;

		Player prey = preys.stream().findAny().get();

		lastPreyLocation = prey.getLocation();

		Bukkit.getServer().broadcast(
			Component.text("The Prey was revealed!\n")
			.append(Component.text("They are located at"))
			.append(Component.text(" X:" + lastPreyLocation.getBlockX()).color(NamedTextColor.RED))
			.append(Component.text(" Y:" + lastPreyLocation.getBlockY()).color(NamedTextColor.GREEN))
			.append(Component.text(" Z:" + lastPreyLocation.getBlockZ()).color(NamedTextColor.BLUE))
		);

		for (Player hunter : teamController.getTeamOnlinePlayers(teamController.preyTeam)) {
			for (ManhuntCompass compass : ManhuntCompass.findAllIn(hunter.getInventory(), (ItemStack stack) -> ManhuntCompass.fromItemStack(stack, this))) {
				compass.setLocation(prey);
			}
		}

		prey.addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (! getGroup().isPlayer(player)) return;
		if (isStarted()) {
			player.setScoreboard(getScoreboard());
		}
	}

	/**
	 * Builder for {@link ManhuntGame}.
	 */
	public static class Builder extends Game.Builder {
		public final ValueConfigOptions<Set<OfflinePlayer>> players =
			new MultipleGroupPlayerConfigOptions(getManager().getLudos().getGroupManager(), "Players", "players", "all");

		public final ValueConfigOptions<OfflinePlayer> prey =
			new SingleGroupPlayerConfigOptions(getManager().getLudos().getGroupManager(), "Prey Player", "prey", "random");

		public final ValueConfigOptions<Integer> revealPeriod =
			new NumberConfigOptions("Reveal period duration seconds", "reveal", null, 180, Set.of(60, 120, 180, 240, 300, 360), true);

		public final ConfigOptionsMap config =
			new ConfigOptionsMap(ID, Set.of(players, prey, WorldBorderArea.CONFIG, revealPeriod));

		public Builder(GameManager manager) {
			super(manager);
		}

		@Override
		public String getId() {
			return ID;
		}

		@Override
		public TextComponent getDisplayName() {
			return Component.text("Manhunt")
				.color(NamedTextColor.RED);
		}
		@Override
		public TextComponent getDescription() {
			return Component.text("Get Hunted down by your friends, or hunt one of them down with the others.\n\n" +
				"As the ").append(getPreyText()).append(Component.text(", survive as long as possible, while the ").append(getHunterText(true)).append(Component.text(" try to find you.\n\n" +
				"The ").append(getHunterText(true)).append(Component.text(" possess a Compass that will update regularly to point at the ").append(getPreyText()).append(Component.text("'s position."
			)))));
		}

		public static Component getHunterText() {
			return getHunterText(false);
		}
		public static Component getHunterText(boolean plural) {
			return Component.text(plural ? "Hunters" : "Hunter").color(NamedTextColor.RED);
		}
		public static Component getPreyText() {
			return Component.text("Prey").color(NamedTextColor.BLUE);
		}


		public @Nullable WorldCreator createWorldCreator() {
			String worldName = "manhunt_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(Ludos.NAMESPACE, worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			wc.keepSpawnLoaded(TriState.FALSE);
			return wc;
		}

		@Override
		public ConfigOptionsCollection getConfig() {
			return config;
		}

		@Override
		public ManhuntGame build(Group group) {
			return new ManhuntGame(this, group);
		}
	}
}