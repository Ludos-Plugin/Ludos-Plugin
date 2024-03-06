package fr.ludos.game.manhunt;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import fr.ludos.Main;
import fr.ludos.game.Game;
import fr.ludos.game.TeamController;

import java.util.Set;
import java.util.HashSet;
import java.util.Random;

import javax.annotation.Nullable;

public final class ManhuntTeamController extends TeamController implements Listener {
	private ManhuntGame game;
	public Team hunterTeam;
	public Team preyTeam;


	public ManhuntTeamController(ManhuntGame game, @Nullable Set<Player> players, @Nullable Player prey) {
		super(game.getScoreboard());
		this.game = game;


		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

		hunterTeam = scoreboard.getTeam("Hunters");
		if (hunterTeam == null) {
			hunterTeam = scoreboard.registerNewTeam("Hunters");
			hunterTeam.setColor(ChatColor.RED);
			hunterTeam.setAllowFriendlyFire(false);
		}

		preyTeam = scoreboard.getTeam("Prey");
		if (preyTeam == null) {
			preyTeam = scoreboard.registerNewTeam("Prey");
			preyTeam.setColor(ChatColor.BLUE);
			preyTeam.setAllowFriendlyFire(false);
		}

		if (players == null) {
			players = new HashSet<Player>();
			players.addAll(Bukkit.getOnlinePlayers());
		}

		if (prey == null) {
			Player[] playersArray = players.toArray( new Player[players.size()] );
			prey = playersArray[ new Random().nextInt(players.size()) ];
		}
		players.remove(prey);



		for (Player hunter : players) {
			hunterTeam.addEntry(hunter.getName());
			hunter.setScoreboard(scoreboard);
			hunter.sendMessage("You are a Hunter.");
		}

		preyTeam.addEntry(prey.getName());
		prey.setScoreboard(scoreboard);
		prey.sendMessage("You are the Prey.");
	}

	@Override
	public void stop() {
		super.stop();

		HandlerList.unregisterAll(this);

		preyTeam.unregister();
		hunterTeam.unregister();
	}


	@Override
	protected Team[] getTeams() {
		return new Team[] {
			hunterTeam, preyTeam
		};
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (
			preyTeam.hasEntry(event.getEntity().getName())
		) {
			Bukkit.broadcastMessage("Prey" + player.getName() + "Slain!");
			preyTeam.removeEntry(player.getName());
		}

		if ( preyTeam.getSize() == 0 ) {
			Bukkit.broadcastMessage("All Prey Dead! End of Game!");
			Game.stopGame();
		}
	}

}