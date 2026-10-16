package fr.ludos.core.command;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * {@link Subcommand} for getting the usage of another {@link Subcommand}.
 */
public class HelpSubcommand implements Subcommand {
	private final static String ID = "help";

	private final String label;
	private final Map<String, Subcommand> subcommands;
	public HelpSubcommand(String label, Map<String, Subcommand> subcommands) {
		this.label = label;
		this.subcommands = subcommands;
	}
	public HelpSubcommand(String label, List<Subcommand> subcommands) {
		this(
			label,
			subcommands.stream()
				.filter((sc) -> sc.id() != ID)
				.collect(Collectors.toMap(
					(sc) -> sc.id(),
					(sc) -> sc
				))
		);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Show help for " + label + " commands.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length < 1) {
			sender.sendMessage(getUsage(sender));
			return true;
		}

		String arg = args[0].toLowerCase();
		Subcommand sc = subcommands.get(arg);
		if (sc == null) return false;

		sender.sendMessage(sc.getUsage(sender));
		return true;
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			return subcommands.keySet()
				.stream()
				.collect(Collectors.toList());
		}
		return null;
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return SubcommandManager.getUsage(
			subcommands.keySet()
				.stream()
		);
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}