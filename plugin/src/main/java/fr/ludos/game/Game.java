package fr.ludos.game;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.lang3.EnumUtils;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.ludos.Ludos;
import fr.ludos.role.Role;
import fr.ludos.command.GameCommandOptions;


public abstract class Game implements Listener {

	@Nullable
	private static Game current = null;
	@Nullable
	public static Game getCurrent() {
		return current;
	}

	private boolean started = false;

	private static final Map<String, Builder> registered = new HashMap<>();
	public static Map<String, Builder> getRegistered() {
		return registered;
	}

	private final Map<String, Role> activeRoles = new HashMap<>();
	public Map<String, Role> getActiveRoles() {
		return activeRoles;
	}

	private final Builder builder;
	protected Builder getBuilder() {
		return builder;
	}

	public Ludos getPlugin() {
		return builder.getPlugin();
	}

	public Game(Builder builder) {
		this.builder = builder;
	}

	public final void start() {
		if (started) return;
		started = true;

		stopCurrentGame();
		current = this;


		onInit();

		Bukkit.getPluginManager().registerEvents(this, getPlugin());

		for (Role.Builder roleBuilder : Role.getRegistered().values()) {
			Role role = roleBuilder.build(this);
			activeRoles.put(roleBuilder.getId(), role);
			role.start();
		}

		try {
			onStart();
		} catch (Exception e) {
			getPlugin().getLogger().severe("Error while starting game " + builder.getId() + ": " + e.getMessage());
			e.printStackTrace();
			stop();
		}
	}
	protected void onInit() { }
	protected void onStart() { }

	public final void stop() {
		if (! started) return;
		started = false;


		HandlerList.unregisterAll(this);

		for (Role role : activeRoles.values()) {
			role.stop();
		}
		activeRoles.clear();

		onStop();
	}
	protected void onStop() { }

	public abstract TeamController getTeamController();
	public abstract Boolean canPlayerHaveRole(Player player, String roleId);

	public static void registerGame(Builder builder) {
		registered.put(builder.getId(), builder);
	}

	public static void startGame(String id) {
		if (! registered.containsKey(id)) return;

		Game game = registered.get(id).build();
		game.start();
	}

	public static void stopCurrentGame() {
		if (current != null) {
			current.stop();
			current = null;
		}
	}

	public static abstract class Builder implements TabExecutor {
		public final Ludos plugin;
		public Ludos getPlugin() {
			return plugin;
		}

		public abstract String getId();

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

		public Builder(Ludos plugin) {
			this.plugin = plugin;
		}
	}
}