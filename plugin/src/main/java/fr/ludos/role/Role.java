package fr.ludos.role;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.Ludos;
import fr.ludos.book.BookUtility;
import fr.ludos.game.Game;
import fr.ludos.game.GameEvents;
import fr.ludos.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


/**
 * The Role class contains runtime data for the Role itself as well as the events for the Role-users
 * It contains events and Data.
 */
public abstract class Role extends GameProcessBase {
	private static final String rolesKey = "playerRoles";

	public static Map<String, Builder> getRegistered() {
		return registered;
	}
	private static Map<String, Builder> registered = new HashMap<String, Builder>();


	public static List<String> getRoleIds() {
		return registered.keySet().stream().collect( Collectors.toList() );
	}
	public static List<Builder> getRoleBuilders() {
		return registered.values().stream().collect( Collectors.toList() );
	}


	public static Map<String, String> getPlayerRoles() {
		return playerRoles;
	}
	private static Map<String, String> playerRoles = new HashMap<String, String>();


	private final Game game;
	public Game getGame() {
		return game;
	}

	private final Builder builder;
	public Builder getBuilder() {
		return builder;
	}

	public JavaPlugin getPlugin() {
		return game.getPlugin();
	}

	private final Map<String, GameEvents> gameEvents;

	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public Role(Builder builder, Game game) {
		this.game = game;
		this.builder = builder;
		gameEvents = game.modifyEvents(createGameEvents(builder, game));
	}

	protected final void onInit() {
		onRoleInit();
	}
	@Override
	protected final void onStart() {
		for (GameEvents events : gameEvents.values()) {
			events.start();
		}

		onRoleStart();
	}

	protected void onRoleInit() { }
	protected void onRoleStart() { }


	protected final void onDeinit() {
		onRoleDeinit();
	}
	@Override
	protected final void onStop() {
		for (GameEvents events : gameEvents.values()) {
			events.stop();
		}

		onRoleStop();
	}

	protected void onRoleDeinit() { }
	protected void onRoleStop() { }


	protected abstract LinkedHashMap<String, GameEvents> createGameEvents(Builder builder, Game game);

	public static void loadConfigRoles(Ludos plugin) {
		ConfigurationSection rolesSection = plugin.getConfig().getConfigurationSection(rolesKey);
		if (rolesSection != null) {
			playerRoles = rolesSection.getKeys(false).stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap((s) -> s, (s) -> plugin.getConfig().getString(rolesKey + '.' + s)));
		}
	}

	public static void registerRole(Builder constructor) {
		Role.registered.put(constructor.getId().toLowerCase(), constructor);
	}

	public static List<Player> getPlayersOfRole(String roleId) {
		return Role.getPlayerRoles().entrySet().stream()
			.filter(entry -> (entry.getValue().equals(roleId)))
			.map(entry -> Bukkit.getPlayerExact(entry.getKey()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	public static Builder getRole(HumanEntity player) {
		return registered.getOrDefault(playerRoles.getOrDefault(player.getName(), ""), null);
	}

	@Nullable
	public static Builder getRoleById(String roleId) {
		return registered.getOrDefault(roleId, null);
	}

	public static boolean isPlayerRole(HumanEntity player, String role) {
		Builder currentRole = getRole(player);
		return (currentRole != null && currentRole.getId().equals(role));
	}

	public static void setRole(HumanEntity player, String roleId) {
		if ( playerRoles.containsKey(player.getName()) && playerRoles.get(player.getName()).equalsIgnoreCase(roleId) ) return;

		Role.Builder role = getRegistered().get(roleId);
		if (role == null) return;

		playerRoles.put(player.getName(), roleId);

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);

		plugin.getConfig().set(rolesKey + '.' + player.getName(), roleId);
		plugin.saveConfig();

	}

	public static void removeRole(Player player) {
		if ( ! playerRoles.containsKey(player.getName()) ) return;

		Role.Builder role = getRegistered().get(playerRoles.get(player.getName()));
		if (role == null) return;

		playerRoles.remove(player.getName());
		player.sendMessage("You now have no role");

		Ludos plugin = JavaPlugin.getPlugin(Ludos.class);

		plugin.getConfig().set(rolesKey + '.' + player.getName(), null);
		plugin.saveConfig();
	}


	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public static abstract class Builder {
		private final JavaPlugin plugin;
		public final JavaPlugin getPlugin() { return plugin; }

		public abstract String getId();

		public abstract TextComponent getDisplayName();
		public abstract TextComponent getDescription();

		public TextComponent[] buildPages() {
			return BookUtility.truncatePage(
				Component.text()
					.append(BookUtility.centerBookLine(getDisplayName()))
					.append(
						BookUtility.spaceBookLine(
							Component.text("Reset")
								.color(NamedTextColor.DARK_RED)
								.decorate(TextDecoration.BOLD)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(
									ClickEvent.runCommand("/ludos:ludos role reset")
								),
							Component.text("Pick")
								.color(NamedTextColor.DARK_GREEN)
								.decorate(TextDecoration.BOLD)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(
									ClickEvent.runCommand(String.format("/ludos:ludos role set %s", getId()))
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


		public boolean executeRoleConfig(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) { return false; }
		public List<String> roleConfigTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) { return null; }
		public String getRoleConfigUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) { return null; }


		public Builder(JavaPlugin plugin) {
			this.plugin = plugin;
		}

		public abstract Role build(Game game);
	}
}