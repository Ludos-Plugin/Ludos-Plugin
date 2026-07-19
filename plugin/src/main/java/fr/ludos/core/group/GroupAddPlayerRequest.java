package fr.ludos.core.group;

import org.bukkit.OfflinePlayer;

/**
 * A pending request to add a Player to a {@link Group}.
 */
public final class GroupAddPlayerRequest {
	private final OfflinePlayer player;
	public final OfflinePlayer getPlayer() {
		return player;
	}

	private final boolean fromLeader;
	public final boolean isFromLeader() {
		return fromLeader;
	}


	public GroupAddPlayerRequest(OfflinePlayer player, boolean isFromLeader) {
		super();
		this.player = player;
		this.fromLeader = isFromLeader;
	}
}