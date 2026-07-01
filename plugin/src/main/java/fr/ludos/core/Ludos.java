package fr.ludos.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.core.book.BookUtility;
import fr.ludos.core.command.ludos.LudosCommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupEvents;
import fr.ludos.core.item.texture.TextureListener;
import fr.ludos.core.item.texture.TextureManager;
import fr.ludos.core.packets.player.PlayerPackets;
import fr.ludos.core.packets.player.PlayerPacketsFactory;
import fr.ludos.core.role.Role;
import fr.ludos.games.arena.ArenaGame;
import fr.ludos.games.manhunt.ManhuntGame;
import fr.ludos.games.raid.RaidGame;
import fr.ludos.roles.assassin.AssassinRole;
import fr.ludos.roles.berserker.BerserkerRole;
import fr.ludos.roles.harvester.HarvesterRole;
import fr.ludos.roles.huntsman.HuntsmanRole;
import fr.ludos.roles.tank.TankRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Ludos extends JavaPlugin implements Listener {

	public static final String namespace = "ludos";
	private TextureManager textureManager;

	public final PlayerPackets playerPackets = PlayerPacketsFactory.createHandler();

	public Ludos() { }

	public TextureManager getTextureManager() {
		return textureManager;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		Role.loadConfigRoles(this);
		Group.loadConfigGroups(this);

		Game.registerGame(new ManhuntGame.Builder(this));
		Game.registerGame(new ArenaGame.Builder(this));
		Game.registerGame(new RaidGame.Builder(this));

		Role.registerRole(new HuntsmanRole.Builder(this));
		Role.registerRole(new HarvesterRole.Builder(this));
		Role.registerRole(new TankRole.Builder(this));
		Role.registerRole(new AssassinRole.Builder(this));
		Role.registerRole(new BerserkerRole.Builder(this));

		textureManager = new TextureManager(this);
		getServer().getPluginManager().registerEvents(new TextureListener(this), this);

		String commandLabel = "ludos";
		PluginCommand cmd = getCommand(commandLabel);
		LudosCommand ludosCommand = new LudosCommand();
		cmd.setExecutor(ludosCommand);
		cmd.setTabCompleter(ludosCommand);
		cmd.setUsage(ludosCommand.getUsage());

		PluginCommand textureCmd = getCommand("texture");
		if (textureCmd != null) {
			textureCmd.setExecutor(textureManager);
		}

		Bukkit.getPluginManager().registerEvents(this, this);

		Bukkit.getPluginManager().registerEvents(new GroupEvents(), this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		for (Game game : Game.getActiveGames()) {
			game.stop();
		}
		HandlerList.unregisterAll((Plugin) this);
	}

	public static ItemStack createGuidebook() {
		final int gameHeaderPageIdx = 2;

		final List<Game.Builder> gameBuilders = Game.getGameBuilders();
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

		final List<Role.Builder> roleBuilders = Role.getRoleBuilders();
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
		BookMetaBuilder meta = ((BookMeta) book.getItemMeta()).toBuilder();

		meta.title(
			Component.text("Ludos Guidebook")
				.color(NamedTextColor.DARK_GREEN)
				.decoration(TextDecoration.ITALIC, false)
		);
		meta.author(Component.text("Ludos"));

		meta.addPage(headerPage);

		for (TextComponent page : gameHeaderPages) {
			meta.addPage(page);
		}
		for (TextComponent page : gamePages) {
			meta.addPage(page);
		}

		for (TextComponent page : roleHeaderPages) {
			meta.addPage(page);
		}
		for (TextComponent page : rolePages) {
			meta.addPage(page);
		}

		book.setItemMeta(meta.build());
		return book;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player currentPlayer = event.getPlayer();

		TextComponent message = Component.text("Click here to get a guidebook!")
			.color(NamedTextColor.GOLD)
			.decoration(TextDecoration.UNDERLINED, true)
			.clickEvent(ClickEvent.runCommand("/ludos guidebook"));
		currentPlayer.sendMessage(message);
	}
}
