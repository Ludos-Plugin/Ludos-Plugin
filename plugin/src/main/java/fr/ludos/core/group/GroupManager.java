package fr.ludos.core.group;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.ludos.core.Ludos;

public class GroupManager implements Listener {
	private final Ludos plugin;
	public GroupManager(Ludos plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLeaderDisconnect(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Group group = Group.getGroupOfPlayer(player);
		if (group == null) return;

		if (! group.isLeader(player)) return;

		group.demoteLeader();
	}
}