package fr.ludos.game.manhunt;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Random;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.entity.Player;

import fr.ludos.Utility;
import fr.ludos.game.Game;
import fr.ludos.game.TeamController;


public final class ManhuntTeamController extends TeamController {
	public Team hunterTeam;
	public Team preyTeam;

	private final Set<Player> selectedPlayers;
	public Set<Player> getSelectedPlayers() {
		return selectedPlayers;
	}
	private final Player selectedPrey;
	public Player getSelectedPrey() {
		return selectedPrey;
	}


	public ManhuntTeamController(ManhuntGame game, @Nullable Set<Player> players, @Nullable Player prey) {
		super(game, game.getScoreboard());

		if (players == null) {
			players = new HashSet<>(Bukkit.getOnlinePlayers());
		}

		if (prey == null) {
			Player[] playersArray = players.toArray(new Player[players.size()]);
			prey = playersArray[ new Random().nextInt(players.size()) ];
		}

		if (prey == null) {
			throw new IllegalArgumentException("Prey could not be selected");
		}
		players.remove(prey);

		this.selectedPlayers = players;
		this.selectedPrey = prey;
	}

	@Override
	protected void onStart() {
		hunterTeam = scoreboard.getTeam("Hunters");
		if (hunterTeam == null) {
			hunterTeam = scoreboard.registerNewTeam("Hunters");
			hunterTeam.color(NamedTextColor.RED);
			hunterTeam.setAllowFriendlyFire(false);
		}

		preyTeam = scoreboard.getTeam("Prey");
		if (preyTeam == null) {
			preyTeam = scoreboard.registerNewTeam("Prey");
			preyTeam.color(NamedTextColor.BLUE);
			preyTeam.setAllowFriendlyFire(false);
		}

		preyTeam.addEntry(selectedPrey.getName());
		selectedPrey.setScoreboard(scoreboard);

		for (Player hunter : selectedPlayers) {
			if (hunter == null) continue;
			hunterTeam.addEntry(hunter.getName());
			hunter.setScoreboard(scoreboard);
		}
	}

	@Override
	protected void onStop() {
		preyTeam.unregister();
		preyTeam = null;

		hunterTeam.unregister();
		hunterTeam = null;
	}


	public Set<Player> getHunters() {
		if (hunterTeam == null) return Set.of();
		return hunterTeam.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(p -> p != null)
			.collect(Collectors.toSet());
	}
	public Optional<Player> getPrey() {
		if (preyTeam == null) return Optional.empty();
		return preyTeam.getEntries().stream()
			.map(Bukkit::getPlayerExact)
			.filter(p -> p != null)
			.findFirst();
	}


	@Override
	public Collection<Team> getTeams() {
		return List.of(hunterTeam, preyTeam);
	}
	@Override
	public Collection<Player> getPlayers() {
		Set<Player> hunters = getHunters();

		Optional<Player> prey = getPrey();
		if (prey.isPresent()) {
			hunters.add(prey.get());
		}

		return hunters;
	}


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (! preyTeam.hasEntry(event.getEntity().getName())) {
			Utility.onDeathSpectate(event.getEntity(), 5, game.getPlugin());
			return;
		}

		Bukkit.getServer().broadcast(Component.text("Prey " + player.getName() + " Slain!")); // TODO: Translate
		preyTeam.removeEntry(player.getName());

		if (preyTeam.getSize() == 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().sendMessage(Component.text("All Prey Dead! End of Game!")); // TODO: Translate
					Game.stopCurrentGame();
				}
			}.runTaskLater(game.getPlugin(), 0);
		}
	}

	// @Override
	// public void updatePlayerTeam(Player player) {
	// 	String newPlayer = player.getName();
	// 	if (! huntersBound && ! preyTeam.hasEntry(newPlayer) && ! hunterTeam.hasEntry(newPlayer)) {
	// 		for (String hunterName : hunterTeam.getEntries()) {
	// 			var teammate = Bukkit.getPlayerExact(hunterName);
	// 			if (teammate == null) continue;

	// 			player.setBedSpawnLocation(teammate.getLocation());
	// 			player.teleport(teammate.getLocation());
	// 		}
	// 		hunterTeam.addEntry(newPlayer);
	// 	}
	// }
}