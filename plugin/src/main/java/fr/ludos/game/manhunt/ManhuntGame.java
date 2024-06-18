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
import java.util.Optional;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.Main;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.GameCommandOptions;
import fr.ludos.game.Game;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.WorldBorder;


public class ManhuntGame extends Game {
	public static final String manhuntKey = "Manhunt";
	public static final String playersKey = "Players";
	public static final String preyKey = "Prey";
	public static final String areaKey = "Area";
	public static final String locationKey = "Location";
	public static final String revealKey = "Reveal";

	private Scoreboard scoreboard;
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private ManhuntTeamController teamController;
	@Override
	public ManhuntTeamController getTeamController() {
		return this.teamController;
	}

	private ManhuntCompass.Events compassEvents;
	private ManhuntTimer timer;

	private WorldBorder border;
	private Location borderResetCenter;
	private double borderResetSize;

	private BukkitTask saturationTask;


	protected ManhuntGame(Builder builder) {
		super(builder);

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new ManhuntTeamController(this, builder.getChosenPlayers(), builder.getChosenPrey());

		timer = new ManhuntTimer(this, builder.reveal.getDuration());
		compassEvents = new ManhuntCompass.Events();


		Optional<Player> nullablePrey = teamController.getPrey();
		if (nullablePrey.isEmpty()) {
			stop();
			return;
		}
		Player prey = nullablePrey.get();

		int areaDiameter = builder.getArea().getSize();
		int areaRadius = areaDiameter / 2;

		ManhuntLocationOptions locationOption = builder.getLocation();

		prey.sendTitle("You are the " + ChatColor.BLUE + "Prey", "Run for your life", 10, 70, 20);

		if (locationOption == ManhuntLocationOptions.random) {
			prey.teleport(getGroundedLocationAround(prey.getLocation(), 300, 2500));
			prey.setBedSpawnLocation(prey.getLocation(), true);
		}

		prey.getInventory().clear();
		prey.setGameMode(GameMode.SURVIVAL);

		prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 600, 0, false, false));
		prey.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1, false, false));
		prey.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 800, 0, false, false));
		prey.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 99, false, false));

		Set<Player> hunters = teamController.getHunters();
		for (Player hunter : hunters) {
			hunter.sendTitle("You are a " + ChatColor.RED + "Hunter", "Go and seek the Prey", 10, 70, 20);

			hunter.teleport(getGroundedLocationAround(prey.getLocation(), (int)(areaRadius * 0.3), (int)(areaRadius * 0.8)));
			hunter.setBedSpawnLocation(hunter.getLocation(), true);

			hunter.getInventory().clear();
			hunter.setGameMode(GameMode.SURVIVAL);

			hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 600, 0, false, false));
			hunter.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 99, false, false));
			hunter.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 99, false, false));
			compassEvents.updateItemInInventory(hunter);
		}


		border = prey.getWorld().getWorldBorder();
		borderResetCenter = border.getCenter();
		borderResetSize = border.getSize();

		border.setCenter(prey.getLocation());
		border.setSize(areaDiameter, 10);


		registerRoles(builder);

		Bukkit.broadcastMessage("The Game of Manhunt started");

		saturationTask = new BukkitRunnable() {
            @Override
            public void run() {
				for (Player hunter : hunters) {
					hunter.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
				}
				prey.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1, 0, true, false));
            }
        }.runTaskTimer(Main.getInstance(), 400, 400);
	}

	@Override
	public void stop() {
		super.stop();

		teamController.stop();
		timer.stop();

		border.setSize(borderResetSize, 0);
		border.setCenter(borderResetCenter);

		saturationTask.cancel();
		new BukkitRunnable() {
            @Override
            public void run() {
				compassEvents.stop();
				stopRoles();

				Bukkit.broadcastMessage("The Game of Manhunt ended");
            }
        }.runTaskLater(Main.getInstance(), 2);
	}



	private Location getGroundedLocationAround(Location searchOrigin, int min, int max) {
		Random rand = new Random();

		int retries = 0;

		Location location;
		do {
			location = searchOrigin;
			location.setX(searchOrigin.getX() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1));
			location.setZ(searchOrigin.getZ() + rand.nextInt(min, max + 1) * (rand.nextBoolean() ? 1 : -1));
			location.setY(location.getWorld().getHighestBlockYAt(location));

			retries++;
		}
		while (location.getBlock().isLiquid() && retries < 50);

		if (retries == 0) {
			Bukkit.broadcastMessage("HOLY SHIT 50 TRIES WT F");
		}

		location.setY(location.getY() + 1);

		return location;
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

		// DEBUG
		// for (ManhuntCompass compass : ManhuntCompass.findAllIn(prey.get().getInventory(), ManhuntCompass::getItem)) {
		// 	compass.setLocation(prey.get());
		// }

		prey.get().addPotionEffect(PotionEffectType.GLOWING.createEffect(100, 0));
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

		if (hunters.size() == 0 && prey.isEmpty()) {
			timer.pause();
		}
    }

    public static class Builder extends Game.Builder {
        private static final String allOption = "all";
        private static final String randomOption = "random";

        public static final List<String> areaOptions = Arrays.stream(ManhuntAreaOptions.values())
			.map(v -> v.getName())
			.collect(Collectors.toList());
        public static final List<String> locationOptions = Arrays.stream(ManhuntLocationOptions.values())
			.map(v -> v.getName())
			.collect(Collectors.toList());
        public static final List<String> revealOptions = Arrays.stream(ManhuntRevealOptions.values())
			.map(v -> v.getName())
			.collect(Collectors.toList());


        private String prey = null;
        private Set<String> players = null;

        private ManhuntAreaOptions area = ManhuntAreaOptions.medium;
		public ManhuntAreaOptions getArea() {
			return area;
		}

        private ManhuntLocationOptions location = ManhuntLocationOptions.random;
		public ManhuntLocationOptions getLocation() {
			return location;
		}

        private ManhuntRevealOptions reveal = ManhuntRevealOptions.medium;
		public ManhuntRevealOptions getReveal() {
			return reveal;
		}


		public Builder() {
			Main main = Main.getInstance();

			players = main.getConfig().getStringList(getConfigKey(playersKey)).stream()
				.collect(Collectors.toSet());

			if (players.size() == 0) {
				players = null;
			}
			prey = main.getConfig().getString(getConfigKey(preyKey), null);

			String areaString = main.getConfig().getString(getConfigKey(areaKey), ManhuntAreaOptions.medium.getName());
			area = EnumUtils.isValidEnum(ManhuntAreaOptions.class, areaString)
				? ManhuntAreaOptions.valueOf( areaString )
				: ManhuntAreaOptions.medium;

			String locationString = main.getConfig().getString(getConfigKey(locationKey), ManhuntLocationOptions.random.getName());
			location = EnumUtils.isValidEnum(ManhuntLocationOptions.class, locationString)
				? ManhuntLocationOptions.valueOf( locationString )
				: ManhuntLocationOptions.random;

			String revealString = main.getConfig().getString(getConfigKey(revealKey), ManhuntRevealOptions.medium.getName());
			reveal = EnumUtils.isValidEnum(ManhuntRevealOptions.class, revealString)
				? ManhuntRevealOptions.valueOf( revealString )
				: ManhuntRevealOptions.medium;
		}


		@Nullable
		public Set<Player> getChosenPlayers() {
			if (players == null) {
				return null;
			}
			return new HashSet<Player>(
				players.stream()
					.map(Bukkit::getPlayerExact)
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
			return players == null ? "All" : players.stream() // TODO: Translate
				.collect(Collectors.joining(" "));
		}

		public String getPreyString() {
			return prey == null ? "Random" : prey; // TODO: Translate
		}


		@Override
		public String getId() {
			return "manhunt";
		}

		public void gameHelp(CommandSender sender, Command command, String label, GameCommandOptions option) {
			switch ( option ) {
			case config:
				sender.sendMessage("Usage: /" + label + " config <config> [value]");
				sender.sendMessage("Available configs:");
				sender.sendMessage("  players [player1] [player2] ... [playerN]");
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
				Game.startGame(this);
				break;
			case stop:
				Game.stopGame();
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
			Main main = Main.getInstance();
			switch ( config ) {
			case players:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( getPlayersString() );
					return true;
				}

				if ( args[0].equalsIgnoreCase(allOption) ) {
					// Reset to default option
					players = null;

					main.getConfig().set(getConfigKey(playersKey), null);
					main.saveConfig();

					sender.sendMessage("All players included in the game"); // TODO: Translate
					return true;
				}

				players = new HashSet<String>();
				for ( int i = 0; i < args.length; i++) {
					players.add(args[i]);
				}
				if (players.isEmpty()) {
					players = null;
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
					sender.sendMessage( area.getName() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntAreaOptions.class, option) ) {
					return false;
				}
				ManhuntAreaOptions areaOption = ManhuntAreaOptions.valueOf( option );

				area = areaOption;

				main.getConfig().set(getConfigKey(areaKey), area);
				main.saveConfig();

				sender.sendMessage("Game area set to " + area); // TODO: Translate
				return true;

			case location:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( location.getName() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntLocationOptions.class, option) ) {
					return false;
				}
				ManhuntLocationOptions locationOption = ManhuntLocationOptions.valueOf( option );

				location = locationOption;

				main.getConfig().set(getConfigKey(locationKey), location);
				main.saveConfig();

				sender.sendMessage("Game location set to " + location); // TODO: Translate
				return true;

			case reveal:
				if ( args.length == 0 ) {
					// Field is left empty, send the current config
					sender.sendMessage( reveal.getName() );
					return true;
				}

				option = args[0];
				if ( ! EnumUtils.isValidEnum(ManhuntRevealOptions.class, option) ) {
					return false;
				}
				ManhuntRevealOptions revealOption = ManhuntRevealOptions.valueOf( option );

				reveal = revealOption;

				main.getConfig().set(getConfigKey(revealKey), reveal);
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

				if ( args.length == 1 ) {
					allPlayers.add(randomOption);
					return allPlayers;
				}
				return null;
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