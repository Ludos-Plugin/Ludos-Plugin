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

import fr.ludos.Ludos;
import fr.ludos.command.CommandUtility;
import fr.ludos.role.Role;

public final class StructureSubcommandManager implements TabExecutor {
	public static final String arg = "structure";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		StructureSubcommand option = Arrays.stream(StructureSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
		if (option == null) return false;

		return option.onCommand(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(StructureSubcommand.values())
				.map(StructureSubcommand::name)
				.collect(Collectors.toList());
		}

		String arg = args[0].toLowerCase();
		StructureSubcommand option = Arrays.stream(StructureSubcommand.values()).filter(o -> o.name().equalsIgnoreCase(arg)).findFirst().orElse(null);
		if (option == null) return null;

		return option.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}

	public String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label) {
		StringBuilder usage = new StringBuilder("/" + label + " " + arg + " ");

		usage.append('<');
		usage.append(
			Arrays.stream(StructureSubcommand.values())
				.filter(o -> o != StructureSubcommand.help)
				.map(StructureSubcommand::name).sorted()
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append('<');
		usage.append(
			Role.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append("[option]");

		return usage.toString();
	}
}
