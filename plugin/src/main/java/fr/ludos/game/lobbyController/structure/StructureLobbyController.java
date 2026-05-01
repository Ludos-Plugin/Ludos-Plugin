package fr.ludos.game.lobbyController.structure;

import java.time.Duration;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.game.Game;
import fr.ludos.game.lobbyController.GameLobbyController;
import fr.ludos.game.lobbyController.LobbyStartDelayOption;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class StructureLobbyController extends GameLobbyController {
	private final Function<Location, Location> structureBuilder;

	public StructureLobbyController(Game game, LobbyWaitPlayersOption waitPlayersOption, LobbyStartDelayOption waitDurationOption, Function<Location, Location> structureBuilder) {
		super(game, waitPlayersOption, waitDurationOption);
		this.structureBuilder = structureBuilder;
	}

	@Override
	public void onStart() {
		super.onStart();

		Location structureLocation = getGame().getAreaController().getCenter();
		Location waitLocation = structureBuilder.apply(structureLocation);

		new BukkitRunnable() {
			@Override
			public void run() {
				Set<OfflinePlayer> playersToWaitFor = getWaitPlayersOption().getPlayers(getGame().getGroup());
				Set<OfflinePlayer> allPlayersToTeleport = playersToWaitFor.stream()
					.filter( player -> !player.isOnline() || !player.getPlayer().getWorld().equals(waitLocation.getWorld()) )
					.collect(Collectors.toSet());

				if (allPlayersToTeleport.isEmpty()) {
					startOnTimer(getWaitDurationOption().getDuration());
					cancel();
					return;
				}

				for (OfflinePlayer offlinePlayer : playersToWaitFor) {
					Player player = offlinePlayer.getPlayer();
					if (player == null) continue;

					if (!player.getWorld().equals(waitLocation.getWorld())) {
						player.teleport(waitLocation);
					}
					else {
						player.showTitle(
							Title.title(
								Component.text("Waiting for players to join"),
								Component.empty(),
								Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
							)
						);
					}
				}
			}
		}.runTaskTimer(getPlugin(), 0, 20);
	}

}
