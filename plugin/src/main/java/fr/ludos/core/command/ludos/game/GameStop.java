package fr.ludos.core.command.ludos.game;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.group.GroupConfigs;
import fr.ludos.core.game.Game;
import fr.ludos.core.group.Group;

public class GameStop implements Subcommand {
	private final static String id = "stop";

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Stop the current game.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can stop games.");
			return true;
		}

		Group group = Group.getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		boolean membersCanRunGames = GroupConfigs.getGroupRightsOption(group.getConfig()).canRunGames();
		if (! group.isLeader(player) && ! membersCanRunGames) {
			sender.sendMessage("Only the group leader can stop the game.");
			return true;
		}

		Game game = group.getGame();
		if (game == null) {
			sender.sendMessage("There is no game running.");
			return true;
		}

		game.stop();
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return null;
	}
	@Override
	public String getUsage() {
		return "";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}