package fr.ludos.game.manhunt;

import java.util.Optional;
import java.util.Iterator;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.TriState;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Ludos;
import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaController;
import fr.ludos.game.areaController.worldborder.WorldBorderAreaOption;
import fr.ludos.game.areaController.worldborder.WorldBorderLocationOption;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import fr.ludos.game.lobbyController.structure.StructureLobbyController;
import fr.ludos.game.teamController.GameJoinOption;
import fr.ludos.game.worldController.SingleWorldController;
import fr.ludos.group.Group;


public class ManhuntGame extends Game {
	public static final String ID = "manhunt";

	private final Scoreboard scoreboard;
	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private final SingleWorldController worldController;
	@Override
	public SingleWorldController getWorldController() {
		return this.worldController;
	}

	private final WorldBorderAreaController areaController;
	@Override
	public WorldBorderAreaController getAreaController() {
		return this.areaController;
	}

	private final ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private final StructureLobbyController lobbyController;
	@Override
	public StructureLobbyController getLobbyController() {
		return this.lobbyController;
	}

	private final ManhuntCompass.Events compassEvents;
	private final ManhuntTimer timer;

	public Builder getManhuntBuilder() {
		return (Builder) this.getBuilder();
	}

	private Location lastPreyLocation = null;
	private BukkitTask actionBarTask;

	private BukkitTask saturationTask;


	protected ManhuntGame(Builder builder, Group group) {
		super(builder, group);

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

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		this.worldController = new SingleWorldController(
			this,
			builder.createWorldCreator(),
			returnLocation
		);
		this.areaController = new WorldBorderAreaController(
			this,
			ManhuntGameConfigs.getLocation(config),
			ManhuntGameConfigs.getArea(config)
		);
		this.teamController = new ManhuntTeamController(
			this,
			ManhuntGameConfigs.getChosenPlayers(config),
			ManhuntGameConfigs.getChosenPrey(config)
		);
		this.lobbyController = new StructureLobbyController(
			this,
			ManhuntGameConfigs.getWaitPlayersOption(config),
			ManhuntGameConfigs.getWaitDurationOption(config),
			(location) -> {
				Location structureLocation = location.clone();
				structureLocation.setY(275);

				structureLocation.setX(structureLocation.getBlockX());
				structureLocation.setZ(structureLocation.getBlockZ());

				// Build enclosed platform
				for (int x = -5; x <= 5; x++) {
					for (int z = -5; z <= 5; z++) {
						Location blockLoc = structureLocation.clone().add(x, 0, z);
						blockLoc.getBlock().setType(Material.OBSIDIAN);
					}
				}

				// Add walls
				for (int x = -5; x <= 5; x++) {
					for (int y = 1; y <= 3; y++) {
						structureLocation.clone().add(x, y, -5).getBlock().setType(Material.OBSIDIAN);
						structureLocation.clone().add(x, y, 5).getBlock().setType(Material.OBSIDIAN);
					}
				}

				for (int z = -4; z <= 4; z++) {
					for (int y = 1; y <= 3; y++) {
						structureLocation.clone().add(-5, y, z).getBlock().setType(Material.OBSIDIAN);
						structureLocation.clone().add(5, y, z).getBlock().setType(Material.OBSIDIAN);
					}
				}

				return structureLocation;
			}
		);

		timer = new ManhuntTimer(this, ManhuntGameConfigs.getReveal(config));
		compassEvents = new ManhuntCompass.Events(this);
	}

	@Override
	protected void onGameStart() {
		World world = worldController.getWorld();
		world.setTime(1000);
		Collection<OfflinePlayer> players = teamController.getPlayers();

		for (OfflinePlayer offlinePlayer : players) {
			Player player = offlinePlayer.getPlayer();
			if (player == null) continue;

			player.getInventory().clear();
			player.setScoreboard(scoreboard);
			Utility.revokeAllAdvancements(player);
		}

		compassEvents.start();
		timer.start();

		actionBarTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (lastPreyLocation == null) return;

				for (OfflinePlayer offlinePlayer : players) {
					Player player = offlinePlayer.getPlayer();
					if (player == null) continue;

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
				for (OfflinePlayer offlinePlayer : players) {
					Player player = offlinePlayer.getPlayer();
					if (player == null) continue;
					player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
			}
		}.runTaskTimer(getPlugin(), 400, 400);


		Bukkit.getServer().broadcast(Component.text("The Game of Manhunt started"));
	}

	@Override
	protected void onGameStop() {
		compassEvents.stop();
		timer.stop();

		areaController.resetBorder();

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
		Player prey = teamController.getOnlinePrey();
		if (prey == null) return;

		lastPreyLocation = prey.getLocation();

		Bukkit.getServer().broadcast(
			Component.text("The Prey was revealed!\n")
			.append(Component.text("They are located at"))
			.append(Component.text(" X:" + lastPreyLocation.getBlockX()).color(NamedTextColor.RED))
			.append(Component.text(" Y:" + lastPreyLocation.getBlockY()).color(NamedTextColor.GREEN))
			.append(Component.text(" Z:" + lastPreyLocation.getBlockZ()).color(NamedTextColor.BLUE))
		);

		for (Player hunter : teamController.getOnlineHunters()) {
			for (ManhuntCompass compass : ManhuntCompass.findAllIn(hunter.getInventory(), (ItemStack stack) -> ManhuntCompass.fromItemStack(stack, this))) {
				compass.setLocation(prey);
			}
		}

		prey.addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Set<Player> hunters = teamController.getOnlineHunters();
		Player prey = teamController.getOnlinePrey();

		if (hunters.isEmpty() || prey == null) {
			timer.resume();
			return;
		}

		var player = event.getPlayer();

		if (!hunters.contains(player) && player != prey) {
			player.setGameMode(GameMode.SPECTATOR);

			player.teleport(prey.getLocation());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		Set<Player> hunters = teamController.getOnlineHunters();
		if (hunters.contains(player)) {
			hunters.remove(player);
		}

		Player prey = teamController.getOnlinePrey();

		if (hunters.isEmpty() || prey == null) {
			timer.pause();
		}
	}

	@Override
	public Boolean canPlayerHaveRole(Player player, String roleId) {
		// if (teamController.preyTeam.getEntries().contains(player.getName())) {
		// 	return false;
		// }

		return true;
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

		public String getGameConfigUsage(CommandSender sender, Command command, String label) {
			StringBuilder usage = new StringBuilder("/" + label + " game " + getId() + " config <config> [value]");

			for (ManhuntGameConfigs config : ManhuntGameConfigs.values()) {
				usage.append("\n  ").append(config.name()).append(" ")
					.append(config.getUsage());
			}

			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args) {
			if (args.length == 0) {
				return false;
			}

			String arg = args[0];
			ManhuntGameConfigs option = Arrays.stream(ManhuntGameConfigs.values()).filter(o -> o.name().equals(arg)).findFirst().orElse(null);
			if (option == null) return false;

			return option.handleConfigsCommand(sender, command, label, config, Arrays.copyOfRange(args, 1, args.length));
		}


		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(ManhuntGameConfigs.values())
					.map(ManhuntGameConfigs::name)
					.collect(Collectors.toList());
			}

			String arg = args[0];
			if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, arg) ) {
				return null;
			}
			ManhuntGameConfigs option = ManhuntGameConfigs.valueOf( arg );

			return option.handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
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