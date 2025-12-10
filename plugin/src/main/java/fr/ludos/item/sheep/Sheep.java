package fr.ludos.item.sheep;

import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.function.TriFunction;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin; 
import org.bukkit.event.entity.EntityDeathEvent;

import fr.ludos.game.Game;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import fr.ludos.item.ItemUtilities;
import fr.ludos.Ludos;

public class Sheep implements org.bukkit.event.Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    public static ItemStack createNukeSheepWool() {
        ItemStack sheepItem = new ItemStack(Material.BROWN_WOOL, 4);
        ItemMeta meta = sheepItem.getItemMeta();
        meta.displayName(Component.text("Nuke Sheep", NamedTextColor.GREEN, TextDecoration.BOLD));
        meta.lore(List.of(Component.text("Throw this sheep to create a nuke!", NamedTextColor.YELLOW)));
        sheepItem.setItemMeta(meta);
        return sheepItem;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        if (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
            if (item != null && item.getType() == Material.BROWN_WOOL) {
                event.setCancelled(true);

                item.setAmount(item.getAmount() - 1);

                launchNukeSheep(player);

            }
        }
    }

    public void launchNukeSheep(Player launcher) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);
        sheep.setCustomName("jeb_");
        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        // sheep.setColor(DyeColor.BROWN); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startNukeSheepTimer(sheep);
    }

    public void startNukeSheepTimer(org.bukkit.entity.Sheep sheep) {

        new BukkitRunnable() {
            int flightTime = 0; 

            @Override
            public void run() {
                if (sheep == null || sheep.isDead()) {
                    this.cancel();
                    return;
                }

                if (sheep.isOnGround() || sheep.getVelocity().length() < 0.1) {
            
                    sheep.getWorld().createExplosion(sheep.getLocation(), 3.0f);
                    sheep.remove();
                    this.cancel();
                    return;
                }

                if (flightTime >= 100) {
                    sheep.getWorld().createExplosion(sheep.getLocation(), 3.0f);
                    sheep.remove();
                    this.cancel();
                    return;
                }

                sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
                flightTime++;
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
    }
}


