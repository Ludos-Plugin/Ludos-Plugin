package fr.ludos.game.lobbyController;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import fr.ludos.group.Group;

public enum LobbyWaitPlayersOption {
	online () {
		@Override
		public final Set<OfflinePlayer> getPlayers(Group group) {
			return group.getPlayers().stream()
				.map( offlinePlayer -> offlinePlayer.getPlayer() )
				.filter( player -> player != null )
				.collect(Collectors.toSet());
		}
	}, all () {
		@Override
		public final Set<OfflinePlayer> getPlayers(Group group) {
			return group.getPlayers().stream()
				.collect(Collectors.toSet());
		}
	};

	public abstract Set<OfflinePlayer> getPlayers(Group group);

	public static String getUsage() {
		StringBuilder sb = new StringBuilder();

		sb.append("<");
		sb.append(
			Arrays.stream(LobbyWaitPlayersOption.values()).map(LobbyWaitPlayersOption::toString)
				.collect(Collectors.joining( " | "))
		);
		sb.append(">");

		return sb.toString();
	}
}