package fr.ludos.game.manhunt;

import java.util.Optional;
import java.util.Iterator;
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
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Ludos;
import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.game.Game;


public class ManhuntGame extends Game {
	public static final String id = "manhunt";

	public static final String playersKey = "players";
	public static final String playersPath = id + '.' + playersKey;
	public static final String preyKey = "prey";
	public static final String preyPath = id + '.' + preyKey;

	public static final String areaKey = "area";
	public static final String areaPath = id + '.' + areaKey;
	public static final String locationKey = "location";
	public static final String locationPath = id + '.' + locationKey;
	public static final String revealKey = "reveal";
	public static final String revealPath = id + '.' + revealKey;

	public static final String borderWorldUUIDKey = "borderWorldUUID";
	public static final String borderWorldUUIDPath = id + '.' + borderWorldUUIDKey;
	public static final String borderLocationKey = "borderLocation";
	public static final String borderLocationPath = id + '.' + borderLocationKey;
	public static final String borderSizeKey = "borderSize";
	public static final String borderSizePath = id + '.' + borderSizeKey;


	private final Scoreboard scoreboard;
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private final ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private final ManhuntCompass.Events compassEvents;
	private final ManhuntTimer timer;

	private final Builder builder;


	private Player prey;
	private Set<Player> hunters;


	private WorldBorder border;

	private Location lastPreyLocation = null;
	private BukkitTask actionBarTask;

	private BukkitTask saturationTask;


	@Nullable
	public static UUID getBorderWorldUID(Ludos plugin) {
		String value = plugin.getConfig().getString(borderWorldUUIDPath);

		if (value == null) return null;
		return UUID.fromString(value);
	}
	@Nullable
	public static Location getBorderLocation(Ludos plugin) {
		return plugin.getConfig().getLocation(borderLocationPath);
	}
	public static double getBorderSize(Ludos plugin) {
		return plugin.getConfig().getDouble(borderSizePath);
	}
	public static void setCachedBorder(World world, Ludos plugin) {
		FileConfiguration config = plugin.getConfig();
		if (world == null) {
			config.set(borderWorldUUIDPath, null);
			config.set(borderLocationPath, null);
			config.set(borderSizePath, null);
			plugin.saveConfig();
			return;
		}

		WorldBorder border = world.getWorldBorder();
		config.set(borderWorldUUIDPath, world.getUID().toString());
		config.set(borderLocationPath, border.getCenter());
		config.set(borderSizePath, border.getSize());
		plugin.saveConfig();
	}
	public static void resetBorder(Ludos plugin) {
		Location location = ManhuntGame.getBorderLocation(plugin);
		if (location == null) return;
		World world = Bukkit.getWorld(ManhuntGame.getBorderWorldUID(plugin));
		if (world == null) return;

		double size = ManhuntGame.getBorderSize(plugin);
		WorldBorder border = world.getWorldBorder();
		border.setCenter(location);
		border.setSize(size, 0);

		ManhuntGame.setCachedBorder(null, plugin);
	}


	protected ManhuntGame(Builder builder) {
		super(builder);

		this.builder = builder;

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new ManhuntTeamController(this, builder.getChosenPlayers(), builder.getChosenPrey());

		timer = new ManhuntTimer(this, builder.getReveal());
		compassEvents = new ManhuntCompass.Events(this);
	}

