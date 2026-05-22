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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.lobbyController.GameLobbyController;
import fr.ludos.game.lobbyController.LobbyWaitPlayersOption;
import fr.ludos.structure.Structure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class StructureLobbyController extends GameLobbyController {
	private final Structure.Builder structureBuilder;
	private final Runnable startFunc;

	private BukkitTask joinLobbyTask;
	private BukkitTask startGameTask;

	public StructureLobbyController(Game game, LobbyWaitPlayersOption waitPlayersOption, int waitDuration, Structure.Builder structureBuilder, Runnable onStart) {
		super(game, waitPlayersOption, waitDuration);
		this.startFunc = Objects.requireNonNull(onStart, "No Start function provided for lobby");
		this.structureBuilder = structureBuilder;
	}

	@Override
	public void onStart() {
		super.onStart();

		Location structureLocation = getGame().getWorldController().getAreaController().getCenter();
		Structure structure = structureBuilder.build(structureLocation);

		Location waitLocation = structure.getEntranceLocation();
		Set<Player> targetPlayers = Utility.getOnline(getWaitPlayersOption().getPlayers(getGame().getGroup())).collect(Collectors.toSet());
		for (Player player : targetPlayers) {
			player.teleport(waitLocation);
			Utility.resetPlayer(player);
		}

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
					startGameTask = startOnTimer(getWaitDuration(), () -> {
						structure.destroy();

						startFunc.run();
					});
					for (Player waitingPlayer : waitingPlayers) {
						Utility.resetPlayer(waitingPlayer);
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
					Utility.resetPlayer(player);
				}
			}
		}.runTaskTimer(getPlugin(), 0, 20);
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (joinLobbyTask != null) {
			joinLobbyTask.cancel();
		}
		joinLobbyTask = null;

		if (startGameTask != null) {
			startGameTask.cancel();
		}
		startGameTask = null;
	}

	@EventHandler
	public void onPlayerDieInLobby(PlayerDeathEvent event) {
		Player player = event.getPlayer();
		if (! getGame().getGroup().isPlayer(player)) return;

		event.setCancelled(true);
		player.playEffect(EntityEffect.TOTEM_RESURRECT);
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (! (event.getEntity() instanceof Player player)) return;
		if (! getGame().getGroup().isPlayer(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (player == null || ! getGame().getGroup().isPlayer(player)) return;

		event.setCancelled(true);
	}
}
