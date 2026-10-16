package fr.ludos.core.command;

import org.bukkit.command.TabExecutor;

/**
 * A Subcommand is a Command with a String ID, used as a key for a Map-like parenting relation with other Commands.
 */
public interface Subcommand extends TabExecutor, CommandInfoProvider {
	String id();
}
