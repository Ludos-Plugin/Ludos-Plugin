package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.group.GroupManager;

/**
 * {@link Subcommand} for {@link Group}-specific configuration.
 */
public class GroupConfig implements Subcommand {
	private final static String ID = "config";
	private final ScopeConfigMap map;

	public GroupConfig(GroupManager manager) {
		this.map = new ScopeConfigMap(manager.getLudos(), GroupConfigMap.INSTANCE);
	}


	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Configure for this group.";
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
		return "<" +
			GroupConfigMap.INSTANCE.options().getOptions(sender).stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}