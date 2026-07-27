package fr.ludos.core.command.ludos.config;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;

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
		return map.execute(plugin, args, sender);
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return map.tabComplete(args, sender);
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return '<' + map.options(sender).stream().collect(Collectors.joining(" | ")) + "> [config] [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}