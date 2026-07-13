package fr.ludos.core.command.ludos.game;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import fr.ludos.core.Ludos;
import fr.ludos.core.command.Subcommand;
import fr.ludos.core.command.ludos.ScopeConfigMap;
import fr.ludos.core.game.Game;

public class GameConfig implements Subcommand {
	private final static String id = "config";
	private final ScopeConfigMap map;

	public GameConfig(Ludos plugin) {
		this.map = new ScopeConfigMap(plugin, GameConfigMap.instance);
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String getDescription() {
		return "Configure a game.";
	}
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return map.exec(args, sender);
	}
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return map.tabComplete(args, sender);
	}
	@Override
	public String getUsage() {
		return "<" +
			Game.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}