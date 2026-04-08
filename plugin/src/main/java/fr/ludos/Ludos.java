package fr.ludos;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.BookMetaBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import fr.ludos.book.BookUtility;
import fr.ludos.command.ludos.LudosCommand;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
import fr.ludos.game.sheepwars.SheepwarsGame;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.HuntsmanRole;
import fr.ludos.role.Role;
import fr.ludos.role.TrapperRole;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;


public class Ludos extends JavaPlugin implements Listener {

	public static final String namespace = "ludos";

	public Ludos() { }

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Role.loadConfigRoles(this);

        // getServer().getPluginManager().registerEvents(new Sheep(), this);

		Game.registerGame(new ManhuntGame.Builder(this));
		Game.registerGame(new SheepwarsGame.Builder(this));

		Role.registerRole(new HuntsmanRole.Builder(this));
		Role.registerRole(new BurrowerRole.Builder(this));
		Role.registerRole(new TrapperRole.Builder(this));



		PluginCommand cmd = getCommand("ludos");
		LudosCommand ludosCommand = new LudosCommand(this);
		cmd.setExecutor(ludosCommand);
		cmd.setTabCompleter(ludosCommand);
		// cmd.setUsage(ludosCommand.getUsage());

		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		Game.stopCurrentGame();
		HandlerList.unregisterAll((Listener)this);
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

	@org.bukkit.event.EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player currentPlayer = event.getPlayer();

		TextComponent message = Component.text("Click here to get a guidebook!")
			.color(NamedTextColor.GOLD)
			.decoration(TextDecoration.UNDERLINED, true)
			.clickEvent(ClickEvent.runCommand("/ludos guidebook"));
		currentPlayer.sendMessage(message);
	}
}
