package fr.ludos.group;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class GroupEvents implements Listener {
	@EventHandler
	public void onLeaderDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Group group = Group.getGroupOfPlayer(player);
		if (group == null) return;

		if (! group.isLeader(player)) return;

		group.demoteLeader();
	}
}
