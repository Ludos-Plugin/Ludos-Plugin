package fr.ludos.game.manhunt;
import java.util.Random;
import org.bukkit.Bukkit;
// import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.apache.commons.lang3.EnumUtils;

import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.Ludos;
import fr.ludos.Utility;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.GameCommandOptions;
import fr.ludos.game.Game;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;


public class ManhuntGame extends Game {
	public static final String id = "manhunt";

	public static final String playersKey = "players";
	public static final String preyKey = "prey";
	public static final String areaKey = "area";
	public static final String locationKey = "location";
	public static final String revealKey = "reveal";

	private Scoreboard scoreboard;
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private final ManhuntCompass.Events compassEvents;
	private final ManhuntTimer timer;

	private final Builder builder;
	private final Player prey;
	private final Set<Player> hunters;

	private WorldBorder border;
	private Location borderResetCenter;
	private double borderResetSize;

	private BukkitTask saturationTask;


	protected ManhuntGame(Builder builder) {
		super(builder);

		this.builder = builder;

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new ManhuntTeamController(this, builder.getChosenPlayers(), builder.getChosenPrey());


		Optional<Player> nullablePrey = teamController.getPrey();
		if (nullablePrey.isEmpty()) {
			throw new IllegalArgumentException("No players were found");
		}
		prey = nullablePrey.get();
		hunters = teamController.getHunters();


		timer = new ManhuntTimer(this, builder.reveal.getDuration());
		compassEvents = new ManhuntCompass.Events(this);
	}

