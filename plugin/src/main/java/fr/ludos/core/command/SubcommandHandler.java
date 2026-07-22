package fr.ludos.core.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around a {@link SubcommandManager} for use as a Subcommand.
 */
public class SubcommandHandler implements Subcommand {
	private final String id;
	private final String description;
	private final boolean requireOp;
	private final SubcommandManager manager;

	public SubcommandHandler(String id, String description, boolean requireOp, ArrayList<Subcommand> subcommands) {
		this.id = id;
		this.description = description;
		this.requireOp = requireOp;
		manager = new SubcommandManager(subcommands);
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
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
		return requireOp;
	}
}
