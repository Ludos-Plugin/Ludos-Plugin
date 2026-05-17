package fr.ludos.command.ludos;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public final class CheatsSubcommandManager implements TabExecutor {
	public static final String arg = "cheats";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		CheatsSubcommand option = Arrays.stream(CheatsSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
		if (option == null) return false;

		return option.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			return Arrays.stream(CheatsSubcommand.values())
				.map(CheatsSubcommand::name)
				.collect(Collectors.toList());
		}

		return null;
	}

	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " " + arg + " ");

		usage.append('<');
		usage.append(
			Arrays.stream(CheatsSubcommand.values())
				.filter(o -> o != CheatsSubcommand.help)
				.map(CheatsSubcommand::name).sorted()
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');
		usage.append(' ');
		usage.append("<amount>");

		return usage.toString();
	}
}
