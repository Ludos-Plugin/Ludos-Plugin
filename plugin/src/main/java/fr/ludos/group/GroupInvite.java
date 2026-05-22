package fr.ludos.group;

import org.bukkit.OfflinePlayer;

public final class GroupInvite {
	private final OfflinePlayer player;
	public final OfflinePlayer getPlayer() {
		return player;
	}

	private final boolean fromLeader;
	public final boolean isFromLeader() {
		return fromLeader;
	}


	public GroupInvite(OfflinePlayer player, boolean isFromLeader) {
		super();
		this.player = player;
		this.fromLeader = isFromLeader;
	}
}