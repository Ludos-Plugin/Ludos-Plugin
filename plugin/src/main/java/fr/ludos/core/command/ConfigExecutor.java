package fr.ludos.core.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public interface ConfigExecutor {
	public abstract boolean onCommand(CommandSender sender, Command command, String label, ConfigurationSection config, String[] args);
}
