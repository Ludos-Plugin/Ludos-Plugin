package fr.ludos.core.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulation of a set of Subcommands, for use by parent Commands.
 */
public final class SubcommandManager implements CommandExecutor, TabCompleter, CommandUsageProvider  {
	private final Map<String, Subcommand> subcommands;
	// private Predicate<Subcommand, CommandSender> filter;

	public SubcommandManager(Map<String, Subcommand> values) {
		this.subcommands = values;
	}
	public SubcommandManager(List<Subcommand> values) {
		this(
			values.stream()
				.collect(Collectors.toMap(
					(sc) -> sc.id(),
					(sc) -> sc
				))
		);
	}
	public SubcommandManager(Subcommand[] values) {
		this(Arrays.asList(values));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		Subcommand option = subcommands.get(arg);
		if (option == null) return false;

		return option.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return subcommands.keySet()
				.stream()
				.collect(Collectors.toList());
		}

		String arg = args[0].toLowerCase();
		Subcommand option = subcommands.get(arg);
		if (option == null) return null;

		return option.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}


	public static String getUsage(Stream<String> values) {
		StringBuilder usage = new StringBuilder();
		usage.append('<');
		usage.append(
			values
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append("[option]");

		return usage.toString();
	}

	public String getUsage(@NotNull CommandSender sender) {
		return getUsage(subcommands.keySet().stream());
	}
}