package fr.ludos.core.role;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.Ludos;
import fr.ludos.core.book.BookUtility;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameEvents;
import fr.ludos.core.game.GameProcessBase;
import fr.ludos.core.gui.ConfigHolder;
import fr.ludos.core.gui.GuiObject;
import fr.ludos.core.gui.GuidebookProvider;
import fr.ludos.core.persistence.config.ConfigNode;
import fr.ludos.core.persistence.config.ConfigNodeCollection;
import fr.ludos.core.persistence.config.ConfigNodeMap;
import fr.ludos.core.persistence.config.scope.ScopeConfigMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


/**
 * The Role class contains runtime data for the Role itself as well as the events for the Role-users
 * It contains events and State.
 */
public abstract class Role extends GameProcessBase {
	public static final String NAMESPACE = "role";
	public static final TextComponent WINDOW_TITLE = Component.text("Role Configuration");
	public static final String NONE_LABEL = "none";

	private final Game game;
	public Game getGame() {
		return game;
	}

	private final Builder builder;
	public Builder getBuilder() {
		return builder;
	}

	public Ludos getLudos() {
		return game.ludos();
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

	public static ScopeConfigMap scopeConfig(Ludos ludos, ConfigNodeCollection nodes) {
		return new ScopeConfigMap(
			WINDOW_TITLE,
			ludos,
			nodes
		);
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
	public static abstract class Builder implements GuiObject, ConfigHolder, GuidebookProvider {
		private final RoleManager manager;
		public final RoleManager getManager() {
			return manager;
		}

		public final Ludos getLudos() {
			return manager.getLudos();
		}

		private final JavaPlugin plugin;
		public final JavaPlugin getPlugin() { return plugin; }

		public abstract String getId();
		public EnumSet<RoleFlag> getRoleFlags() {
			return EnumSet.noneOf(RoleFlag.class);
		}

		public abstract TextComponent getDescription();

		public TextComponent[] buildPages() {
			return BookUtility.truncatePage(
				Component.text()
					.append(BookUtility.centerBookLine(normalizedDisplayName()))
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


		public Builder(RoleManager manager, JavaPlugin plugin) {
			this.manager = manager;
			this.plugin = plugin;
		}

		public abstract Role build(Game game);
	}
}