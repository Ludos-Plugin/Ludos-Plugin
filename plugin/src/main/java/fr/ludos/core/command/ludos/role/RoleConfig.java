package fr.ludos.core.command.ludos.role;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.role.Role;

public class RoleConfig implements Subcommand {
	private final static String ID = "config";

	private final ScopeConfigMap map;

	public RoleConfig(Ludos plugin) {
		this.map = new ScopeConfigMap(plugin, RoleConfigMap.INSTANCE);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Configure a role.";
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
		return "<" +
			Role.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}