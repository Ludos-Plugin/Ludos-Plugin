package fr.ludos.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.book.BookUtility;
import fr.ludos.core.command.ludos.LudosCommand;
import fr.ludos.core.command.ludos.config.GlobalScopedConfigMap;
import fr.ludos.core.command.ludos.config.GroupScopedConfigMap;
import fr.ludos.core.command.ludos.config.PlayerScopedConfigMap;
import fr.ludos.core.command.ludos.config.ludos.LudosConfigMap;
import fr.ludos.core.command.ludos.config.player.PlayerConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.GroupManager;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.texture.TextureListener;
import fr.ludos.core.item.texture.TextureManager;
import fr.ludos.core.packets.player.PlayerPackets;
import fr.ludos.core.packets.player.PlayerPacketsFactory;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;
import fr.ludos.games.arena.ArenaGame;
import fr.ludos.games.manhunt.ManhuntGame;
import fr.ludos.games.raid.RaidGame;
import fr.ludos.other.ExcludeFromJacocoGeneratedReport;
import fr.ludos.roles.assassin.AssassinRole;
import fr.ludos.roles.berserker.BerserkerRole;
import fr.ludos.roles.harvester.HarvesterRole;
import fr.ludos.roles.huntsman.HuntsmanRole;
import fr.ludos.roles.rampart.RampartRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * The main Ludos plugin.
 * Contains Configuration file utility functions.
 */
public class Ludos extends JavaPlugin implements Listener {
	private final File playersFile = new File(getDataFolder(), "players.yml");
	private final FileConfiguration playersData = YamlConfiguration.loadConfiguration(playersFile);

	public static final String NAMESPACE = "ludos";
	public static final String GLOBAL_KEY = "global";
	public static final String PLAYER_NAMESPACE = "player";
	public static final String CONFIG_NAMESPACE = "config";
	public static final String DATA_NAMESPACE = "data";

	private final GroupManager groupManager = new GroupManager(this);
	private final GameManager gameManager = new GameManager(this);
	private final RoleManager roleManager = new RoleManager(this);

	private final TextureManager textureManager = new TextureManager(this);
	private final TextureListener textureListener = new TextureListener(this);
	public final PlayerPackets playerPackets = PlayerPacketsFactory.createHandler();

	public final PlayerScopedConfigMap playerConfigMap = new PlayerScopedConfigMap(this);
	public final GroupScopedConfigMap groupConfigMap = new GroupScopedConfigMap(this);
	public final GlobalScopedConfigMap globalConfigMap = new GlobalScopedConfigMap(this);

	public final GroupManager getGroupManager() {
		return this.groupManager;
	}
	public final GameManager getGameManager() {
		return this.gameManager;
	}
	public final RoleManager getRoleManager() {
		return this.roleManager;
	}

	public ConfigurationSection getPluginConfig() {
		return Utility.getOrCreateConfigSection(getConfig(), LudosConfigMap.INSTANCE.namespace());
	}
	public ConfigurationSection getGlobalRoleConfig(Role.Builder role) {
		return Utility.getOrCreateConfigSection(getConfig(), roleManager.configMap.namespace() + "." + role.getId());
	}
	public ConfigurationSection getGlobalPlayerConfig() {
		return Utility.getOrCreateConfigSection(getConfig(), PlayerConfigMap.INSTANCE.namespace());
	}

	public final FileConfiguration getPlayersConfig() {
		return playersData;
	}
	public ConfigurationSection getPlayerConfigSection(OfflinePlayer player) {
		return Utility.getOrCreateConfigSection(playersData, player.getUniqueId().toString());
	}

	public ConfigurationSection getPlayerScopedConfig(OfflinePlayer player) {
		return Utility.getOrCreateConfigSection(getPlayerConfigSection(player), CONFIG_NAMESPACE);
	}
	public ConfigurationSection getPlayerScopedConfig(OfflinePlayer player, String path) {
		return Utility.getOrCreateConfigSection(getPlayerConfigSection(player), CONFIG_NAMESPACE + "." + path);
	}
	public ConfigurationSection getPlayerRoleConfig(OfflinePlayer player, Role.Builder role) {
		return getPlayerScopedConfig(player, Role.NAMESPACE + "." + role.getId());
	}
	public ConfigurationSection getPlayerConfig(OfflinePlayer player) {
		return getPlayerScopedConfig(player, PLAYER_NAMESPACE);
	}

