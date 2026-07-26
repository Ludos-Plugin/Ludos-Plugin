package fr.ludos.core.command.ludos.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.persistence.config.ConfigNodeOperation;

/**
 * Subcommand for configuration.
 */
public class ConfigSubcommand implements Subcommand {
	private static final String ID = "config";
	private final Plugin plugin;
	private final String description;
	private final ScopeConfigMap map;

	public ConfigSubcommand(Plugin plugin, String description, ScopeConfigMap map) {
		this.plugin = Objects.requireNonNull(plugin);
		this.description = Objects.requireNonNull(description);
		this.map = Objects.requireNonNull(map);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return description;
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 0) return false;

		try {
			ConfigNodeOperation op = Enum.valueOf(ConfigNodeOperation.class, args[0]);
			return map.execute(plugin, Arrays.copyOfRange(args, 1, args.length), sender, op);
		} catch (Exception e) {
			return false;
		}
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length <= 1) {
			return Arrays.stream(ConfigNodeOperation.values()).map(Enum::name).toList();
		}

		try {
			ConfigNodeOperation op = Enum.valueOf(ConfigNodeOperation.class, args[0]);
			return map.tabComplete(Arrays.copyOfRange(args, 1, args.length), sender, op);
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return '<' + Arrays.stream(ConfigNodeOperation.values()).map(Enum::name).collect(Collectors.joining(" | ")) + "> <" + map.getProviderKeys(sender).stream().collect(Collectors.joining(" | ")) + "> [config] [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}