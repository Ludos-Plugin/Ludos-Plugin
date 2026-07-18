package fr.ludos.core.command.ludos.cheats;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;
import fr.ludos.core.item.SpecialItem;
import fr.ludos.core.item.level.LevelItemInterface;

public class CheatsXp implements Subcommand {
	private final static String ID = "xp";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Add XP to the held level item.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length != 1) return false;

		Integer amount = CheatsSubcommand.parsePositiveInt(args[0]);
		if (amount == null) {
			sender.sendMessage("Invalid number");
			return true;
		}

		if (! (sender instanceof Player player)) {
			sender.sendMessage("Only players can use cheats.");
			return true;
		}
		if (!player.isOp()) {
			sender.sendMessage("Only server operators can use cheats.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		Game game = group.getGame();
		if (game == null) {
			sender.sendMessage("You are not in a game.");
			return true;
		}

		ItemStack heldItem = player.getInventory().getItemInMainHand();
		if (heldItem == null || heldItem.getType().isAir()) {
			sender.sendMessage("Hold a LevelItem or MultiLevelBranchItem in your main hand.");
			return true;
		}

		SpecialItem item = CheatsSubcommand.findHeldSpecialItem(game, heldItem);
		if (item == null) {
			sender.sendMessage("Held item is not a valid game item.");
			return true;
		}

		if (item instanceof LevelItemInterface levelItem) {
			levelItem.addXp(amount);
		} else {
			sender.sendMessage("Held item is not a valid level item.");
			return true;
		}

		sender.sendMessage("Added " + amount + " XP to your held item.");
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return null;
	}
	@Override
	public String getUsage() {
		return "<amount>";
	}

	@Override
	public boolean requireOp() {
		return true;
	}
}