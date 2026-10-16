package fr.ludos.core.group;

import org.bukkit.OfflinePlayer;

/**
 * A pending request to add a Player to a {@link Group}.
 * @param player The player that was requested to add to the group
 * @param isFromLeader Whether the request was emitted by the {@link Group} leader or not.
 */
public final record GroupAddPlayerRequest(OfflinePlayer player, boolean isFromLeader) {}