package fr.ludos.core.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CommandUtility {

	@Nullable
	public static Player getPlayerFromArg(String[] args, int index, CommandSender sender) {
		if ( args.length > index ) {
			return Bukkit.getPlayer(args[index]);
		} else {
			return null;
		}
	}
	@Nullable
	public static OfflinePlayer getOfflinePlayerFromArg(String[] args, int index, CommandSender sender) {
		if ( args.length > index ) {
			return Bukkit.getOfflinePlayer(args[index]);
		} else {
			return null;
		}
	}

	@Nullable
	public static Player getPlayerFromArgsOrSender(String[] args, int index, CommandSender sender) {
		Player target = getPlayerFromArg(args, index, sender);

		if ( target == null && (sender instanceof Player playerSender) ) {
			target = playerSender;
		}

		return target;
	}
	@Nullable
	public static OfflinePlayer getOfflinePlayerFromArgsOrSender(String[] args, int index, CommandSender sender) {
		OfflinePlayer target = getOfflinePlayerFromArg(args, index, sender);

		if ( target == null && (sender instanceof Player playerSender) ) {
			target = playerSender;
		}

		return target;
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