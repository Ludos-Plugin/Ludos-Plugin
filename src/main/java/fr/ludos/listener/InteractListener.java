package fr.ludos.listener;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import fr.ludos.Main;
import fr.ludos.monster.ZombieBomber;

/**
 * InteractListener class handles player interactions and entity damage events in a Bukkit/Spigot server.
 * It is responsible for spawning a special zombie entity, known as "Bomber," when a player interacts
 * with a specific item, and it cancels damage caused by explosions to Bomber zombies.
 * <br><br>
 * Features:
 * - Spawns Bomber zombies upon player interaction with a specific item.
 * - Cancels damage caused by explosions to Bomber zombies.
 * - Updates the custom name of living entities.
 * <br><br>
 * Usage:
 * - Instantiate InteractListener with the Main plugin instance.
 * - Register this listener with the Bukkit event manager.
 * <br><br>
 * Example:
 * Bukkit.getPluginManager().registerEvents(new InteractListener(plugin), plugin);
 * <br><br>
 * @param plugin The Main plugin instance for event handling.
 * @author feur25
 * @version 1.0
 * @see org.bukkit.ChatColor
 * @see org.bukkit.GameMode
 * @see org.bukkit.Location
 * @see org.bukkit.attribute.Attribute
 * @see org.bukkit.entity.EntityType
 * @see org.bukkit.entity.LivingEntity
 * @see org.bukkit.event.EventHandler
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.block.Action
 * @see org.bukkit.event.entity.EntityDamageEvent
 * @see org.bukkit.event.player.PlayerInteractEvent
 * @see org.bukkit.inventory.EquipmentSlot
 * @see fr.ludos.Main
 * @see fr.ludos.monster.ZombieBomber
 */

public class InteractListener implements Listener {

    private final Main plugin;

    /**
     * Constructs a new InteractListener instance.
     *
     * @param plugin The Main plugin instance for event handling.
     */

    public InteractListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player interactions, spawning Bomber zombies, and canceling item usage.
     *
     * @param event The PlayerInteractEvent triggered when a player interacts with the environment.
     */

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getHand() != null && event.getHand() == EquipmentSlot.HAND) {
                if (event.getItem() != null && event.getItem().getItemMeta() != null
                        && event.getItem().getItemMeta().getLore() != null
                        && event.getItem().getItemMeta().getLore().contains("Spawns a bomber zombie, explodes if you near")) {
                    Location spawnLocation;
                    if (event.getClickedBlock().isPassable()) {
                        spawnLocation = event.getClickedBlock().getLocation().add(0.5, 0, 0.5);
                    } else {
                        spawnLocation = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
                    }
                    new ZombieBomber(spawnLocation, event.getPlayer(), plugin);
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles entity damage events, canceling damage caused by explosions to Bomber zombies
     * and updating the custom name of living entities.
     *
     * @param event The EntityDamageEvent triggered when an entity takes damage.
     */

     @EventHandler
     public void onDamage(EntityDamageEvent event) {
         if (event.getEntity() instanceof LivingEntity) {
             LivingEntity livingEntity = (LivingEntity) event.getEntity();
     
             // Check if the entity is a Bomber zombie
             if (livingEntity.getType() == EntityType.ZOMBIE && livingEntity.getCustomName() != null &&
                     livingEntity.getCustomName().startsWith("Bomber")) {
                 if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                     event.setCancelled(true);
                 }
     
                 double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
     
                 int currentHealth = (int) livingEntity.getHealth();
                 livingEntity.setCustomName("Bomber" + ChatColor.RED + currentHealth + " / " + (int) maxHealth + " ❤︎");
             }
         }
     }
}