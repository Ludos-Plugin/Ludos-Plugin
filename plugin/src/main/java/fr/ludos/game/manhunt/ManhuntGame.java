package fr.ludos.game.manhunt;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Ludos;
import fr.ludos.command.ConfigSubcommandManager;
import fr.ludos.command.ludos.GroupConfigs;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaController;
import fr.ludos.game.lobbyController.structure.StructureLobbyController;
import fr.ludos.game.worldController.SingleWorldController;
import fr.ludos.group.Group;
import fr.ludos.structure.LobbyStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;


public class ManhuntGame extends Game {
	public static final String ID = "manhunt";

	private final Builder builder;
	public final Builder getBuilder() {
		return this.builder;
	}

	private final SingleWorldController worldController;
	@Override
	public SingleWorldController getWorldController() {
		return this.worldController;
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
		super(builder, group);
		this.builder = builder;

		ConfigurationSection config = group.getConfig();

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

		this.worldController = new SingleWorldController(
			this,
			new StructureLobbyController(
				this,
				false,
				GroupConfigs.getWaitPlayersOption(config),
				GroupConfigs.getWaitDurationOption(config).getDuration(),
				new LobbyStructure.Builder(),
				this::start
			),
			new WorldBorderAreaController(
				this,
				ManhuntGameConfigs.getArea(config)
			),
			returnLocation
		);
		this.teamController = new ManhuntTeamController(
			this,
			ManhuntGameConfigs.getChosenPlayers(config),
			ManhuntGameConfigs.getChosenPrey(config)
		);

		timer = new ManhuntTimer(this, ManhuntGameConfigs.getReveal(config));
		compassEvents = new ManhuntCompass.Events(this);
	}

	@Override
	protected void onGameSetup() {
		super.onGameSetup();

		getWorldController().transferToNewWorld(builder.createWorldCreator());
	}

	@Override
	protected void onGameStart() {
		super.onGameStart();

		World world = worldController.getWorld();
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


	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		// if (teamController.preyTeam.getEntries().contains(player.getName())) {
		// 	return false;
		// }

		return true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (! getGroup().isPlayer(player)) return;
		if (isStarted()) {
			player.setScoreboard(getScoreboard());
		}
	}


	public static class Builder extends Game.Builder {

		public Builder(Ludos plugin) {
			super(plugin);
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
			return Component.text("A game of hide and seek.\n" +
				"As the Prey, survive for as long as possible, while the Hunters try to find you.\n" +
				"The Hunters possess a Compass that will update regularly to point at the Prey's position."
			);
		}

		private final ConfigSubcommandManager<ManhuntGameConfigs> configsSubcommand = new ConfigSubcommandManager<>(ManhuntGameConfigs.values());
		@Override
		protected ConfigSubcommandManager<?> getConfigsSubcommand() {
			return configsSubcommand;
		}


		public @Nullable WorldCreator createWorldCreator() {
			String worldName = "manhunt_" + UUID.randomUUID();
			WorldCreator wc = new WorldCreator(worldName, new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
			wc.keepSpawnLoaded(TriState.FALSE);
			return wc;
		}

		@Override
		public ManhuntGame build(Group group) {
			return new ManhuntGame(this, group);
		}
	}
}