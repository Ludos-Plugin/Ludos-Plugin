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
import fr.ludos.core.command.ludos.game.GameSubcommand;
import fr.ludos.core.command.ludos.group.GroupSubcommand;
import fr.ludos.core.command.ludos.role.RoleSubcommand;

public class LudosCommand implements Subcommand {
	private final static String ID = "ludos";

	private final SubcommandManager manager;

	public LudosCommand(Ludos plugin) {
		ArrayList<Subcommand> subcommands = new ArrayList<>() {{
			add(new GroupSubcommand(plugin));
			add(new GameSubcommand(plugin));
			add(new RoleSubcommand(plugin));
			add(new CheatsSubcommand());
			add(new LudosGuidebook());
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
	public String getUsage() {
		return manager.getUsage();
	}

	@Override
	public boolean requireOp() {
		return false;
	}
}
