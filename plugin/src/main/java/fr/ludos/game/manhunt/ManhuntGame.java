package fr.ludos.game.manhunt;

import java.util.Optional;
import java.util.Iterator;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import fr.ludos.game.GameJoinOption;
import fr.ludos.game.worldborder.WorldBorderAreaController;
import fr.ludos.game.worldborder.WorldBorderAreaOption;
import fr.ludos.game.worldborder.WorldBorderLocationOption;
import fr.ludos.group.Group;


public class ManhuntGame extends Game {
	public static final String ID = "manhunt";

	public static final String playersKey = "players";
	public static final String playersPath = ID + '.' + playersKey;
	public static final String preyKey = "prey";
	public static final String preyPath = ID + '.' + preyKey;

	public static final String areaKey = "area";
	public static final String areaPath = ID + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = ID + '.' + locationKey;
	public static final String revealKey = "reveal";
	public static final String revealPath = ID + '.' + revealKey;
	public static final String joinKey = "join";
	public static final String joinPath = ID + '.' + joinKey;

	private final Scoreboard scoreboard;
	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private final ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private final WorldBorderAreaController areaController;
	@Override
	public WorldBorderAreaController getAreaController() {
		return this.areaController;
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

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new ManhuntTeamController(
			this,
			builder.getChosenPlayers(config),
			builder.getChosenPrey(config)
		);
		this.areaController = (WorldBorderAreaController) new WorldBorderAreaController(
			this,
			this.teamController.getSelectedPrey().getLocation(),
			this.getManhuntBuilder().getLocation(config),
			this.getManhuntBuilder().getArea(config)
		)
			.withinWorld(builder.createWorldCreator());

		timer = new ManhuntTimer(this, builder.getReveal(config));
		compassEvents = new ManhuntCompass.Events(this);
	}

	@Override
	protected void onGameStart() {
		World world = areaController.getWorld();
		world.setTime(1000);

		Set<Player> hunters = teamController.getSelectedHunters();
		Player prey = teamController.getSelectedPrey();

		prey.getInventory().clear();
		Utility.revokeAllAdvancements(prey);


		for (Player hunter : hunters) {
			hunter.getInventory().clear();
			Utility.revokeAllAdvancements(hunter);
		}

		compassEvents.start();
		timer.start();

		actionBarTask = new BukkitRunnable() {
			@Override
			public void run() {
				if (lastPreyLocation == null) return;

				for (Player player : Bukkit.getOnlinePlayers()) {
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
				for (Player hunter : hunters) {
					hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
				prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
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
		Player prey = teamController.getTeamPrey();
		if (prey == null) return;

		lastPreyLocation = prey.getLocation();

		Bukkit.getServer().broadcast(
			Component.text("The Prey was revealed!\n")
			.append(Component.text("They are located at"))
			.append(Component.text(" X:" + lastPreyLocation.getBlockX()).color(NamedTextColor.RED))
			.append(Component.text(" Y:" + lastPreyLocation.getBlockY()).color(NamedTextColor.GREEN))
			.append(Component.text(" Z:" + lastPreyLocation.getBlockZ()).color(NamedTextColor.BLUE))
		);

		for (Player hunter : teamController.getTeamHunters()) {
			for (ManhuntCompass compass : ManhuntCompass.findAllIn(hunter.getInventory(), (ItemStack stack) -> ManhuntCompass.fromItemStack(stack, this))) {
				compass.setLocation(prey);
			}
		}

		prey.addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Set<Player> hunters = teamController.getTeamHunters();
		Player prey = teamController.getTeamPrey();

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

		var hunters = teamController.getTeamHunters();
		if (hunters.contains(player)) {
			hunters.remove(player);
		}

		var prey = teamController.getTeamPrey();

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
		private static final String allOption = "all";
		private static final String randomOption = "random";

		public static final List<String> areaOptions = Arrays.stream(WorldBorderAreaOption.values())
			.map(v -> v.name())
			.collect(Collectors.toList());
		public static final List<String> locationOptions = Arrays.stream(WorldBorderLocationOption.values())
			.map(v -> v.name())
			.collect(Collectors.toList());
		public static final List<String> revealOptions = Arrays.stream(ManhuntRevealOptions.values())
			.map(v -> v.name())
			.collect(Collectors.toList());
		public static final List<String> joinOptions = Arrays.stream(GameJoinOption.values())
			.map(v -> v.name())
			.collect(Collectors.toList());



		public String getPreyName(ConfigurationSection config) {
			return config.getString(preyPath);
		}
		public void setPreyName(ConfigurationSection config, String prey) {
			String value = prey == null ? null : prey;
			config.set(preyPath, value);
		}

		public Set<String> getPlayerNames(ConfigurationSection config) {
			return config.getStringList(playersPath).stream()
				.collect(Collectors.toSet());
		}
		public void setPlayerNames(ConfigurationSection config, Set<String> players) {
			List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
			config.set(playersPath, value);
		}

		public WorldBorderAreaOption getArea(ConfigurationSection config) {
			String areaString = config.getString(areaPath);
			return Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equals(areaString)).findFirst()
				.orElse(WorldBorderAreaOption.medium);
		}
		public void setArea(ConfigurationSection config, WorldBorderAreaOption area) {
			String value = area == null ? null : area.name();
			config.set(areaPath, value);
		}

		public WorldBorderLocationOption getLocation(ConfigurationSection config) {
			String locationString = config.getString(locationPath);
			return Arrays.stream(WorldBorderLocationOption.values()).filter(o -> o.name().equals(locationString)).findFirst()
				.orElse(WorldBorderLocationOption.random);
		}
		public void setLocation(ConfigurationSection config, WorldBorderLocationOption location) {
			String value = location == null ? null : location.name();
			config.set(locationPath, value);
		}

		public ManhuntRevealOptions getReveal(ConfigurationSection config) {
			String revealString = config.getString(revealPath);
			return Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.name().equals(revealString)).findFirst()
				.orElse(ManhuntRevealOptions.three_minutes);
		}
		public void setReveal(ConfigurationSection config, ManhuntRevealOptions reveal) {
			String value = reveal == null ? null : reveal.name();
			config.set(revealPath, value);
		}

		public GameJoinOption getJoinOption(ConfigurationSection config) {
			String joinString = config.getString(joinPath);
			return Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(joinString)).findFirst()
				.orElse(GameJoinOption.auto);
		}
		public void setJoinOption(ConfigurationSection config, GameJoinOption join) {
			String value = join == null ? null : join.name();
			config.set(joinPath, value);
		}


		public Builder(Ludos plugin) {
			super(plugin);
		}


		@Nullable
		public Set<Player> getChosenPlayers(ConfigurationSection config) {
			Set<String> playerNames = this.getPlayerNames(config);
			if (playerNames.isEmpty()) return null;

			return new HashSet<>(
				playerNames.stream()
					.map(Bukkit::getPlayerExact)
					.filter(p -> p != null)
					.collect(Collectors.toSet())
			);
		}

		@Nullable
		public Player getChosenPrey(ConfigurationSection config) {
			String preyName = this.getPreyName(config);
			if (preyName == null) {
				return null;
			}
			return Bukkit.getPlayerExact(preyName);
		}

		public String getPlayersString(ConfigurationSection config) {
			Set<String> playerNames = this.getPlayerNames(config);
			return playerNames.isEmpty() ? "All" : playerNames.stream() // TODO: Translate
				.collect(Collectors.joining(" "));
		}

		public String getPreyString(ConfigurationSection config) {
			String preyName = this.getPreyName(config);
			return preyName == null ? "Random" : preyName; // TODO: Translate
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

			return handleConfigsCommand(sender, command, label, config, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args, ManhuntGameConfigs configOption) {
			switch ( configOption ) {
			case prey:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPreyString(config) );
					return true;
				}

				String givenPreyName = args[0];

				if ( givenPreyName.equalsIgnoreCase(randomOption) ) {
					// Reset to default option
					setPreyName(config, null);

					sender.sendMessage("Prey player set to Random"); // TODO: Translate
					return true;
				}

				setPreyName(config, givenPreyName);

				sender.sendMessage( "Prey player set to " + getPreyString(config) );
				return true;

			case area:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getArea(config).name() );
					return true;
				}

				String givenArea = args[0];
				WorldBorderAreaOption areaOption = Arrays.stream(WorldBorderAreaOption.values()).filter(o -> o.name().equals(givenArea)).findFirst().orElse(null);
				if (areaOption == null) return false;

				setArea(config, areaOption);

				sender.sendMessage("Game area set to " + areaOption.name()); // TODO: Translate
				return true;

			case location:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( this.getLocation(config).name() );
					return true;
				}

