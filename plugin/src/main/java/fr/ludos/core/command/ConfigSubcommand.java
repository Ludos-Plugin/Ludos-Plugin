package fr.ludos.core.command;

import org.bukkit.command.TabCompleter;

public interface ConfigSubcommand extends ConfigExecutor, TabCompleter, CommandInfoProvider {}
