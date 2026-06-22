package fr.ludos.core;

import java.util.ArrayList;
import java.util.List;

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
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMetaBuilder meta = ((BookMeta) book.getItemMeta()).toBuilder();

		meta.title(
			Component.text("Ludos Guidebook")
				.color(NamedTextColor.DARK_GREEN)
				.decoration(TextDecoration.ITALIC, false)
		);
		meta.author(Component.text("Ludos"));

		List<TextComponent> gamePages = new ArrayList<>();
		for (Game.Builder builder : Game.getGameBuilders()) {
			for (TextComponent page : builder.buildPages()) {
				gamePages.add(page);
			}
		}

		List<TextComponent> rolePages = new ArrayList<>();
		for (Role.Builder builder : Role.getRoleBuilders()) {
			for (TextComponent page : builder.buildPages()) {
				rolePages.add(page);
			}
		}

		int gamePage = 2;
		int rolePage = gamePage + gamePages.size();

		TextComponent headerPage = Component.text()
			.append(
				BookUtility.centerBookLine(
					Component.text("Ludos Guidebook")
						.color(NamedTextColor.DARK_GREEN)
						.decoration(TextDecoration.BOLD, true)
				)
			)
			.append(Component.text("\n\n\n"))
			.append(
				BookUtility.spaceBookLine(
					Component.text("Games :"),
					Component.text("Page " + gamePage)
						.color(NamedTextColor.BLUE)
						.decoration(TextDecoration.UNDERLINED, true)
						.clickEvent(ClickEvent.changePage(gamePage))
				)
			)
			.append(Component.text('\n'))
			.append(
				BookUtility.spaceBookLine(
					Component.text("Roles :"),
					Component.text("Page " + rolePage)
						.color(NamedTextColor.RED)
						.decoration(TextDecoration.UNDERLINED, true)
						.clickEvent(ClickEvent.changePage(rolePage))
				)
			)
			.build();

		meta.addPage(headerPage);
		for (TextComponent page : gamePages) {
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
