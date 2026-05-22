package fr.ludos.command;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class ConfigSubcommandManager<T extends Enum<T> & ConfigExecutor & TabCompleter & CommandUsageProvider> implements ConfigExecutor, TabCompleter, CommandUsageProvider {
	private final Function<CommandSender, Stream<T>> valuesFunc;
	private final List<T> values;
	public Stream<T> getValues() {
		return getValues(null);
	}
	public Stream<T> getValues(CommandSender sender) {
		if (values != null) {
			return values.stream();
		}

		if (valuesFunc != null) {
			return valuesFunc.apply(sender);
		}

		return null;
	}

	public ConfigSubcommandManager(Function<CommandSender, Stream<T>> valuesFunc) {
		this.values = null;
		this.valuesFunc = valuesFunc;
	}
	public ConfigSubcommandManager(List<T> values) {
		this.values = values;
		this.valuesFunc = null;
	}
	public ConfigSubcommandManager(T[] values) {
		this(Arrays.asList(values));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, ConfigurationSection config, @NotNull String[] args) {
		if (args.length == 0) return false;

		String arg = args[0].toLowerCase();
		T option = getValues(sender)
			.filter(o -> o.name().equalsIgnoreCase(arg))
			.findFirst()
			.orElse(null);
		if (option == null) return false;

		return option.onCommand(sender, command, label, config, Arrays.copyOfRange(args, 1, args.length));
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return getValues(sender)
				.map(T::name)
				.collect(Collectors.toList());
		}

		String arg = args[0].toLowerCase();
		T option = getValues(sender)
			.filter(o -> o.name().equalsIgnoreCase(arg))
			.findFirst()
			.orElse(null);
		if (option == null) return null;

		return option.onTabComplete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
	}


	public static <T extends Enum<T> & ConfigExecutor & TabCompleter & CommandUsageProvider> String getUsage(T[] values) {
		return getUsage(Arrays.stream(values));
	}
	public static <T extends Enum<T> & ConfigExecutor & TabCompleter & CommandUsageProvider> String getUsage(Stream<T> values) {
		StringBuilder usage = new StringBuilder();
		usage.append('<');
		usage.append(
			values
				.map(T::name)
				.collect(Collectors.joining(" | "))
		);
		usage.append('>');

		usage.append(' ');

		usage.append("[option]");

		return usage.toString();
	}

	public String getUsage() {
		return getUsage(getValues());
	}
}