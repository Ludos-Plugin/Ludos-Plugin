package fr.ludos.core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.book.BookUtility;
import fr.ludos.core.game.teamController.GameTeamController;
import fr.ludos.core.group.Group;
import fr.ludos.core.gui.ConfigHolder;
import fr.ludos.core.gui.GuiObject;
import fr.ludos.core.gui.GuidebookProvider;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.scope.ScopeConfigMap;
import fr.ludos.core.role.Role;
import fr.ludos.core.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import xyz.xenondevs.invui.item.builder.AbstractItemBuilder;
import xyz.xenondevs.invui.item.builder.ItemBuilder;

/**
 * An encapsulation of a Game instance's logic within a Ludos environment.
 */
public abstract class Game extends TwoStepGameProcessBase {
	public static final String NAMESPACE = "game";
	public static final TextComponent CONFIG_WINDOW_TITLE = Component.text("Game configuration");

	public final static ItemBuilder createItem() {
		return new ItemBuilder(Material.MUSIC_DISC_CHIRP);
	}
	public final static GuiObject GUI_OBJECT = new GuiObject() {
		@Override
		public @NotNull AbstractItemBuilder<?> createItem(Player player) {
			return Game.createItem();
		}
		@Override
		public TextComponent displayName() {
			return Component.text("Game")
				.color(NamedTextColor.RED)
				.decoration(TextDecoration.ITALIC, false);
		}
	};

	private static final String SCHEDULE_END_GAME_TEXT = "Game finished, returning in %s seconds...";

	protected final Builder builder;
	private final Group group;
	private final Map<String, Role> activeRoles = new HashMap<>();
	private final Set<SpecialItem.Events<?>> activeItems = new HashSet<>();

	private final Map<OfflinePlayer, PreGameInfo> preGameInfo = new HashMap<>();

	private final Random random = new Random();
	private Scoreboard scoreboard;

	private final List<Runnable> setupListeners = new ArrayList<>();
	public void addSetupListener(Runnable listener) {
		setupListeners.add(listener);
	}
	public void removeSetupListener(Runnable listener) {
		setupListeners.remove(listener);
	}
	private void notifySetup() {
		for (Runnable listener : setupListeners) {
			listener.run();
		}
	}

	private final List<Runnable> teardownListeners = new ArrayList<>();
	public void addTeardownListener(Runnable listener) {
		teardownListeners.add(listener);
	}
	public void removeTeardownListener(Runnable listener) {
		teardownListeners.remove(listener);
	}
	private void notifyTeardown() {
		for (Runnable listener : teardownListeners) {
			listener.run();
		}
	}

	protected Game(Builder builder, Group group, Scoreboard scoreboard) {
		this.builder = builder;
		this.group = group;
		this.scoreboard = builder.getLudos().getServer().getScoreboardManager().getNewScoreboard();
	}

	public static ScopeConfigMap scopeConfig(Ludos ludos, ConfigNodeCollection nodes) {
		return new ScopeConfigMap(
			CONFIG_WINDOW_TITLE,
			ludos,
			nodes,
			nodes,
			null
		);
	}

	public Map<String, Role> getActiveRoles() {
		return activeRoles;
	}

	public Set<SpecialItem.Events<?>> getActiveItems() {
		return activeItems;
	}

	public Builder builder() {
		return builder;
	}
	public Ludos ludos() {
		return builder.getLudos();
	}
	@Override
	public JavaPlugin plugin() {
		return ludos();
	}

	public Group group() {
		return group;
	}

	public final Random random() {
		return random;
	}
	public final Scoreboard scoreboard() {
		return scoreboard;
	}

	public abstract WorldManager worldManager();
	public abstract GameTeamController teamController();

	@Override
	public boolean isClear() {
		return super.isClear() && worldManager().isClear() && teamController().isClear();
	}

