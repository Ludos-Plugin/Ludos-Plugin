package fr.ludos.game;

import org.bukkit.entity.HumanEntity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public abstract class TeamController {

	protected Scoreboard scoreboard;


	public TeamController(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	public void stop() {}
	

	protected abstract Team[] getTeams();

	public boolean areAllies(HumanEntity player1, HumanEntity player2) {
		return scoreboard.getEntryTeam(player1.getName()) == scoreboard.getEntryTeam(player1.getName());
	}
}