	public ConfigurationSection getPlayerScopedData(OfflinePlayer player) {
		return Utility.getOrCreateConfigSection(getPlayerConfigSection(player), DATA_NAMESPACE);
	}
	public ConfigurationSection getPlayerScopedData(OfflinePlayer player, String path) {
		return Utility.getOrCreateConfigSection(getPlayerConfigSection(player), DATA_NAMESPACE + "." + path);
	}
	public ConfigurationSection getGameData(OfflinePlayer player, Game.Builder game) {
		return getPlayerScopedData(player, Game.NAMESPACE + "." + game.getId());
	}
	public ConfigurationSection getRoleData(OfflinePlayer player, Role.Builder role) {
		return getPlayerScopedData(player, Role.NAMESPACE + "." + role.getId());
	}
	public ConfigurationSection getItemData(OfflinePlayer player, SpecialItem.Events<?> game) {
		return getPlayerScopedData(player, SpecialItem.NAMESPACE + "." + game.getTypeId());
	}

	public void savePlayersConfig() {
		try {
			playersData.save(playersFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save players to " + playersFile, ex);
		}
	}

	public TextureManager getTextureManager() {
		return textureManager;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		roleManager.loadConfigRoles();
		groupManager.loadConfigGroups();

		gameManager.registerGame(new ManhuntGame.Builder(gameManager));
		gameManager.registerGame(new ArenaGame.Builder(gameManager));
		gameManager.registerGame(new RaidGame.Builder(gameManager));

		roleManager.registerRole(new HuntsmanRole.Builder(this));
		roleManager.registerRole(new HarvesterRole.Builder(this));
		roleManager.registerRole(new RampartRole.Builder(this));
		roleManager.registerRole(new AssassinRole.Builder(this));
		roleManager.registerRole(new BerserkerRole.Builder(this));

		String commandLabel = NAMESPACE;
		PluginCommand cmd = getCommand(commandLabel);
		LudosCommand ludosCommand = new LudosCommand(this);
		cmd.setExecutor(ludosCommand);
		cmd.setTabCompleter(ludosCommand);
		cmd.setUsage(ludosCommand.getUsage(null));

		PluginCommand textureCmd = getCommand("texture");
		if (textureCmd != null) {
			textureCmd.setExecutor(textureManager);
		}

		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(groupManager, this);
		pluginManager.registerEvents(textureListener, this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		for (Game game : gameManager.getActiveGames()) {
			game.stop();
		}
		HandlerList.unregisterAll((Plugin) this);
	}

	@ExcludeFromJacocoGeneratedReport // Fully tested, but coverage abruptly ends BookMetaBuilder builder = meta.toBuilder();
	public ItemStack createGuidebook() {
		final int gameHeaderPageIdx = 2;

		final List<Game.Builder> gameBuilders = gameManager.getGameBuilders();
		final int gameHeaderPageCount = ((2 + gameBuilders.size() * 2) + BookUtility.MC_BOOK_LINE_COUNT - 1) / BookUtility.MC_BOOK_LINE_COUNT;

		int gamePageOffset = 0;

		ArrayList<TextComponent> gamePages = new ArrayList<>();

		TextComponent.Builder gameHeaderPageBuilder = Component.text()
			.append(
				BookUtility.centerBookLine(
					Component.text("Games")
						.color(NamedTextColor.BLUE)
						.decoration(TextDecoration.BOLD, true)
				)
			)
			.append(Component.text("\n"));
		for (Game.Builder builder : gameBuilders) {
			final int gamePageIdx = gameHeaderPageIdx + gameHeaderPageCount + gamePageOffset;
			gameHeaderPageBuilder
				.append(
					BookUtility.spaceBookLine(
						builder.getDisplayName().append(Component.text(" :")),
						Component.text("Page " + gamePageIdx)
							.decoration(TextDecoration.UNDERLINED, true)
							.clickEvent(ClickEvent.changePage(gamePageIdx))
					)
				);
			final TextComponent[] pages = builder.buildPages();
			for (TextComponent textComponent : pages) {
				gamePages.add(textComponent);
				gamePageOffset++;
			}
		}
		TextComponent[] gameHeaderPages = BookUtility.truncatePage(gameHeaderPageBuilder.build());



		final int roleHeaderPageIdx = gameHeaderPageIdx + gameHeaderPageCount + gamePageOffset;

		final List<Role.Builder> roleBuilders = roleManager.getBuilders();
		final int roleHeaderPageCount = ((2 + roleBuilders.size() * 2) + BookUtility.MC_BOOK_LINE_COUNT - 1) / BookUtility.MC_BOOK_LINE_COUNT;

		int rolePageOffset = 0;

		ArrayList<TextComponent> rolePages = new ArrayList<>();

		TextComponent.Builder roleHeaderPageBuilder = Component.text()
			.append(
				BookUtility.centerBookLine(
					Component.text("Roles")
						.color(NamedTextColor.RED)
						.decoration(TextDecoration.BOLD, true)
				)
			)
			.append(Component.text("\n"));
		for (Role.Builder builder : roleBuilders) {
			final int rolePageIdx = roleHeaderPageIdx + roleHeaderPageCount + rolePageOffset;
			roleHeaderPageBuilder
				.append(
					BookUtility.spaceBookLine(
						builder.getDisplayName().append(Component.text(" :")),
						Component.text("Page " + rolePageIdx)
							.decoration(TextDecoration.UNDERLINED, true)
							.clickEvent(ClickEvent.changePage(rolePageIdx))
					)
				);
			final TextComponent[] pages = builder.buildPages();
			for (TextComponent textComponent : pages) {
				rolePages.add(textComponent);
				rolePageOffset++;
			}
		}
		TextComponent[] roleHeaderPages = BookUtility.truncatePage(roleHeaderPageBuilder.build());


		TextComponent headerPage = Component.text()
			.append(
				BookUtility.centerBookLine(
					Component.text("Ludos Guidebook")
						.color(NamedTextColor.DARK_GREEN)
						.decoration(TextDecoration.BOLD, true)
				)
			)
			.append(Component.text("\n"))
			.append(
				BookUtility.spaceBookLine(
					Component.text("Games").color(NamedTextColor.BLUE).append(Component.text(" :")),
					Component.text("Page " + gameHeaderPageIdx)
						.decoration(TextDecoration.UNDERLINED, true)
						.clickEvent(ClickEvent.changePage(gameHeaderPageIdx))
				)
			)
			.append(Component.text('\n'))
			.append(
				BookUtility.spaceBookLine(
					Component.text("Roles").color(NamedTextColor.RED).append(Component.text(" :")),
					Component.text("Page " + roleHeaderPageIdx)
						.decoration(TextDecoration.UNDERLINED, true)
						.clickEvent(ClickEvent.changePage(roleHeaderPageIdx))
				)
			)
			.build();


		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		BookMetaBuilder builder = meta.toBuilder();

		builder.title(
			Component.text("Ludos Guidebook")
				.color(NamedTextColor.DARK_GREEN)
				.decoration(TextDecoration.ITALIC, false)
		);
		builder.author(Component.text("Ludos"));

		builder.addPage(headerPage);

		for (TextComponent page : gameHeaderPages) {
			builder.addPage(page);
		}
		for (TextComponent page : gamePages) {
			builder.addPage(page);
		}

		for (TextComponent page : roleHeaderPages) {
			builder.addPage(page);
		}
		for (TextComponent page : rolePages) {
			builder.addPage(page);
		}

		book.setItemMeta(builder.build());
		return book;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player currentPlayer = event.getPlayer();

		boolean showMessage = PlayerConfigMap.GUIDEBOOK_MESSAGE.getPlayerConfig(currentPlayer, this);
		if (showMessage) {
			TextComponent message = Component.text("Click here to get a guidebook!")
				.color(NamedTextColor.GOLD)
				.decoration(TextDecoration.UNDERLINED, true)
				.clickEvent(ClickEvent.runCommand("/ludos guidebook"));
			currentPlayer.sendMessage(message);
		}
	}
}