	private void setGameArea(Player prey, Set<Player> hunters, int areaDiameter, Location location) {
		World world = prey.getWorld();
		int areaRadius = areaDiameter / 2;

		prey.sendTitle("You are the " + ChatColor.BLUE + "Prey", "Run for your life", 10, 70, 20);

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
			hunter.sendTitle("You are a " + ChatColor.RED + "Hunter", "Go and seek " + ChatColor.BLUE + prey.getName(), 10, 70, 20);

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
		borderResetCenter = border.getCenter();
		borderResetSize = border.getSize();

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

			Bukkit.broadcastMessage(animal.toString());
			Bukkit.broadcastMessage(spawnLocation.toString());

			world.spawnEntity(spawnLocation, animal);
		}
	}

	@Override
	protected void onInit() {
		prey.getInventory().clear();
		for (Player hunter : hunters) {
			hunter.getInventory().clear();
		}
	}

	@Override
	protected void onStart() {
		int areaDiameter = builder.getArea().getSize();

		ManhuntLocationOptions locationOption = builder.getLocation();
		Location gameLocation = prey.getLocation();
		if (locationOption == ManhuntLocationOptions.random) {
			gameLocation = Utility.getGroundedLocationAround(gameLocation, 300, 2500, gameLocation);
		}

		setGameArea(prey, hunters, areaDiameter, gameLocation);
		prey.getWorld().setTime(1000);


		compassEvents.start();
		timer.start();

		saturationTask = new BukkitRunnable() {
			@Override
			public void run() {
				for (Player hunter : hunters) {
					hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
				prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
			}
		}.runTaskTimer(Ludos.getInstance(), 400, 400);


		Bukkit.broadcastMessage("The Game of Manhunt started");
	}

	@Override
	protected void onStop() {
		border.setSize(borderResetSize, 0);
		border.setCenter(borderResetCenter);

		teamController.stop();

		compassEvents.stop();
		timer.stop();

		saturationTask.cancel();

		Bukkit.broadcastMessage("The Game of Manhunt ended");
	}


	public void revealPrey() {
		Optional<Player> prey = teamController.getPrey();
		if (prey.isEmpty()) {
			return;
		}

		Location preyLocation = prey.get().getLocation();

		Bukkit.broadcastMessage("The Prey was revealed!\nThey are located at " + ChatColor.RED + "X:" + preyLocation.getBlockX() + ChatColor.GREEN + " Y:" + preyLocation.getBlockY() + ChatColor.BLUE + " Z:" + preyLocation.getBlockZ() + ".");

		for (Player hunter : teamController.getHunters()) {
			for (ManhuntCompass compass : ManhuntCompass.findAllIn(hunter.getInventory(), ManhuntCompass::getItem)) {
				compass.setLocation(prey.get());
			}
		}

		prey.get().addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Utility.onDeathSpectate(event.getEntity(), 5);
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		var hunters = teamController.getHunters();
		var prey = teamController.getPrey();

		if (hunters.size() > 0 || ! prey.isEmpty()) {
			timer.resume();
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		var hunters = teamController.getHunters();
		if (hunters.contains(event.getPlayer())) {
			hunters.remove(event.getPlayer());
		}
		var prey = teamController.getPrey();
		if (prey.isPresent() && prey.get() == event.getPlayer()) {
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


		private String prey = null;
		private final Set<String> players;

		private ManhuntAreaOptions area = ManhuntAreaOptions.medium;
		public ManhuntAreaOptions getArea() {
			return area;
		}

		private ManhuntLocationOptions location = ManhuntLocationOptions.random;
		public ManhuntLocationOptions getLocation() {
			return location;
		}

		private ManhuntRevealOptions reveal = ManhuntRevealOptions.occasional;
		public ManhuntRevealOptions getReveal() {
			return reveal;
		}


		public Builder() {
			Ludos main = Ludos.getInstance();

			players = main.getConfig().getStringList(getConfigKey(playersKey)).stream()
				.collect(Collectors.toSet());

			prey = main.getConfig().getString(getConfigKey(preyKey), null);

			String areaString = main.getConfig().getString(getConfigKey(areaKey));
			area = EnumUtils.isValidEnum(ManhuntAreaOptions.class, areaString)
				? ManhuntAreaOptions.valueOf( areaString )
				: ManhuntAreaOptions.medium;

			String locationString = main.getConfig().getString(getConfigKey(locationKey));
			location = EnumUtils.isValidEnum(ManhuntLocationOptions.class, locationString)
				? ManhuntLocationOptions.valueOf( locationString )
				: ManhuntLocationOptions.random;

			String revealString = main.getConfig().getString(getConfigKey(revealKey));
			reveal = EnumUtils.isValidEnum(ManhuntRevealOptions.class, revealString)
				? ManhuntRevealOptions.valueOf( revealString )
				: ManhuntRevealOptions.occasional;
		}


		@Nullable
		public Set<Player> getChosenPlayers() {
			if (players.isEmpty()) return null;

			return new HashSet<Player>(
				players.stream()
					.map(Bukkit::getPlayerExact)
					.filter(p -> p != null)
					.collect(Collectors.toSet())
			);
		}

		@Nullable
		public Player getChosenPrey() {
			if (prey == null) {
				return null;
			}
			return Bukkit.getPlayerExact(prey);
		}




		public String getPlayersString() {
			return players.isEmpty() ? "All" : players.stream() // TODO: Translate
				.collect(Collectors.joining(" "));
		}

		public String getPreyString() {
			return prey == null ? "Random" : prey; // TODO: Translate
		}


		@Override
		public String getId() {
			return id;
		}

		public void gameHelp(CommandSender sender, Command command, String label, GameCommandOptions option) {
			switch ( option ) {
			case config:
				sender.sendMessage("Usage: /" + label + " config <config> [value]");
				sender.sendMessage("Available configs:");
				sender.sendMessage("  players [player1] [player2] ...");
				sender.sendMessage("  prey [player]");
				sender.sendMessage("  area <large|medium|small>");
				sender.sendMessage("  location <random|here>");
				sender.sendMessage("  frequency <short|medium|long>");
				break;
			case start:
				sender.sendMessage("Usage: /" + label + " start");
				break;
			case stop:
				sender.sendMessage("Usage: /" + label + " stop");
				break;
			}
		}

		@Override
		public boolean gameCommand(CommandSender sender, Command command, String label, String[] args, GameCommandOptions option) {
			switch ( option ) {
			case config:
				if (args.length == 0) {
					return false;
				}

				String arg = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, arg) ) {
					return false;
				}
				ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( arg );

				return handleConfigsCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
			case start:
				build().start();
				break;
			case stop:
				Game.stopCurrentGame();
				break;
			}

			return true;
		}

		@Override
		public List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, GameCommandOptions option) {
			if (args.length == 0) {
				return null;
			}

			switch ( option ) {
			case config:
				if (args.length == 1) {
					// Show all configs
					return Arrays.stream(ManhuntGameConfigs.values())
						.map(ManhuntGameConfigs::toString)
						.sorted()
						.collect(Collectors.toList());
				}

				String arg = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntGameConfigs.class, arg) ) {
					return null;
				}
				ManhuntGameConfigs config = ManhuntGameConfigs.valueOf( arg );

				return handleConfigsTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), config);
			case start:
			case stop:
				break;
			}

			return null;
		}

		private boolean handleConfigsCommand(CommandSender sender, Command command, String label, String[] args, ManhuntGameConfigs config) {
			Ludos main = Ludos.getInstance();
			switch ( config ) {
			case players:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPlayersString() );
					return true;
				}

				if ( args[0].equalsIgnoreCase(allOption) ) {
					// Reset to default option
					players.clear();

					main.getConfig().set(getConfigKey(playersKey), null);
					main.saveConfig();

					sender.sendMessage("All players included in the game"); // TODO: Translate
					return true;
				}

				players.clear();
				for ( int i = 0; i < args.length; i++) {
					players.add(args[i]);
				}

				main.getConfig().set(getConfigKey(playersKey), players.stream().collect(Collectors.toList()));
				main.saveConfig();

				sender.sendMessage( getPlayersString() );
				return true;

			case prey:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPreyString() );
					return true;
				}

				String option = args[0];

				if ( args[0].equalsIgnoreCase(randomOption) ) {
					// Reset to default option
					prey = null;

					main.getConfig().set(getConfigKey(preyKey), null);
					main.saveConfig();

					sender.sendMessage("Prey player set to Random"); // TODO: Translate
					return true;
				}

				prey = args[0];

				main.getConfig().set(getConfigKey(preyKey), prey);
				main.saveConfig();

				sender.sendMessage( getPreyString() );
				return true;

			case area:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( area.toString() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntAreaOptions.class, option) ) {
					return false;
				}
				ManhuntAreaOptions areaOption = ManhuntAreaOptions.valueOf( option );

				area = areaOption;

				main.getConfig().set(getConfigKey(areaKey), area.toString());
				main.saveConfig();

				sender.sendMessage("Game area set to " + area); // TODO: Translate
				return true;

			case location:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( location.toString() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntLocationOptions.class, option) ) {
					return false;
				}
				ManhuntLocationOptions locationOption = ManhuntLocationOptions.valueOf( option );

				location = locationOption;

				main.getConfig().set(getConfigKey(locationKey), location.toString());
				main.saveConfig();

				sender.sendMessage("Game location set to " + location); // TODO: Translate
				return true;

			case reveal:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( reveal.toString() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntRevealOptions.class, option) ) {
					return false;
				}
				ManhuntRevealOptions revealOption = ManhuntRevealOptions.valueOf( option );

				reveal = revealOption;

				main.getConfig().set(getConfigKey(revealKey), reveal.toString());
				main.saveConfig();

				sender.sendMessage("Prey Reveal Frequency set to " + reveal); // TODO: Translate
				return true;
			}

			return false;
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
				return areaOptions;

			case location:
				// Options are : random, here
				return locationOptions;

			case reveal:
				// Options are : short, medium, long
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