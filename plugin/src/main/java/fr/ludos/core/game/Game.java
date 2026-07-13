package fr.ludos.core.game;

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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import fr.ludos.core.Ludos;
import fr.ludos.core.book.BookUtility;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.group.Group;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.role.Role;
import fr.ludos.core.world.WorldManager;
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

	public abstract WorldManager getWorldManager();
	public abstract GameTeamController getTeamController();

	@Override
	public boolean isClear() {
		return super.isClear() && getWorldManager().isClear() && getTeamController().isClear();
	}

	protected Game(Builder builder, Group group, Scoreboard scoreboard) {
		this.builder = builder;
		this.group = group;
		this.scoreboard = group.getLudos().getServer().getScoreboardManager().getNewScoreboard();
	}

	public static boolean startGame(String id, Group group) {
		if (! registered.containsKey(id)) return false;

		Game oldGame = group.getGame();
		if (oldGame != null) {
			oldGame.stop();

			new BukkitRunnable() {
				@Override
				public void run() {
					if (! oldGame.isClear()) return;

					registered.get(id).build(group).setup();
					cancel();
				}
			}.runTaskTimer(oldGame.getPlugin(), 0, 20);
		}
		else {
			registered.get(id).build(group).setup();
		}

		return true;
	}

	private void onJoinGroup(OfflinePlayer player) {
		if (isStarted()) {
			getTeamController().addPlayer(player);
		}
	}
	private void onLeaveGroup(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			getWorldManager().evacuatePlayer(onlinePlayer);
		}

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

	private static final String SCHEDULE_END_GAME_TEXT = "Game finished, returning in %s seconds...";
	public final void scheduleEndGame(int seconds) {
		scheduleEndGame(seconds, null);
	}
	public void scheduleEndGame(int seconds, @Nullable String text) {
		Component message = Component.text(
			String.format(
				text != null ? text : SCHEDULE_END_GAME_TEXT,
				seconds
			)
		).color(NamedTextColor.YELLOW);
		for (Player player : getGroup().getOnlinePlayers()) {
			player.sendMessage(message);
		}
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			stop();
		}, seconds * 20);
	}


	@Override
	protected final void onSetup() {
		super.onSetup();
		Game oldGame = group.getGame();
		if (oldGame != null && oldGame != this) {
			oldGame.stop();
		}

		group.setGame(this);
		activeGames.add(this);

		getWorldManager().start();

		getGroup().addJoinGroupListener(this::onJoinGroup);
		getGroup().addLeaveGroupListener(this::onLeaveGroup);

		onGameSetup();
	}

	@Override
	protected final void onInit() {
		super.onInit();
		onGameInit();
	}
	@Override
	protected final void onStart() {
		super.onStart();

		getTeamController().start();

		activateRoles();

		onGameStart();
	}


	@Override
	protected final void onStop() {
		super.onStop();
		onGameStop();

		deactivateRoles();
		deactivateItems();

		getTeamController().stop();
	}

	@Override
	protected final void onDeinit() {
		super.onDeinit();
		onGameDeinit();
	}
	@Override
	protected final void onSetdown() {
		super.onSetdown();

		onGameSetdown();

		getGroup().removeJoinGroupListener(this::onJoinGroup);
		getGroup().removeLeaveGroupListener(this::onLeaveGroup);

		getWorldManager().stop();

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
								// .decorate(TextDecoration.BOLD)
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

		public abstract ConfigOptionsCollection getConfig();


		public abstract Game build(Group group);

		public Builder(Ludos plugin) {
			this.plugin = plugin;
		}
	}
}