package fr.ludos.game.lobbyController.structure;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.game.Game;
import fr.ludos.game.lobbyController.GameLobbyController;
import fr.ludos.game.lobbyController.LobbyStartDelayOption;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import fr.ludos.structure.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class StructureLobbyController extends GameLobbyController {
	private final Structure.Builder structureBuilder;
	private BukkitTask joinLobbyTask;
	private BukkitTask startGameTask;

	public StructureLobbyController(Game game, LobbyWaitPlayersOption waitPlayersOption, LobbyStartDelayOption waitDurationOption, Structure.Builder structureBuilder) {
		super(game, waitPlayersOption, waitDurationOption);
		this.structureBuilder = structureBuilder;
	}

	@Override
	public void onStart() {
		super.onStart();

		Location structureLocation = getGame().getAreaController().getCenter();
		Structure structure = structureBuilder.build(structureLocation);

		Location waitLocation = structure.getEntranceLocation();

		joinLobbyTask = new BukkitRunnable() {
			@Override
			public void run() {
				Set<OfflinePlayer> targetPlayers = getWaitPlayersOption().getPlayers(getGame().getGroup());
				Set<Player> waitingPlayers = targetPlayers.stream()
					.map(OfflinePlayer::getPlayer)
					.filter(Objects::nonNull)
					.filter(player -> player.getWorld().equals(waitLocation.getWorld()) )
					.collect(Collectors.toSet());
				Set<OfflinePlayer> allPlayersToTeleport = targetPlayers.stream()
					.filter(player -> !player.isOnline() || !player.getPlayer().getWorld().equals(waitLocation.getWorld()) )
					.collect(Collectors.toSet());

				if (allPlayersToTeleport.isEmpty()) {
					startGameTask = startOnTimer(getWaitDurationOption().getDuration(), () -> {structure.destroy();});
					for (Player waitingPlayer : waitingPlayers) {
						waitingPlayer.clearTitle();
					}
					cancel();
					return;
				}

				for (Player waitingPlayer : waitingPlayers) {
					waitingPlayer.showTitle(
						Title.title(
							Component.text("Waiting for players to join"),
							Component.empty(),
							Times.times(Duration.ZERO, Duration.ofHours(5), Duration.ZERO)
						)
					);
				}

				for (OfflinePlayer waitedPlayer : allPlayersToTeleport) {
					Player player = waitedPlayer.getPlayer();
					if (player == null) continue;
					if (! player.isOnline()) continue;

					player.teleport(waitLocation);

					player.getInventory().clear();
				}
			}
		}.runTaskTimer(getPlugin(), 0, 20);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (startGameTask != null) {
			startGameTask.cancel();
			startGameTask = null;
		}

		if (joinLobbyTask != null) {
			joinLobbyTask.cancel();
			joinLobbyTask = null;
		}
	}

	@EventHandler
	public void onPlayerDieInLobby(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		if (! getGame().getGroup().isPlayer(player)) return;

		if (joinLobbyTask != null) {
			event.setCancelled(true);
			player.playEffect(EntityEffect.TOTEM_RESURRECT);
		}
	}

}
