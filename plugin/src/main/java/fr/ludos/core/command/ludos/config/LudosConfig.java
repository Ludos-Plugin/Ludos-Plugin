package fr.ludos.core.command.ludos.config;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;

/**
 * Subcommand for all Ludos configuration.
 */
public class LudosConfig implements Subcommand {
	private final static String ID = "config";
	private final ScopeConfigMap map;

	public LudosConfig(Ludos ludos) {
		this.map = new ScopeConfigMap(
			ludos,
			ludos.globalConfigMap,
			ludos.groupConfigMap,
			ludos.playerConfigMap
		);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Configure Ludos.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return map.exec(args, sender);
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return map.tabComplete(args, sender);
	}
	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return "<global | group> [config] [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}