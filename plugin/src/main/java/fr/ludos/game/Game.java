package fr.ludos.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.book.BookUtility;
import fr.ludos.game.areaController.GameAreaController;
import fr.ludos.game.lobbyController.GameLobbyController;
import fr.ludos.game.teamController.GameTeamController;
import fr.ludos.game.worldController.GameWorldController;
import fr.ludos.group.Group;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public abstract class Game extends TwoStepGameProcessBase {
	private static final Map<String, Builder> registered = new HashMap<>();
	public static Map<String, Builder> getRegistered() {
		return registered;
	}

	private static final Set<Game> activeGames = new HashSet<>();
	public static Set<Game> getActiveGames() {
		return Collections.unmodifiableSet(activeGames);
	}

	@Nullable
	public static Builder getGameById(String gameId) {
		return registered.getOrDefault(gameId, null);
	}

	public static List<String> getGameIds() {
		return registered.keySet().stream().collect( Collectors.toList() );
	}
	public static List<Builder> getGameBuilders() {
		return registered.values().stream().collect( Collectors.toList() );
	}

	public static void registerGame(Builder builder) {
		registered.put(builder.getId(), builder);
	}


	protected final Builder builder;
	public Builder getBuilder() {
		return builder;
	}
	@Override
	public JavaPlugin getPlugin() {
		return builder.getPlugin();
	}

	private final Group group;
	public Group getGroup() {
		return group;
	}

	private final Map<String, Role> activeRoles = new HashMap<>();
	public Map<String, Role> getActiveRoles() {
		return activeRoles;
	}

	private final Set<SpecialItem.Events<?>> activeItems = new HashSet<>();
	public Set<SpecialItem.Events<?>> getActiveItems() {
		return activeItems;
	}

	public abstract Scoreboard getScoreboard();
	public abstract GameWorldController getWorldController();
	public abstract GameAreaController getAreaController();
	public abstract GameTeamController getTeamController();
	public abstract GameLobbyController getLobbyController();

	protected Game(Builder builder, Group group) {
		this.builder = builder;
		this.group = group;
	}

	public static boolean startGame(String id, Group group) {
		if (! registered.containsKey(id)) return false;

		Game oldGame = group.getGame();
		if (oldGame != null) {
			oldGame.stop();
		}

		Game game;
		try {
			game = registered.get(id).build(group);
		} catch (Exception e) {
			Bukkit.getServer().broadcast(Component.text("Error while building game " + id + ": " + e.getMessage()).color(NamedTextColor.RED));
			e.printStackTrace();
			return false;
		}

		game.setup();
		return true;
	}

	private void onJoinGroup(OfflinePlayer player) {
		if (isStarted()) {
			getTeamController().addPlayer(player);
		}
	}
	private void onLeaveGroup(OfflinePlayer player) {
		if (isStarted()) {
			getTeamController().removePlayer(player);
		}
	}

	@Override
	protected final void onSetup() {
		Game oldGame = group.getGame();
		if (oldGame != null && oldGame != this) {
			oldGame.stop();
		}

		onGameSetup();

		getWorldController().setup();
		getAreaController().setup();
		getTeamController().setup();

		getLobbyController().start();

		group.setGame(this);
		activeGames.add(this);
	}

	@Override
	protected final void onInit() {
		onGameInit();
	}
	@Override
	protected final void onStart() {
		getTeamController().start();
		getAreaController().start();
		getWorldController().start();

		getLobbyController().stop();

		getGroup().addJoinGroupListener(this::onJoinGroup);
		getGroup().addLeaveGroupListener(this::onLeaveGroup);

		onGameStart();

		for (Role.Builder roleBuilder : Role.getRegistered().values()) {
			Role role = roleBuilder.build(this);
			activeRoles.put(roleBuilder.getId(), role);
			role.start();
		}
	}


	@Override
	protected final void onStop() {
		for (Role role : activeRoles.values()) {
			role.stop();
		}
		activeRoles.clear();

		onGameStop();

		getGroup().removeJoinGroupListener(this::onJoinGroup);
		getGroup().removeLeaveGroupListener(this::onLeaveGroup);

		getTeamController().stop();
		getAreaController().stop();
		getWorldController().stop();
	}
	@Override
	protected final void onDeinit() {
		onGameDeinit();
	}
	@Override
	protected final void onSetdown() {
		group.setGame(null);
		activeGames.remove(this);

		getLobbyController().stop();

		onGameSetdown();

		getTeamController().setdown();
		getAreaController().setdown();
		getWorldController().setdown();
	}

	protected void onGameSetup() { }
	protected void onGameInit() { }
	protected void onGameStart() { }

	protected void onGameStop() { }
	protected void onGameDeinit() { }
	protected void onGameSetdown() { }


	public LinkedHashMap<String, GameEvents> modifyEvents(LinkedHashMap<String, GameEvents> events) {
		return events;
	}

	public abstract Boolean canPlayerHaveRole(Player player, String roleId);



	public static abstract class Builder {
		public final Ludos plugin;
		public Ludos getPlugin() {
			return plugin;
		}

		public abstract String getId();

		public abstract TextComponent getDisplayName();
		public abstract TextComponent getDescription();

		public TextComponent[] buildPages() {
			return BookUtility.truncatePage(
				Component.text()
					.append(BookUtility.centerBookLine(getDisplayName()))
					.append(
						BookUtility.alignRightBookLine(
							Component.text("Start")
								.color(NamedTextColor.DARK_GREEN)
								.decorate(TextDecoration.BOLD)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(
									ClickEvent.runCommand(String.format("/ludos:ludos game start %s", getId()))
								)
						)
					)
					.append(Component.text("\n"))
					.append(getDescription())
				.build()
			);
		}

		public final ItemStack createGuidebook() {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMetaBuilder meta = ((BookMeta) book.getItemMeta()).toBuilder();

			meta.title(getDisplayName());
			meta.author(Component.text("Ludos"));

			for (TextComponent page : buildPages()) {
				meta.addPage(page);
			}

			populateGuidebook(meta);

			book.setItemMeta(meta.build());
			return book;
		}

		public void populateGuidebook(BookMetaBuilder builder) { }


		public abstract boolean executeGameConfig(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, ConfigurationSection config, @NotNull String[] args);
		public abstract List<String> gameConfigTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
		public abstract String getGameConfigUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label);


		public abstract Game build(Group group);

		public Builder(Ludos plugin) {
			this.plugin = plugin;
		}
	}
}