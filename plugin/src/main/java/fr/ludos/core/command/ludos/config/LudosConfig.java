package fr.ludos.core.command.ludos.config;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;

public class LudosConfig implements Subcommand {
	private final static String ID = "config";
	private final ScopeConfigMap map;

	public LudosConfig(Ludos plugin) {
		this.map = new ScopeConfigMap(plugin, GlobalConfigMap.INSTANCE, LocalConfigMap.INSTANCE);
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
	public String getUsage() {
		return "<global | group> [config] [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}