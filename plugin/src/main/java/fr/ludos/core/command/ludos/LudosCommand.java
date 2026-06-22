package fr.ludos.core.command.ludos;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.SubcommandManager;

public class LudosCommand implements Subcommand {
	private final SubcommandManager<LudosSubcommand> manager = new SubcommandManager<>(LudosSubcommand::getAllowedSubcommands);

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
