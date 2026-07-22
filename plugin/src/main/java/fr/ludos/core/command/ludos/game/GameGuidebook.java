package fr.ludos.core.command.ludos.game;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;

/**
 * {@link Subcommand} to obtain a Ludos Guidebook for the specified {@link Game}.
 */
public class GameGuidebook implements Subcommand {
	private final static String ID = "guidebook";

	private final GameManager manager;

	public GameGuidebook(GameManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Give the guidebook for a game to a player.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		String guidebookGameId = args[0].toLowerCase();
		Game.Builder guidebookGame = manager.getRegistered().get(guidebookGameId);
		if (guidebookGame == null) {
			sender.sendMessage("Game not found: " + guidebookGameId);
			return true;
		}

		Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
		if (player == null) {
			sender.sendMessage("Player not found");
			return true;
		}

		ItemStack book = guidebookGame.createGuidebook();
		player.getInventory().addItem(book);
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return manager.getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		if (args.length == 2)
			return CommandUtility.getOnlinePlayerNames();

		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<" +
			manager.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}