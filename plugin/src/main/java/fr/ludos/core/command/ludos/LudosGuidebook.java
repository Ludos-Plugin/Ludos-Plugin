package fr.ludos.core.command.ludos;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.CommandUtility;
import fr.ludos.core.command.Subcommand;

/**
 * {@link Subcommand} to obtain a Ludos Guidebook.
 */
public class LudosGuidebook implements Subcommand {
	private final static String ID = "guidebook";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Give a Ludos guidebook to a player.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		Player player = CommandUtility.getPlayerFromArgsOrSender(args, 0, sender);
		if (player != null) {
			ItemStack book = Ludos.createGuidebook();
			player.getInventory().addItem(book);
		}
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return CommandUtility.getOnlinePlayerNames();
		return null;
	}
	@Override
	public String getUsage() {
		return "[player]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}