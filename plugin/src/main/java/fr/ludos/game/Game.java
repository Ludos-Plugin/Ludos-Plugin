package fr.ludos.game;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.jetbrains.annotations.NotNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.ludos.Ludos;
import fr.ludos.role.Role;


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

	public static List<String> getGameIds() {
		return registered.keySet().stream().collect( Collectors.toList() );
	}
	public static List<Builder> getGameBuilders() {
		return registered.values().stream().collect( Collectors.toList() );
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

	public static abstract class Builder {
		public final Ludos plugin;
		public Ludos getPlugin() {
			return plugin;
		}

		public abstract String getId();

		public abstract TextComponent getDisplayName();
		public abstract TextComponent getDescription();

		public final ItemStack createGuidebook() {
			ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
			BookMetaBuilder meta = ((BookMeta) book.getItemMeta()).toBuilder();

			meta.title(getDisplayName());
			meta.author(Component.text("Ludos"));

			TextComponent page =
				getDisplayName().append(Component.text("\n\n"))
				.append(getDescription().append(Component.text("\n\n")))
				.append(
					Component.text("Start")
					.color(NamedTextColor.DARK_GREEN)
					.decorate(TextDecoration.BOLD)
					.clickEvent(
						ClickEvent.runCommand(String.format("/ludos:game %s start", getId()))
					)
				);
			meta.addPage(page);

			populateGuidebook(meta);

			book.setItemMeta(meta.build());
			return book;
		}

		public void populateGuidebook(BookMetaBuilder builder) { }


		public abstract boolean executeGameConfig(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
		public abstract List<String> gameConfigTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
		public abstract String getGameConfigUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label);


		public abstract Game build();

		public Builder(Ludos plugin) {
			this.plugin = plugin;
		}
	}
}