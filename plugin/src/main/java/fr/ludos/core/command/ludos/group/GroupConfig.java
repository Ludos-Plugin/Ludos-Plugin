package fr.ludos.core.command.ludos.group;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;

/**
 * {@link Subcommand} for {@link Group}-specific configuration.
 */
public class GroupConfig implements Subcommand {
	private final static String ID = "config";
	private final Ludos ludos;
	private final ScopeConfigMap map;

	public GroupConfig(Ludos ludos) {
		this.ludos = ludos;
		this.map = new ScopeConfigMap(ludos, GroupConfigMap.INSTANCE);
	}


	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Configure a game for this group.";
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
			ludos.getGameManager().getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}