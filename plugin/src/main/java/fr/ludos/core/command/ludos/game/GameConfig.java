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
import fr.ludos.core.game.GameManager;

/**
 * {@link Subcommand} for {@link Game}-specific configuration.
 */
public class GameConfig implements Subcommand {
	private final static String ID = "config";

	private final GameManager gameManager;
	private final ScopeConfigMap map;

	public GameConfig(Ludos ludos) {
		this.gameManager = ludos.getGameManager();
		this.map = new ScopeConfigMap(ludos, gameManager.configMap);
	}

	@Override
	public String id() {
		return ID;
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
			gameManager.getRegistered().keySet().stream().sorted()
				.collect(Collectors.joining(" | "))
			+ "> [name] [option]";
	}
	@Override
	public boolean requireOp() {
		return false;
	}
}