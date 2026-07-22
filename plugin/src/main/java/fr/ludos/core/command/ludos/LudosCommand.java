package fr.ludos.core.command.ludos;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.HelpSubcommand;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandManager;
import fr.ludos.core.command.ludos.cheats.CheatsSubcommand;
import fr.ludos.core.command.ludos.config.LudosConfig;
import fr.ludos.core.command.ludos.game.GameSubcommand;
import fr.ludos.core.command.ludos.group.GroupSubcommand;
import fr.ludos.core.command.ludos.role.RoleSubcommand;

/**
 * {@link Subcommand} encapsulating all Ludos subcommands.
 */
public class LudosCommand implements Subcommand {
	private final static String ID = "ludos";

	private final SubcommandManager manager;

	public LudosCommand(Ludos ludos) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GroupSubcommand(ludos.getGroupManager()));
			add(new GameSubcommand(ludos.getGameManager()));
			add(new RoleSubcommand(ludos.getRoleManager()));
			add(new LudosConfig(ludos));
			add(new CheatsSubcommand(ludos));
			add(new LudosGuidebook(ludos));
		}};
		HelpSubcommand help = new HelpSubcommand("ludos", subcommands);
		subcommands.add(help);
		manager = new SubcommandManager(subcommands);
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public String getDescription() {
		return "Main Ludos Command";
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return manager.onCommand(sender, command, label, args);
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return manager.onTabComplete(sender, command, label, args);
	}

	@Override
	public String getUsage(@NotNull CommandSender sender) {
		return manager.getUsage(sender);
	}

	@Override
	public boolean requireOp() {
		return false;
	}
}
