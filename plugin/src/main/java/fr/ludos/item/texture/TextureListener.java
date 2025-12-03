package fr.ludos.item.texture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.ludos.Ludos;

public final class TextureListener implements Listener {
    private final Ludos plugin;
    private final ConcurrentMap<Player, BukkitTask> tasks = new ConcurrentHashMap<>();
    
    public TextureListener(Ludos plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        schedule(e.getPlayer(), 20);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) schedule(p, 1);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent e) {
        schedule(e.getPlayer(), 1);
    }
    
    private void schedule(Player player, int delay) {
        var oldTask = tasks.remove(player);
        if (oldTask != null && !oldTask.isCancelled()) oldTask.cancel();
        
        tasks.put(player, plugin.getServer().getScheduler()
            .runTaskLater(plugin, () -> {
                TextureApplier.process(player);
                tasks.remove(player);
            }, delay));
    }
    
    public void shutdown() {
        tasks.values().stream()
             .filter(task -> !task.isCancelled())
             .forEach(BukkitTask::cancel);
        tasks.clear();
    }
}