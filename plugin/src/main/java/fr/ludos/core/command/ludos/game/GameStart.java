package fr.ludos.core.command.ludos.game;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.game.Game;
import fr.ludos.core.game.GameManager;
import fr.ludos.core.group.Group;

/**
 * {@link Subcommand} to start a given {@link Game}, as a {@link Group} leader.
 */
public class GameStart implements Subcommand {
	private final static String ID = "start";
	private final GameManager manager;

	public GameStart(GameManager manager) {
		this.manager = manager;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "As a group leader, start a game.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) return false;

		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can start games.");
			return true;
		}

		String startGameId = args[0].toLowerCase();
		if (! manager.getRegistered().containsKey(startGameId) ) {
			sender.sendMessage("Game not found: " + startGameId);
			return true;
		}

		Group group = manager.getLudos().getGroupManager().getGroupOfPlayer(player);
		if (group == null) {
			sender.sendMessage("You are not in a group.");
			return true;
		}

		boolean membersCanRunGames = GroupConfigMap.MEMBERS_AUTH.getGroupConfig(group).canRunGames();
		if (! group.isLeader(player) && ! membersCanRunGames) {
			sender.sendMessage("Only the group leader can start games.");
			return true;
		}

		manager.startGame(startGameId, group);
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1)
			return manager.getRegistered().keySet().stream().sorted().collect(Collectors.toList());
		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<" +
			manager.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ ">";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}