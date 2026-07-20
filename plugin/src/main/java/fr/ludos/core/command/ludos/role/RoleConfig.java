package fr.ludos.core.command.ludos.role;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.role.Role;
import fr.ludos.core.role.RoleManager;

/**
 * {@link Subcommand} for {@link Role}-specific configuration.
 */
public class RoleConfig implements Subcommand {
	private final static String ID = "config";

	private final RoleManager manager;
	private final ScopeConfigMap map;

	public RoleConfig(RoleManager manager) {
		this.manager = manager;
		this.map = new ScopeConfigMap(manager.getLudos(), manager.configMap);
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
			manager.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}