	private void setGameArea(Player prey, Set<Player> hunters, int areaDiameter, Location location) {
		World world = prey.getWorld();
		setCachedBorder(world, getPlugin());


		int areaRadius = areaDiameter / 2;

		prey.showTitle(Title.title(
			Component.text("You are the ")
				.append(Component.text("Prey")
					.color(NamedTextColor.BLUE)
				),
			Component.text("Run for your life"),
			Title.Times.times(
				Duration.ofMillis(500),
				Duration.ofMillis(3500),
				Duration.ofMillis(1000)
			)
		));

		double highestY = world.getHighestBlockYAt(location) + 1;
		location.setY(highestY);

		prey.teleport(location);
		prey.setBedSpawnLocation(location, true);
		prey.setGameMode(GameMode.SURVIVAL);
		prey.setHealth(prey.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

		prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));
		prey.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 1, false, false));
		prey.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 40, 0, false, true));

		Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
		for (Advancement advancement = iterator.next(); iterator.hasNext(); advancement = iterator.next()) {
			AdvancementProgress progress = prey.getAdvancementProgress(advancement);
			for (String criteria : progress.getAwardedCriteria())
			progress.revokeCriteria(criteria);
		}


		for (Player hunter : hunters) {
			hunter.showTitle(Title.title(
				Component.text("You are a ")
				.append(Component.text("Hunter")
					.color(NamedTextColor.RED)),
				Component.text("Go and seek ")
				.append(Component.text(prey.getName())
					.color(NamedTextColor.BLUE)),
				Title.Times.times(
					Duration.ofMillis(500),
					Duration.ofMillis(3500),
					Duration.ofMillis(1000)
				)
			));

			Location hunterLocation = Utility.getGroundedLocationAround(location, (int)(areaRadius * 0.3), (int)(areaRadius * 0.7), location);

			hunter.teleport(hunterLocation);
			hunter.setBedSpawnLocation(hunterLocation, true);
			hunter.setGameMode(GameMode.SURVIVAL);
			hunter.setHealth(hunter.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

			hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20 * 30, 0, false, false));

			iterator = Bukkit.getServer().advancementIterator();
			for (Advancement advancement = iterator.next(); iterator.hasNext(); advancement = iterator.next()) {
				AdvancementProgress progress = hunter.getAdvancementProgress(advancement);
				for (String criteria : progress.getAwardedCriteria())
				progress.revokeCriteria(criteria);
			}
		}

		border = world.getWorldBorder();
		border.setCenter(location);
		border.setSize(areaDiameter, 3);


		int limit = 0;

		EntityType[] animals = {
			EntityType.COW,
			EntityType.SHEEP,
			EntityType.PIG,
			EntityType.CHICKEN,
		};

		Random random = new Random();


		for (int i = 0; i < limit; i++) {
			int x = location.getBlockX() + random.nextInt(areaRadius * 2) - areaRadius;
			int z = location.getBlockZ() + random.nextInt(areaRadius * 2) - areaRadius;

			Location spawnLocation = new Location(world, x, world.getHighestBlockYAt(x, z) + 2, z);
			EntityType animal = animals[random.nextInt(animals.length)];

			Bukkit.getServer().broadcast(Component.text(animal.toString()));
			Bukkit.getServer().broadcast(Component.text(spawnLocation.toString()));

			world.spawnEntity(spawnLocation, animal);
		}
	}

	@Override
	protected void onInit() {
		teamController.start();


		Optional<Player> nullablePrey = teamController.getPrey();
		if (nullablePrey.isEmpty()) {
			throw new IllegalArgumentException("No players were found");
		}
		prey = nullablePrey.get();
		hunters = teamController.getHunters();


		prey.getInventory().clear();
		for (Player hunter : hunters) {
			hunter.getInventory().clear();
		}
	}

	@Override
	protected void onStart() {
		compassEvents.start();
		timer.start();

		int areaDiameter = builder.getArea().getSize();

		ManhuntLocationOptions locationOption = builder.getLocation();
		Location gameLocation = prey.getLocation();
		if (locationOption == ManhuntLocationOptions.random) {
			gameLocation = Utility.getGroundedLocationAround(gameLocation, 300, 2500, gameLocation);
		}

		setGameArea(prey, hunters, areaDiameter, gameLocation);
		prey.getWorld().setTime(1000);

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
		}.runTaskTimer(getPlugin(), 1, 1);

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
	protected void onStop() {
		teamController.stop();

		compassEvents.stop();
		timer.stop();


		resetBorder(getPlugin());

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
		Optional<Player> prey = teamController.getPrey();
		if (prey.isEmpty()) {
			return;
		}

		lastPreyLocation = prey.get().getLocation();

		Bukkit.getServer().broadcast(
			Component.text("The Prey was revealed!\n")
			.append(Component.text("They are located at"))
			.append(Component.text(" X:" + lastPreyLocation.getBlockX()).color(NamedTextColor.RED))
			.append(Component.text(" Y:" + lastPreyLocation.getBlockY()).color(NamedTextColor.GREEN))
			.append(Component.text(" Z:" + lastPreyLocation.getBlockZ()).color(NamedTextColor.BLUE))
		);

		for (Player hunter : teamController.getHunters()) {
			for (ManhuntCompass compass : ManhuntCompass.findAllIn(hunter.getInventory(), (ItemStack stack) -> ManhuntCompass.fromItemStack(stack, this))) {
				compass.setLocation(prey.get());
			}
		}

		prey.get().addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Set<Player> hunters = teamController.getHunters();
		Optional<Player> prey = teamController.getPrey();

		if (hunters.isEmpty() || ! prey.isEmpty()) {
			timer.resume();
		}

		var player = event.getPlayer();

		if (!hunters.contains(player) && !(prey.isPresent() && prey.get() == player)) {
			player.setGameMode(GameMode.SPECTATOR);

			player.teleport(prey.get().getLocation());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		var hunters = teamController.getHunters();
		if (hunters.contains(player)) {
			hunters.remove(player);
		}

		var prey = teamController.getPrey();
		if (prey.isPresent() && prey.get() == player) {
			prey = Optional.empty();
		}

		if (hunters.isEmpty() && prey.isEmpty()) {
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

		public static final List<String> areaOptions = Arrays.stream(ManhuntAreaOptions.values())
			.map(v -> v.toString())
			.collect(Collectors.toList());
		public static final List<String> locationOptions = Arrays.stream(ManhuntLocationOptions.values())
			.map(v -> v.toString())
			.collect(Collectors.toList());
		public static final List<String> revealOptions = Arrays.stream(ManhuntRevealOptions.values())
			.map(v -> v.toString())
			.collect(Collectors.toList());



		public String getPreyName() {
			return getPlugin().getConfig().getString(preyPath);
		}
		public void setPreyName(String prey) {
			String value = prey == null ? null : prey;
			getPlugin().getConfig().set(preyPath, value);
			getPlugin().saveConfig();
		}

		public Set<String> getPlayerNames() {
			return getPlugin().getConfig().getStringList(playersPath).stream()
				.collect(Collectors.toSet());
		}
		public void setPlayerNames(Set<String> players) {
			List<String> value = players == null ? null : players.stream().collect(Collectors.toList());
			getPlugin().getConfig().set(playersPath, value);
			getPlugin().saveConfig();
		}

		public ManhuntAreaOptions getArea() {
			String areaString = getPlugin().getConfig().getString(areaPath);
			return Arrays.stream(ManhuntAreaOptions.values()).filter(o -> o.toString().equals(areaString)).findFirst()
				.orElse(ManhuntAreaOptions.medium);
		}
		public void setArea(ManhuntAreaOptions area) {
			String value = area == null ? null : area.toString();
			getPlugin().getConfig().set(areaPath, value);
			getPlugin().saveConfig();
		}

		public ManhuntLocationOptions getLocation() {
			String locationString = getPlugin().getConfig().getString(locationPath);
			return Arrays.stream(ManhuntLocationOptions.values()).filter(o -> o.toString().equals(locationString)).findFirst()
				.orElse(ManhuntLocationOptions.random);
		}
		public void setLocation(ManhuntLocationOptions location) {
			String value = location == null ? null : location.toString();
			getPlugin().getConfig().set(locationPath, value);
			getPlugin().saveConfig();
		}

		public ManhuntRevealOptions getReveal() {
			String revealString = getPlugin().getConfig().getString(revealPath);
			return Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.toString().equals(revealString)).findFirst()
				.orElse(ManhuntRevealOptions.three_minutes);
		}
		public void setReveal(ManhuntRevealOptions reveal) {
			String value = reveal == null ? null : reveal.toString();
			getPlugin().getConfig().set(revealPath, value);
			getPlugin().saveConfig();
		}


		public Builder(Ludos plugin) {
			super( plugin );

			resetBorder(plugin);
		}


		@Nullable
		public Set<Player> getChosenPlayers() {
			Set<String> playerNames = this.getPlayerNames();
			if (playerNames.isEmpty()) return null;

			return new HashSet<>(
				playerNames.stream()
					.map(Bukkit::getPlayerExact)
					.filter(p -> p != null)
					.collect(Collectors.toSet())
			);
		}

		@Nullable
		public Player getChosenPrey() {
			String preyName = this.getPreyName();
			if (preyName == null) {
				return null;
			}
			return Bukkit.getPlayerExact(preyName);
		}




		public String getPlayersString() {
			Set<String> playerNames = this.getPlayerNames();
			return playerNames.isEmpty() ? "All" : playerNames.stream() // TODO: Translate
				.collect(Collectors.joining(" "));
		}

		public String getPreyString() {
			String preyName = this.getPreyName();
			return preyName == null ? "Random" : preyName; // TODO: Translate
		}


		@Override
		public String getId() {
			return id;
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
				usage.append("\n  ").append(config.toString()).append(" ")
					.append(config.getUsage());
			}

			return usage.toString();
		}

		@Override
		public boolean executeGameConfig(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				return false;
			}

			String arg = args[0];
			ManhuntGameConfigs option = Arrays.stream(ManhuntGameConfigs.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
			if (option == null) return false;

			return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
		}

		private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args, ManhuntGameConfigs config) {
			switch ( config ) {
			case players:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPlayersString() );
					return true;
				}

				if ( args[0].equalsIgnoreCase(allOption) ) {
					// Reset to default option
					setPlayerNames(null);

					sender.sendMessage("All players included in the game"); // TODO: Translate
					return true;
				}

				setPlayerNames( new HashSet<>(Arrays.asList(args)) );

				sender.sendMessage( getPlayersString() );
				return true;

			case prey:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPreyString() );
					return true;
				}

				String givenPreyName = args[0];

				if ( givenPreyName.equalsIgnoreCase(randomOption) ) {
					// Reset to default option
					setPreyName(null);

					sender.sendMessage("Prey player set to Random"); // TODO: Translate
					return true;
				}

				setPreyName(givenPreyName);

				sender.sendMessage( getPreyString() );
				return true;

			case area:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getArea().toString() );
					return true;
				}

				String givenArea = args[0];
				ManhuntAreaOptions areaOption = Arrays.stream(ManhuntAreaOptions.values()).filter(o -> o.toString().equals(givenArea)).findFirst().orElse(null);
				if (areaOption == null) return false;

				setArea(areaOption);

				sender.sendMessage("Game area set to " + areaOption.toString()); // TODO: Translate
				return true;

			case location:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( this.getLocation().toString() );
					return true;
				}

				String givenLocation = args[0];
				ManhuntLocationOptions locationOption = Arrays.stream(ManhuntLocationOptions.values()).filter(o -> o.toString().equals(givenLocation)).findFirst().orElse(null);
				if (locationOption == null) return false;

				setLocation(locationOption);

				sender.sendMessage("Game location set to " + locationOption.toString()); // TODO: Translate
				return true;

			case reveal:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( this.getReveal().toString() );
					return true;
				}

				String givenReveal = args[0];
				ManhuntRevealOptions revealOption = Arrays.stream(ManhuntRevealOptions.values()).filter(o -> o.toString().equals(givenReveal)).findFirst().orElse(null);
				if (revealOption == null) return false;

				setReveal(revealOption);

				sender.sendMessage("Prey Reveal Frequency set to " + revealOption.toString()); // TODO: Translate
				return true;
			}

			return false;
		}


		@Override
		public List<String> gameConfigTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length <= 1) {
				return Arrays.stream(ManhuntGameConfigs.values())
					.map(ManhuntGameConfigs::toString)
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
			case players:
				// Options are : any enumeration of players, or all players
				if ( args.length == 1 ) {
					allPlayers.add(allOption);
				}
				return allPlayers;

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
			}

			return null;
		}

		@Override
		public ManhuntGame build() {
			return new ManhuntGame(this);
		}
	}
}