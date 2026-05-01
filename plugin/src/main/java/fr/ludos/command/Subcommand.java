package fr.ludos.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

public interface Subcommand extends TabExecutor {
	public abstract String getDescription();
	public abstract String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label);
}
