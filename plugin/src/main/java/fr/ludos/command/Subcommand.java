package fr.ludos.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Subcommand extends TabExecutor {
	public abstract String getDescription();
	public abstract String getUsage(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label);
}
