package fr.ludos.command;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandUtility {

	@Nullable
	public static Player getPlayerFromArgsOrSender(String[] args, int index, CommandSender sender) {
		Player target = null;

		if ( args.length > index ) {
			target = Bukkit.getPlayerExact(args[index]);
		}
		if ( target == null && (sender instanceof Player playerSender) ) {
			target = playerSender;
		}

		return target;
	}

	public static List<String> getOnlinePlayerNames() {
		return Bukkit.getServer().getOnlinePlayers().stream()
				.map(Player::getName)
				.collect(Collectors.toList());
	}
}