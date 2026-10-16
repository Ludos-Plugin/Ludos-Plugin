package fr.ludos.core;

import java.util.Collection;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

import fr.ludos.core.command.ludos.config.group.GroupConfigMap;
import fr.ludos.core.group.Group;
import fr.ludos.core.lobby.LobbyWaitPlayersOption;

/**
 * Provides players from a particular source.
 */
public abstract sealed class PlayerList {

	/**
	 * Provides the online players belonging to a group.
	 */
	public static final class GroupPlayers extends PlayerList {
		private final Group group;

		public GroupPlayers(Group group) {
			this.group = group;
		}

		@Override
		public Collection<? extends OfflinePlayer> getPlayers() {
			return group.getOnlinePlayers();
		}
	}

	/**
	 * Provides the players selected by a group's lobby wait-player option.
	 */
	public static final class GroupWaitPlayers extends PlayerList {
		private final Group group;

		public GroupWaitPlayers(Group group) {
			this.group = group;
		}

		@Override
		public Collection<? extends OfflinePlayer> getPlayers() {
			LobbyWaitPlayersOption option = GroupConfigMap.WAIT_PLAYERS.getGroupConfig(group);
			return option.getPlayers(group);
		}
	}

	/**
	 * Provides all players currently online on a server.
	 */
	public static final class ServerPlayers extends PlayerList {
		private final Server server;

		public ServerPlayers(Server server) {
			this.server = server;
		}

		@Override
		public Collection<? extends OfflinePlayer> getPlayers() {
			return server.getOnlinePlayers();
		}
	}

	/**
	 * Provides a caller-supplied collection of players.
	 */
	public static final class ArbitraryPlayers extends PlayerList {
		private final Collection<OfflinePlayer> players;

		public ArbitraryPlayers(Collection<OfflinePlayer> players) {
			this.players = players;
		}

		@Override
		public Collection<? extends OfflinePlayer> getPlayers() {
			return players;
		}
	}

	public abstract Collection<? extends OfflinePlayer> getPlayers();
}
