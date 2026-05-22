package fr.ludos.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import fr.ludos.Utility;
import fr.ludos.book.BookUtility;
import fr.ludos.command.ConfigSubcommandManager;
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
	public static final String namespace = "game";
	public final Random random = new Random();

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

	private Scoreboard scoreboard;
	public final Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	public abstract GameWorldController getWorldController();
	public abstract GameTeamController getTeamController();

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

	public void activateRoles() {
		for (Role.Builder roleBuilder : Role.getRegistered().values()) {
			String id = roleBuilder.getId();
			if (activeRoles.containsKey(id)) {
				Bukkit.broadcast(
					Component.text("Error: Skipped startup of role " + id + " because of id deduplication.")
						.color(NamedTextColor.RED)
				);
				continue;
			}

			Role role = roleBuilder.build(this);
			activeRoles.put(id, role);
			role.start();
		}
	}
	public void deactivateRoles() {
		for (Role role : activeRoles.values()) {
			role.stop();
		}
		activeRoles.clear();
	}
	public void deactivateItems() {
		for (SpecialItem.Events<?> itemEvents : activeItems) {
			itemEvents.stop();
		}
		activeItems.clear();
	}


	@Override
	protected final void onSetup() {
		Game oldGame = group.getGame();
		if (oldGame != null && oldGame != this) {
			oldGame.stop();
		}

		this.scoreboard = Bukkit.getServer().getScoreboardManager().getNewScoreboard();

		group.setGame(this);
		activeGames.add(this);

		getWorldController().setup();
		getTeamController().setup();

		onGameSetup();
	}

	@Override
	protected final void onInit() {
		onGameInit();
	}
	@Override
	protected final void onStart() {
		getWorldController().start();
		getTeamController().start();

		getGroup().addJoinGroupListener(this::onJoinGroup);
		getGroup().addLeaveGroupListener(this::onLeaveGroup);

		activateRoles();

		onGameStart();
	}


	@Override
	protected final void onStop() {
		onGameStop();

		deactivateRoles();
		deactivateItems();

		for (Player player : getTeamController().getOnlinePlayers()) {
			Utility.resetPlayer(player);
			player.setGameMode(GameMode.SURVIVAL);
		}

		getGroup().removeJoinGroupListener(this::onJoinGroup);
		getGroup().removeLeaveGroupListener(this::onLeaveGroup);

		getTeamController().stop();
		getWorldController().stop();
	}

	@Override
	protected final void onDeinit() {
		onGameDeinit();
	}
	@Override
	protected final void onSetdown() {
		onGameSetdown();

		getTeamController().setdown();
		getWorldController().setdown();

		scoreboard = null;

		group.setGame(null);
		activeGames.remove(this);
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

		protected abstract ConfigSubcommandManager<?> getConfigsSubcommand();

		public boolean executeGameConfig(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, ConfigurationSection config, @NotNull String[] args) {
			return getConfigsSubcommand().onCommand(sender, command, label, config, args);
		}
		public List<String> gameConfigTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
			return getConfigsSubcommand().onTabComplete(sender, command, label, args);
		}
		public String getGameConfigUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
			return getConfigsSubcommand().getUsage();
		}


		public abstract Game build(Group group);

		public Builder(Ludos plugin) {
			this.plugin = plugin;
		}
	}
}