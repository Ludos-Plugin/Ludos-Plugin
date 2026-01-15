package fr.ludos.game;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.book.BookUtility;
import fr.ludos.game.alien.AlienTeamController;
import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public abstract class Game extends GameProcessBase {
	@Nullable
	private static Game current = null;

	@Nullable
	public static Game getCurrent() {
		return current;
	}

	private static final Map<String, Builder> registered = new HashMap<>();

	public static Map<String, Builder> getRegistered() {
		return registered;
	}

	@Nullable
	public static Builder getGameById(String gameId) {
		return registered.getOrDefault(gameId, null);
	}

	public static List<String> getGameIds() {
		return registered.keySet().stream().collect(Collectors.toList());
	}

	public static List<Builder> getGameBuilders() {
		return registered.values().stream().collect(Collectors.toList());
	}

	public static void registerGame(Builder builder) {
		registered.put(builder.getId(), builder);
	}

	private final Map<String, Role> activeRoles = new HashMap<>();

	public Map<String, Role> getActiveRoles() {
		return activeRoles;
	}

	protected final Builder builder;

	public Builder getBuilder() {
		return builder;
	}

	@Override
	public JavaPlugin getPlugin() {
		return builder.getPlugin();
	}

	public abstract Scoreboard getScoreboard();

	public abstract GameTeamController getGameTeamController();

	public abstract GameAreaController getGameAreaController();

	public static boolean startGame(String id) {
		if (!registered.containsKey(id))
			return false;

		Game game;
		try {
			game = registered.get(id).build();
		} catch (Exception e) {
			Bukkit.getServer().broadcast(Component.text("Error while starting game " + id + ": " + e.getMessage())
					.color(NamedTextColor.RED));
			e.printStackTrace();
			return false;
		}

		game.start();
		return true;
	}

	public static void stopCurrentGame() {
		if (current != null) {
			current.stop();
		}
	}

	public Game(Builder builder) {
		this.builder = builder;
	}

	@Override
	protected final void onInit() {
		stopCurrentGame();
		current = this;

		onGameInit();
	}

	@Override
	protected final void onStart() {
		getGameAreaController().start();
		getGameTeamController().start();

		onGameStart();

		for (Role.Builder roleBuilder : Role.getRegistered().values()) {
			Role role = roleBuilder.build(this);
			activeRoles.put(roleBuilder.getId(), role);
			role.start();
		}
	}

	protected void onGameInit() {
	}

	protected void onGameStart() {
	}

	@Override
	protected final void onDeinit() {
		current = null;

		onGameDeinit();
	}

	@Override
	protected final void onStop() {
		getGameAreaController().stop();
		getGameTeamController().stop();

		onGameStop();

		for (Role role : activeRoles.values()) {
			role.stop();
		}
		activeRoles.clear();
	}

	protected void onGameDeinit() {
	}

	protected void onGameStop() {
	}

	public LinkedHashMap<String, SpecialItem.Events<?>> modifyEvents(
			LinkedHashMap<String, SpecialItem.Events<?>> events) {
		return events;
	}

	public abstract Boolean canPlayerHaveRole(Player player, String roleId);

	public static abstract class Builder {
		public final JavaPlugin plugin;

		public JavaPlugin getPlugin() {
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
															ClickEvent.runCommand(String
																	.format("/ludos:ludos game start %s", getId())))))
							.append(Component.text("\n"))
							.append(getDescription())
							.build());
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

		public void populateGuidebook(BookMetaBuilder builder) {
		}

		public abstract boolean executeGameConfig(@NotNull CommandSender sender, @NotNull Command command,
				@NotNull String label, @NotNull String[] args);

		public abstract List<String> gameConfigTabComplete(@NotNull CommandSender sender, @NotNull Command command,
				@NotNull String label, @NotNull String[] args);

		public abstract String getGameConfigUsage(@NotNull CommandSender sender, @NotNull Command command,
				@NotNull String label);

		public abstract Game build();

		public Builder(JavaPlugin plugin) {
			this.plugin = plugin;
		}
	}

	public AlienTeamController getTeamController() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getTeamController'");
	}
}