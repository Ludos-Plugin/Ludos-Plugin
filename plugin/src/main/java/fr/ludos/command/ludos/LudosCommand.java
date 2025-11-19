package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;

public class LudosCommand implements TabExecutor {
	private final Ludos plugin;
	private final GameSubcommand gameCommand;
	private final RoleSubcommand roleCommand;

	public LudosCommand(Ludos plugin) {
		this.plugin = plugin;

		this.gameCommand = new GameSubcommand(this.plugin);
		this.roleCommand = new RoleSubcommand(this.plugin);
	}


	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(LudosSubcommands.values()).map(LudosSubcommands::toString)
				.collect(Collectors.toList());
		}

		String arg = args[0];
		LudosSubcommands option = Arrays.stream(LudosSubcommands.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return null;

		switch (option) {
			case game:
				return gameCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
			case role:
				return roleCommand.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
			case guidebook:
				return CommandUtility.getOnlinePlayerNames();
			default:
				return null;
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) {
			sender.sendMessage(getUsage(sender, command, label));
			return true;
		}

		String arg = args[0];
		LudosSubcommands option = Arrays.stream(LudosSubcommands.values()).filter(o -> o.toString().equals(arg)).findFirst().orElse(null);
		if (option == null) return false;

		switch (option) {
			case game:
				return gameCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
			case role:
				return roleCommand.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
			case guidebook:
				Player player = CommandUtility.getPlayerFromArgsOrSender(args, 1, sender);
				if (player != null) {
					ItemStack book = Ludos.createGuidebook();
					player.getInventory().addItem(book);
				}
				return true;
			case help:
				sender.sendMessage(getUsage(sender, command, label));
				return true;
			default:
				return false;
		}
	}

	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " ");

		usage.append('<');
		usage.append(
			Arrays.stream(LudosSubcommands.values()).map(LudosSubcommands::toString)
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		return usage.toString();
	}

}
