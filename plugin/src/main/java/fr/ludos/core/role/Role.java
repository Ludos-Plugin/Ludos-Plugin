package fr.ludos.core.role;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.Ludos;
import fr.ludos.core.book.BookUtility;
import fr.ludos.core.config.ConfigOptionsCollection;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.group.Group;
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
	public static final String NAMESPACE = "role";
	public static final String NONE_LABEL = "none";

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


	public static Map<UUID, String> getPlayerRoles() {
		return playerRoles;
	}
	private static Map<UUID, String> playerRoles = new HashMap<UUID, String>();


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
	public final Map<String, GameEvents> getGameEvents() {
		return Collections.unmodifiableMap(gameEvents);
	}

	/**
	 * Builds an instance of this Role, using the configuration inside the builder, and asigning itself to the given Game.
	 * @param builder The builder to use for configuration
	 * @param game The game used for scope
	 */
	public Role(Builder builder, Game game) {
		this.game = game;
		this.builder = builder;
		gameEvents = game.digestRoleEvents(builder.getId(), createGameEvents(builder, game));
	}

	@Override
	protected final void onInit() {
		super.onInit();

		onRoleInit();
	}
	@Override
	protected final void onStart() {
		super.onStart();

		for (GameEvents events : gameEvents.values()) {
			events.start();
		}

		onRoleStart();
	}

	protected void onRoleInit() { }
	protected void onRoleStart() { }


	@Override
	protected final void onDeinit() {
		super.onDeinit();

		onRoleDeinit();
	}
	@Override
	protected final void onStop() {
		super.onStop();

		for (GameEvents events : gameEvents.values()) {
			events.stop();
		}

		onRoleStop();
	}

	protected void onRoleDeinit() { }
	protected void onRoleStop() { }


	protected abstract LinkedHashMap<String, GameEvents> createGameEvents(Builder builder, Game game);

	public static void loadConfigRoles(Ludos ludos) {

		playerRoles = ludos.getPlayersConfig().getKeys(false).stream()
			.filter(Objects::nonNull)
			.map(s -> {
				OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(s));
				String roleId = ludos.getPlayerConfigSection(player).getString(NAMESPACE);
				return Pair.of(player, roleId);
			})
			.filter(p -> p.getKey() != null && p.getValue() != null)
			.collect(Collectors.toMap(
				(p) -> p.getKey().getUniqueId(),
				(p) -> {
					String roleId = p.getValue();
					ludos.getLogger().info("Loaded Role of Player : " + p.getKey() + " | Role ID : " + roleId);
					return roleId;
				}
			));
	}

	public static void registerRole(Builder constructor) {
		Role.registered.put(constructor.getId().toLowerCase(), constructor);
	}

	public static final boolean isAuthorizedToEditRole(ServerOperator operator, OfflinePlayer target, Ludos ludos) {
		if (operator.isOp() || operator == target) {
			return true;
		}

		if (operator instanceof Player player) {
			final Group group = Group.getGroupOfPlayer(player);
			if (group != null && group.isLeader(player) && group == Group.getGroupOfPlayer(target)) {
				return true;
			}
		}

		return false;
	}

	public static List<Player> getPlayersOfRole(String roleId) {
		return Role.getPlayerRoles().entrySet().stream()
			.filter(entry -> (entry.getValue().equals(roleId)))
			.map(entry -> Bukkit.getPlayer(entry.getKey()))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	@Nullable
	public static String getPlayerRoleId(OfflinePlayer player) {
		return playerRoles.getOrDefault(player.getUniqueId(), null);
	}

	@Nullable
	public static Builder getPlayerRole(OfflinePlayer player) {
		return registered.getOrDefault(getPlayerRoleId(player), null);
	}

	@Nullable
	public static Builder getRoleById(String roleId) {
		return registered.getOrDefault(roleId, null);
	}

	public static boolean isPlayerRole(OfflinePlayer player, String roleId) {
		String playerRoleId = getPlayerRoleId(player);
		if (roleId == playerRoleId) return true;
		else if (roleId == null || playerRoleId == null) return false;
		return roleId.equalsIgnoreCase(playerRoleId);
	}

	public static Predicate<OfflinePlayer> ofRole(String id) {
		return (OfflinePlayer p) -> isPlayerRole(p, id);
	}

	public static void setRole(OfflinePlayer player, String roleId, Ludos ludos) {
		UUID playerUUID = player.getUniqueId();
		if ( playerRoles.containsKey(playerUUID) && playerRoles.get(playerUUID).equalsIgnoreCase(roleId) ) return;

		Role.Builder role = getRegistered().get(roleId);
		if (role == null) return;

		playerRoles.put(playerUUID, roleId);

		ludos.getPlayerConfigSection(player).set(NAMESPACE, roleId);
		ludos.savePlayersConfig();

		Player online = player.getPlayer();
		if (online != null) {
			online.sendMessage("Your role is now " + roleId);
		}
	}

	public static void removeRole(OfflinePlayer player, Ludos ludos) {
		UUID playerUUID = player.getUniqueId();
		if ( ! playerRoles.containsKey(playerUUID) ) return;

		Role.Builder role = getRegistered().get(playerRoles.get(playerUUID));
		if (role == null) return;

		playerRoles.remove(playerUUID);
		ludos.getPlayerConfigSection(player).set(NAMESPACE, null);
		ludos.savePlayersConfig();

		Player online = player.getPlayer();
		if (online != null) {
			online.sendMessage("You now have no role");
		}
	}


	public final Boolean isPlayerValid(OfflinePlayer player) {
		if (! game.getGroup().isPlayer(player)) return false;
		return isPlayerValidInternal(player);
	}
	protected Boolean isPlayerValidInternal(OfflinePlayer player) {
		return true;
	}

	/**
	 * The Builder class is used to configure a Role before it is initialized and serves as the data for the Role.
	 * It contains configuration for the Role itself.
	 */
	public static abstract class Builder {
		private final Ludos ludos;
		public final Ludos getLudos() { return ludos; }

		private final JavaPlugin plugin;
		public final JavaPlugin getPlugin() { return plugin; }

		public abstract String getId();
		public EnumSet<RoleFlag> getRoleFlags() {
			return EnumSet.noneOf(RoleFlag.class);
		}

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
								// .decorate(TextDecoration.BOLD)
								.decorate(TextDecoration.UNDERLINED)
								.clickEvent(
									ClickEvent.runCommand("/ludos:ludos role reset")
								),
							Component.text("Pick")
								.color(NamedTextColor.DARK_GREEN)
								// .decorate(TextDecoration.BOLD)
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


		public ConfigOptionsCollection getConfig() {
			return null;
		}


		public Builder(Ludos ludos, JavaPlugin plugin) {
			this.ludos = ludos;
			this.plugin = plugin;
		}
		public Builder(Ludos ludos) {
			this(ludos, ludos);
		}

		public abstract Role build(Game game);
	}
}