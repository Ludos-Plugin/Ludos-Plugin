package fr.ludos.game.lobbyController;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.Game;
import fr.ludos.game.GameProcessBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class GameLobbyController extends GameProcessBase {
	@Override
	protected final JavaPlugin getPlugin() {
		return getGame().getPlugin();
	}

	private final Game game;
	protected final Game getGame() {
		return game;
	}

	private final LobbyWaitPlayersOption waitPlayersOption;
	public final LobbyWaitPlayersOption getWaitPlayersOption() {
		return waitPlayersOption;
	}

	private final int waitDuration;
	public final int getWaitDuration() {
		return waitDuration;
	}

	protected GameLobbyController(Game game, LobbyWaitPlayersOption waitPlayersOption, int waitDuration) {
		if (game == null) {
			throw new IllegalArgumentException("Game cannot be null");
		}

		this.game = game;
		this.waitPlayersOption = waitPlayersOption;
		this.waitDuration = waitDuration;
	}

	public final boolean areAllOnlinePlayersLoadedIn() {
		World gameWorld = getGame().getWorldController().getWorld();
		return getGame().getGroup().getPlayers().stream()
			.map( offlinePlayer -> offlinePlayer.getPlayer() )
			.allMatch( player -> player == null || player.getWorld().equals(gameWorld) );
	}
	public final boolean areAllPlayersOnlineAndLoadedIn() {
		World gameWorld = getGame().getWorldController().getWorld();
		return getGame().getGroup().getPlayers().stream()
			.map( offlinePlayer -> offlinePlayer.getPlayer() )
			.allMatch( player -> player != null && player.getWorld().equals(gameWorld) );
	}

	public final BukkitTask startOnTimer(int seconds, Runnable onFinish) {
		if (onFinish == null) return null;

		return new BukkitRunnable() {
			int timeLeft = seconds;

			@Override
			public void run() {
				if (timeLeft <= 0) {
					stop();
					cancel();

					onFinish.run();
					return;
				}

				for (OfflinePlayer offlinePlayer : getGame().getGroup().getPlayers()) {
					Player player = offlinePlayer.getPlayer();
					if (player == null) continue;
					player.showTitle(
						Title.title(
							Component.text("Game starting in " + timeLeft + " seconds"),
							Component.empty()
						)
					);
				}

				timeLeft--;
			}
		}.runTaskTimer(getPlugin(), 0, 20L);
	}

}