				String givenLocation = args[0];
				WorldBorderLocationOption locationOption = Arrays.stream(WorldBorderLocationOption.values()).filter(o -> o.name().equals(givenLocation)).findFirst().orElse(null);
				if (locationOption == null) return false;

				setLocation(config, locationOption);

				sender.sendMessage("Game location set to " + locationOption.name()); // TODO: Translate
				return true;

			case reveal:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( this.getReveal(config).name() );
					return true;
				}

				String givenReveal = args[0];
				ManhuntRevealOptions revealOption = Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.name().equals(givenReveal)).findFirst().orElse(null);
				if (revealOption == null) return false;

				setReveal(config, revealOption);

				sender.sendMessage("Prey Reveal Frequency set to " + revealOption.displayName()); // TODO: Translate
				return true;

			case join:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( this.getJoinOption(config).name() );
					return true;
				}

				String givenJoin = args[0];
				GameJoinOption joinOption = Arrays.stream(GameJoinOption.values()).filter(o -> o.name().equals(givenJoin)).findFirst().orElse(null);
				if (joinOption == null) return false;

				setJoinOption(config, joinOption);

				sender.sendMessage("Game Join Option set to " + joinOption.name()); // TODO: Translate
				return true;
			}

			return false;
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
			ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( arg );

			return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
		}

		private List<String> handleConfigsTabComplete(CommandSender sender, Command command, String label, String[] args, ManhuntGameConfigs config) {
			List<String> allPlayers = CommandUtility.getOnlinePlayerNames();

			switch ( config ) {
			case prey:
				// Options are : any single player, or a random player
				allPlayers.add(randomOption);
				return allPlayers;

			case area:
				// Options are : large, medium, small
				if (args.length == 1)
					return areaOptions;

			case location:
				// Options are : random, here
				if (args.length == 1)
					return locationOptions;

			case reveal:
				// Options are : short, medium, long
				if (args.length == 1)
					return revealOptions;

			case join:
				// Options are : auto, manual, none
				if (args.length == 1)
					return joinOptions;
			}

			return null;
		}

		public @Nullable WorldCreator createWorldCreator() {
			String worldName = "manhunt_" + UUID.randomUUID();
			return new WorldCreator(worldName, new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), worldName))
				.environment(Environment.NORMAL)
				.type(WorldType.NORMAL)
				.generateStructures(true)
				.seed(new Random().nextLong());
		}

		@Override
		public ManhuntGame build(Group group) {
			return new ManhuntGame(this, group);
		}
	}
}