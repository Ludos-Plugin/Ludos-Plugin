package fr.ludos.game;

import fr.ludos.Main;
import fr.ludos.command.GameCommandOptions;
import fr.ludos.role.Role;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.command.TabExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import javax.annotation.Nullable;


public abstract class Game implements Listener {

	@Nullable
	private static Game current = null;
	private static final Map<String, Builder> registered = new HashMap<String, Builder>();

	public final Map<String, Role> activeRoles = new HashMap<String, Role>();


	public abstract TeamController getTeamController();

	@Nullable
	public static Game getCurrent() {
		return Game.current;
	}
	public static Map<String, Builder> getRegistered() {
		return Game.registered;
	}


	public static void registerGame(Builder builder) {
		Game.registered.put(builder.getId(), builder);
	}

	public static void startGame(Builder builder) {
		stopGame();

		current = builder.build();
	}
	public static void startGame(String id) {
		if ( ! registered.containsKey(id) ) {
			return;
		}

		startGame(registered.get(id));
	}
	public static void stopGame() {
		if (current != null) {
			current.stop();
			current = null;
		}
	}

	protected void registerRoles(Builder gameBuilder) {
		for (Role.Builder roleBuilder : Role.getRegistered().values()) {
			activeRoles.put(roleBuilder.getId(), roleBuilder.build(gameBuilder));
		}
	}

	public Game(Builder gameBuilder) {
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}

	protected void stopRoles() {
		for (Role role : activeRoles.values()) {
			role.stop();
		}
		activeRoles.clear();
	}

	public void stop() {
		HandlerList.unregisterAll(this);
	}



	public static abstract class Builder implements TabExecutor {
		public abstract String getId();

		public String getConfigKey(String key) {
			return getId() + '.' + key;
		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
			if (args.length == 0) {
				return false;
			}

			String arg = args[0];
			if ( ! EnumUtils.isValidEnum(GameCommandOptions.class, arg) ) {
				return false;
			}
			GameCommandOptions option = GameCommandOptions.valueOf( arg );

			return gameCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
		}

		@Override
		public final List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
			if (args.length < 2) {
				return Arrays.stream(GameCommandOptions.values())
					.map(GameCommandOptions::toString)
					.sorted()
					.collect(Collectors.toList());
			}

			String arg = args[0];
			if ( ! EnumUtils.isValidEnum(GameCommandOptions.class, arg) ) {
				return null;
			}
			GameCommandOptions option = GameCommandOptions.valueOf( arg );

			return gameTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length), option);
		}

		public abstract boolean gameCommand(CommandSender sender, Command command, String label, String[] args, GameCommandOptions option);
		public abstract List<String> gameTabComplete(CommandSender sender, Command command, String label, String[] args, GameCommandOptions option);

		public abstract Game build();
	}
}