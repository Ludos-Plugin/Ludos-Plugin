package fr.ludos.core.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility for Commands.
 */
public class CommandUtility {

	@Nullable
	public static Player getPlayerFromArg(String[] args, int index) {
		if ( args.length > index ) {
			return Bukkit.getPlayer(args[index]);
		} else {
			return null;
		}
	}

	public static OfflinePlayer getOfflinePlayerFromArg(String[] args, int index) {
		if ( args.length > index ) {
			return Bukkit.getOfflinePlayer(args[index]);
		} else {
			return null;
		}
	}

	@Nullable
	public static Player getPlayerFromArgsOrSender(String[] args, int index, CommandSender sender) {
		Player target = getPlayerFromArg(args, index);

		if ( target == null && (sender instanceof Player playerSender) ) {
			target = playerSender;
		}

		return target;
	}
	@Nullable
	public static OfflinePlayer getOfflinePlayerFromArgsOrSender(String[] args, int index, CommandSender sender) {
		OfflinePlayer target = getOfflinePlayerFromArg(args, index);

		if ( target == null && (sender instanceof Player playerSender) ) {
			target = playerSender;
		}

		return target;
	}

	public static List<OfflinePlayer> getOfflinePlayersFromArgs(String[] args, CommandSender sender) {
		return Arrays.stream(args)
			.map(Bukkit::getOfflinePlayer)
			.collect(Collectors.toList());
	}

	public static List<Player> getPlayersFromArgs(String[] args, CommandSender sender) {
		return Arrays.stream(args)
			.map(Bukkit::getPlayer)
			.collect(Collectors.toList());
	}

	public static List<String> getOnlinePlayerNames() {
		return Bukkit.getServer().getOnlinePlayers().stream()
				.map(Player::getName)
				.collect(Collectors.toList());
	}
}