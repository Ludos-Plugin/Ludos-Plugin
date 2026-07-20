package fr.ludos.core.command.ludos.game;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.game.Game;

/**
 * {@link Subcommand} to obtain a Ludos Guidebook for the specified {@link Game}.
 */
public class GameGuidebook implements Subcommand {
	private final static String ID = "guidebook";

	private final Ludos ludos;

	public GameGuidebook(Ludos ludos) {
		this.ludos = ludos;
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
		Game.Builder guidebookGame = ludos.getGameManager().getRegistered().get(guidebookGameId);
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
			return ludos.getGameManager().getRegistered().keySet().stream().sorted().collect(Collectors.toList());

		if (args.length == 2)
			return CommandUtility.getOnlinePlayerNames();

		return null;
	}
	@Override
	public String getUsage() {
		return "<" +
			ludos.getGameManager().getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}