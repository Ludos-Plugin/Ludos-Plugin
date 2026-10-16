package fr.ludos.core.game.teamController;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.core.Utility;
import fr.ludos.core.group.Group;

/**
 * A static Utility class containing functions designed like "presets" for Team management.
 */
public class TeamControllerUtility {
	private TeamControllerUtility() {}

	public static Triple<Player, Set<Player>, Set<Player>> oneVersusMultiple(Group group, @Nullable OfflinePlayer selectedSolo, @Nullable Set<OfflinePlayer> selectedTeam) {
		Set<Player> allPlayers = group.getOnlinePlayers();
		Set<Player> teamPlayers = allPlayers;
		Set<Player> spectators;
		if (selectedTeam != null ) {
			if (! selectedTeam.isEmpty()) {
				teamPlayers = allPlayers.stream()
					.filter(p -> selectedTeam.contains(p))
					.collect(Collectors.toSet());

				if (teamPlayers.isEmpty()) {
					throw new IllegalArgumentException("No players available (Check if the configured players are online)");
				}
			}

			spectators = allPlayers.stream()
				.filter(p -> ! (selectedTeam.contains(p) || p != selectedSolo))
				.collect(Collectors.toSet());
		} else {
			spectators = Set.of();
		}

		Player solo = selectedSolo == null ? null : selectedSolo.getPlayer();
		if (solo == null) {
			solo = teamPlayers.iterator().next();
		}
		else if (! solo.isOnline()) {
			throw new IllegalArgumentException("Configured Prey is not online");
		}

		teamPlayers.remove(solo);

		return Triple.of(solo, teamPlayers, spectators);
	}

	public static Triple<Set<Player>, Set<Player>, Set<Player>> splitPlayersInTwoTeams(Group group, @Nullable Set<OfflinePlayer> team1Players, @Nullable Set<OfflinePlayer> team2Players) {
		Set<Player> online = group.getOnlinePlayers();

		Set<Player> finalPrimary;
		Set<Player> finalSecondary;
		Set<Player> spectators;

		if ((team1Players == null || team1Players.isEmpty()) && (team2Players == null || team2Players.isEmpty())) {
			List<? extends Collection<Player>> split = Utility.split(online, 2);
			finalPrimary = split.get(0).stream().collect(Collectors.toSet());
			finalSecondary = split.get(1).stream().collect(Collectors.toSet());
			spectators = Set.of();
		}
		else {
			finalPrimary = Utility.getOnline(team1Players).collect(Collectors.toCollection(HashSet::new));
			finalSecondary = Utility.getOnline(team2Players).collect(Collectors.toCollection(HashSet::new));

			if (team1Players == null || team1Players.isEmpty()) {
				finalPrimary = new HashSet<>(online);
				finalPrimary.removeAll(finalSecondary);
			} else {
				finalSecondary = new HashSet<>(online);
				finalSecondary.removeAll(finalPrimary);
			}

			finalSecondary.removeAll(finalPrimary);
			spectators = Utility.getOnline(team1Players).collect(Collectors.toCollection(HashSet::new));
			spectators.removeAll(finalPrimary);
			spectators.removeAll(finalSecondary);
		}

		if (finalPrimary.isEmpty() || finalSecondary.isEmpty()) {
			throw new IllegalStateException("Cannot start Arena game with only one team");
		}

		return Triple.of(finalPrimary, finalSecondary, spectators);
	}

}
