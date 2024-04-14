package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
// import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

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
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.Main;
import fr.ludos.command.CommandUtility;
import fr.ludos.command.PlayCommandOptions;
import fr.ludos.game.Game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;


public class ManhuntGame extends Game {

	public static final String manhuntKey = "Manhunt";
	public static final String playersKey = "Players";
	public static final String preyKey = "Prey";

	private Scoreboard scoreboard;
	private ManhuntTeamController teamController;

	private ManhuntCompass.Events compassEvents;
	private ManhuntTimer timer;

	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	protected ManhuntGame(Builder builder) {
		super(builder);

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getMainScoreboard();
		this.teamController = new ManhuntTeamController(this, builder.getChosenPlayers(), builder.getChosenPrey());

		PluginManager manager = Bukkit.getPluginManager();

		timer = new ManhuntTimer(this);
		manager.registerEvents(timer, Main.getInstance());

		compassEvents = new ManhuntCompass.Events();
		manager.registerEvents(compassEvents, Main.getInstance());


		Optional<Player> prey = teamController.getPrey();
		if (prey.isEmpty()) {
			stop();
			return;
		}
		Set<Player> hunters = teamController.getHunters();

		makeBorder(prey.get().getWorld(), prey.get().getLocation(), 200);
		for (Player hunter : hunters) {
			ManhuntCompass compass = ManhuntCompass.createItem(hunter);
			hunter.getInventory().addItem(compass.getStack());
		}

		// DEBUG
		// ManhuntCompass compass = ManhuntCompass.createItem(prey.get());
		// prey.get().getInventory().addItem(compass.getStack());

		Bukkit.broadcastMessage("The Game of Manhunt started");
	}


	public void revealPrey() {
		Optional<Player> prey = teamController.getPrey();
		if (prey.isEmpty()) {
			return;
		}

		Location preyLocation = prey.get().getLocation();

		Bukkit.broadcastMessage("The Prey was revealed!\nThey are located at " + preyLocation.getBlockX() + " " + preyLocation.getBlockY() + " " + preyLocation.getBlockZ() + ".");

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


	// public void makeBorder(Player player, int size) {
	// 	for (World world : Bukkit.getWorlds()) {
	// 		makeBorder(world, player.getLocation(), size);
	// 	}
	// }

	private void makeBorder(World world, Location center, int size) {
		WorldBorder border = world.getWorldBorder();
		border.setCenter(center);
		border.setSize(size);
	}

	@Override
	public void stop() {
		super.stop();

		teamController.stop();

		timer.stop();
		HandlerList.unregisterAll((Listener)timer);

		HandlerList.unregisterAll((Listener)compassEvents);


		Bukkit.broadcastMessage("The Game of Manhunt ended");
	}


	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		// if(
		//     event.getDamager() instanceof Player damager &&
		//     event.getEntity() instanceof Player entity &&
		//     teamController.areAllies(damager, entity)
		// ) {
		//     event.setCancelled(false);
		// }
	}

	// TODO: make custom snare effect
	// @EventHandler
	// public void onPlayerSnared(PlayerSnaredEvent event) {

	// }




	public static class Builder extends Game.Builder {
		private static final String allOption = "all";
		private static final String randomOption = "random";

		private String prey = null;
		private Set<String> players = null;


		public Builder() {
			Main main = Main.getInstance();

			players = main.getConfig().getStringList(manhuntKey + '.' + playersKey).stream()
				.collect(Collectors.toSet());

			if (players.size() == 0) {
				players = null;
			}
			prey = main.getConfig().getString(manhuntKey + '.' + preyKey);
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

		public void gameHelp(CommandSender sender, Command command, String label, PlayCommandOptions option) {
			switch ( option ) {
				case config:
					sender.sendMessage("Usage: /" + label + " config <config> [value]");
					sender.sendMessage("Available configs:");
					sender.sendMessage("  players [player1] [player2] ... [playerN]");
					sender.sendMessage("  prey [player]");
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
		public boolean gameCommand(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {
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
		public List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, PlayCommandOptions option) {
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
					break;
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

						main.getConfig().set(manhuntKey + '.' + playersKey, null);
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

					main.getConfig().set(manhuntKey + '.' + playersKey, players.stream().collect(Collectors.toList()));
					main.saveConfig();

					sender.sendMessage( getPlayersString() );
					return true;
				case prey:
					if ( args.length == 0 ) {
						// Field is left empty, send the current config
						sender.sendMessage( getPreyString() );
						return true;
					}

					if ( args[0].equalsIgnoreCase(randomOption) ) {
						// Reset to default option
						prey = null;

						main.getConfig().set(manhuntKey + '.' + preyKey, null);
						main.saveConfig();

						sender.sendMessage("Prey player set to Random"); // TODO: Translate
						return true;
					}

					prey = args[0];

					main.getConfig().set(manhuntKey + '.' + preyKey, prey);
					main.saveConfig();

					sender.sendMessage( getPreyString() );
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
			}

			return null;
		}

		@Override
		public ManhuntGame build() {
			return new ManhuntGame(this);
		}
	}

}