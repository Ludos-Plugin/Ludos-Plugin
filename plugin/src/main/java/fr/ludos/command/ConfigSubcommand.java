package fr.ludos.command;

import org.bukkit.command.TabCompleter;

public interface ConfigSubcommand extends ConfigExecutor, TabCompleter, CommandInfoProvider {}