	private void onJoinGroup(OfflinePlayer player) {
		joinGame(player);
	}
	private void onLeaveGroup(OfflinePlayer player) {
		leaveGame(player);
	}

	public void joinGame(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		addPlayer(onlinePlayer);

		if (isStarted()) {
			teamController().addPlayer(player);
		}
	}

	public void leaveGame(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer != null) {
			returnPlayer(player);
		}

		if (isStarted()) {
			teamController().removePlayer(player);
		}
	}

	public void addPlayer(Player player) {
		preGameInfo.put(player, new PreGameInfo(player));
	}

	public void returnPlayer(OfflinePlayer player) {
		Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) return;

		PreGameInfo info = preGameInfo.remove(onlinePlayer);
		if (info != null) {
			info.apply();
		}
	}
	public void returnAllPlayers() {
		for (PreGameInfo info : preGameInfo.values()) {
			info.apply();
		}
		preGameInfo.clear();
	}

	public void activateRoles() {
		for (Role.Builder roleBuilder : builder.getLudos().roleManager().getRegistered().values()) {
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
		group().sendMessage(message);
		Bukkit.getScheduler().runTaskLater(plugin(), () -> {
			stop();
		}, seconds * 20);
	}


	@Override
	protected final void onSetup() {
		super.onSetup();
		Game oldGame = group.game();
		if (oldGame != null && oldGame != this) {
			oldGame.stop();
		}
		for (Player player : group().getOnlinePlayers()) {
			addPlayer(player);
		}

		notifySetup();

		worldManager().start();

		group().addJoinGroupListener(this::onJoinGroup);
		group().addLeaveGroupListener(this::onLeaveGroup);

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

		teamController().start();

		activateRoles();

		onGameStart();
	}


	@Override
	protected final void onStop() {
		super.onStop();
		onGameStop();

		deactivateRoles();
		deactivateItems();

		teamController().stop();
	}

	@Override
	protected final void onDeinit() {
		super.onDeinit();
		onGameDeinit();
	}
	@Override
	protected final void onTeardown() {
		super.onTeardown();

		onGameSetdown();

		group().removeJoinGroupListener(this::onJoinGroup);
		group().removeLeaveGroupListener(this::onLeaveGroup);

		returnAllPlayers();
		worldManager().stop();

		scoreboard = null;

		notifyTeardown();
	}

	protected void onGameSetup() { }
	protected void onGameInit() { }
	protected void onGameStart() { }

	protected void onGameStop() { }
	protected void onGameDeinit() { }
	protected void onGameSetdown() { }


	public LinkedHashMap<String, GameEvents> digestRoleEvents(String roleId, LinkedHashMap<String, GameEvents> events) {
		return events;
	}

	/**
	 * A simple Factory for a {@link Game}. Useful for registering Games in {@link Ludos}.
	 */
	public static abstract class Builder implements GuiObject, ConfigHolder, GuidebookProvider {
		private final GameManager manager;


		public Builder(GameManager manager) {
			this.manager = manager;
		}


		public GameManager getManager() {
			return manager;
		}

		public final Ludos getLudos() {
			return manager.getLudos();
		}

		public abstract String getId();

		public abstract TextComponent getDescription();

		public TextComponent[] buildPages() {
			return BookUtility.truncatePage(
				Component.text()
					.append(BookUtility.centerBookLine(normalizedDisplayName()))
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

			meta.title(normalizedDisplayName());
			meta.author(Component.text("Ludos"));

			for (TextComponent page : buildPages()) {
				meta.addPage(page);
			}

			populateGuidebook(meta);

			book.setItemMeta(meta.build());
			return book;
		}

		public void populateGuidebook(BookMetaBuilder builder) { }

		public final ConfigNodeMap getConfigMap(List<ConfigNode> configNodes) {
			return getConfigMap(getId(), this, configNodes);
		}
		@Override
		public ConfigNodeCollection getConfig() {
			return null;
		}


		public abstract Game build(Group group);
	}
}