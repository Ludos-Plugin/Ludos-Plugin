package fr.ludos.item.sheep;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Material;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.Effect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import fr.ludos.Ludos;

/**
 * Abstract base class for all sheep types in the Sheepwars game.
 * Provides common functionality and utility methods for sheep projectiles.
 */
public abstract class AbstractSheep implements org.bukkit.event.Listener {

    private final String name;
    private final String description;
    private final Material material;

    // Map of players with inverted controls (UUID -> end timestamp)
    private static final HashMap<UUID, Long> invertedPlayers = new HashMap<>();

    private static final NamespacedKey SHEEP_OWNER =
        new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), "sheep_owner");

    /**
     * Constructs an AbstractSheep with the specified name, description, and material.
     * @param name The name of the sheep item.
     * @param description The description of the sheep item.
     * @param material The material representing the sheep item.
     */
    public AbstractSheep(String name, String description, Material material) {
        this.name = name;
        this.description = description;
        this.material = material;
    }

    /**
     * Abstract method that each concrete sheep must implement.
     * Defines the behavior when the sheep is launched by a player.
     *
     * @param launcher The player launching the sheep
     * @param event The player interact event (nullable)
     * @param item The wool item stack being used (nullable)
     */
    public abstract void launch(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item);

    /**
     * Gets the name of this sheep.
     * @return The sheep name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of this sheep.
     * @return The sheep description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the material representing this sheep.
     * @return The wool material.
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Tags a sheep entity with its owner.
     * @param sheep The sheep entity to tag
     * @param owner The player who owns the sheep
     */
    protected void tagOwner(org.bukkit.entity.Sheep sheep, Player owner) {
        sheep.getPersistentDataContainer().set(
            SHEEP_OWNER,
            PersistentDataType.STRING,
            owner.getUniqueId().toString()
        );
    }

    /**
     * Removes the owner tag from a sheep entity.
     * @param sheep The sheep entity to untag
     */
    protected void untagOwner(org.bukkit.entity.Sheep sheep) {
        sheep.getPersistentDataContainer().remove(SHEEP_OWNER);
    }

    /**
     * Consumes the wool item and cancels the event.
     * Call this when a sheep is successfully launched.
     *
     * @param event The player interact event
     * @param item The wool item stack
     * @return Always returns true
     */
    protected Boolean eventLaunchSheepWool(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);
        return true;
    }

    /**
     * Spawns and launches a sheep with the specified color.
     * Simple version that uses default values (no custom name, velocity multiplier of 3).
     *
     * @param launcher The player launching the sheep
     * @param event The player interact event (nullable)
     * @param item The wool item stack (nullable)
     * @param color The dye color for the sheep
     * @return The spawned sheep entity
     */
    protected org.bukkit.entity.Sheep spawnAndLaunchSheep(
            Player launcher,
            @Nullable PlayerInteractEvent event,
            @Nullable ItemStack item,
            DyeColor color) {
        return spawnAndLaunchSheep(launcher, event, item, color, null, 3);
    }

    /**
     * Template method that handles the common sheep spawning and launching logic.
     * This is the complete version with full customization options for all sheep types.
     *
     * @param launcher The player launching the sheep
     * @param event The player interact event (nullable)
     * @param item The wool item stack (nullable)
     * @param color The dye color for the sheep (nullable - if null, no color is set)
     * @param customName The custom name for the sheep (nullable - e.g., "jeb_" for rainbow effect)
     * @param velocityMultiplier The velocity multiplier for launching (typically 2 or 3)
     * @return The spawned sheep entity
     */
    protected org.bukkit.entity.Sheep spawnAndLaunchSheep(
            Player launcher,
            @Nullable PlayerInteractEvent event,
            @Nullable ItemStack item,
            @Nullable org.bukkit.DyeColor color,
            @Nullable String customName,
            int velocityMultiplier) {

        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        // Consume the wool item
        if (event != null && item != null) {
            eventLaunchSheepWool(event, item);
        }

        // Spawn the sheep entity
        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep)
            world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        // Configure sheep appearance
        sheep.setCustomNameVisible(true);
        sheep.setAdult();

        if (customName != null) {
            sheep.setCustomName(customName);
        }

        if (color != null) {
            sheep.setColor(color);
        }

        // Launch the sheep with configurable velocity
        Vector direction = launcher.getLocation().getDirection().multiply(velocityMultiplier);
        sheep.setVelocity(direction);

        return sheep;
    }

    /**
     * Starts a timer for a riding sheep that follows the player's view direction.
     * Used specifically for AllAboardSheep.
     *
     * @param sheep The sheep entity being ridden
     * @param rider The player riding the sheep
     */
    protected void startRidingSheepTimer(org.bukkit.entity.Sheep sheep, Player rider) {
        new BukkitRunnable() {
            int flightTime = 0;
            final int maxFlightTime = 100; // 5 seconds

            @Override
            public void run() {
                if (sheep == null || sheep.isDead()) {
                    this.cancel();
                    return;
                }

                // If player is no longer riding or time is up
                if (!sheep.getPassengers().contains(rider) || flightTime >= maxFlightTime) {
                    rider.leaveVehicle();
                    sheep.remove();
                    this.cancel();
                    return;
                }

                // Keep sheep flying and follow player's view direction
                Vector direction = rider.getLocation().getDirection().multiply(0.8);
                direction.setY(Math.max(direction.getY(), -0.1)); // Prevent falling too fast
                sheep.setVelocity(direction);

                sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
                flightTime++;
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
    }

    /**
     * Starts a timer for a sheep projectile that triggers an effect on landing.
     *
     * @param sheep The sheep entity
     * @param effectAction The effect to trigger when the sheep lands (Runnable)
     * @param owner The player who launched the sheep
     */
    protected void startSheepTimer(org.bukkit.entity.Sheep sheep, Runnable effectAction, Player owner) {
        tagOwner(sheep, owner);

        new BukkitRunnable() {
            int flightTime = 0;

            @Override
            public void run() {
                if (sheep == null || sheep.isDead()) {
                    this.cancel();
                    return;
                }

                if (sheep.isOnGround() || sheep.getVelocity().length() < 0.1 || flightTime >= 100) {
                    effectAction.run();  // Execute the custom effect

                    untagOwner(sheep);
                    sheep.remove();

                    this.cancel();
                }

                sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
                flightTime++;
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
    }

    /**
     * Creates an ItemStack representing the sheep item.
     * @param amount The quantity of the item stack.
     * @return The created ItemStack.
     */
    public ItemStack createSheepItem(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name, NamedTextColor.BLUE)
            .decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true));
        meta.lore(List.of(Component.text(description, NamedTextColor.AQUA)));

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Transforms blocks in a sphere from one material to another.
     *
     * @param center The center location of the sphere
     * @param radius The radius of the sphere
     * @param materialToReplace The material that should be replaced (e.g., AIR)
     * @param materialToSet The material to set blocks to
     * @param sound The sound to play after transformation
     */
    protected void transformToAnotherMaterial(Location center, int radius, Material materialToReplace,
        Material materialToSet, Sound sound) {

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (Math.sqrt(x*x + y*y + z*z) <= radius) {
                        Block block = center.clone().add(x, y, z).getBlock();

                        if (block.getType() != materialToReplace)
                            block.setType(materialToSet);
                    }
                }
            }
        }
        center.getWorld().playSound(center, sound, 1.0f, 1.0f);
    }

    /**
     * Creates a potion effect cloud at the specified location.
     *
     * @param loc The location to spawn the cloud
     * @param effectType The potion effect type
     * @param particle The particle effect
     * @param color The color of the cloud
     */
    protected void createPotionEffectCloud(Location loc, PotionEffectType effectType, Particle particle, Color color, int amplifier) {
        AreaEffectCloud cloud = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);

        cloud.setRadius(4.0f);
        cloud.setDuration(160); // Duration of presence (in ticks, 160 = 8 seconds)
        cloud.setWaitTime(0); // Apply effect immediately

        PotionEffect effect = new PotionEffect(effectType, 60, amplifier);
        cloud.addCustomEffect(effect, true);

        cloud.setParticle(particle);
        cloud.setColor(color);

        loc.getWorld().playSound(loc, Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 0.5f);
    }

    /**
     * Spawns a grid of anvils above the target location.
     *
     * @param center The center location
     * @param radius The radius of the anvil grid
     */
    protected void spawnAnvils(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.sqrt(x*x + z*z) <= radius) {
                    Block block = center.clone().add(x, 7, z).getBlock();

                    if (block.getType() != Material.ANVIL)
                        block.setType(Material.ANVIL);
                }
            }
        }
    }

    /**
     * Transforms connected structure blocks to dirt using flood fill algorithm.
     *
     * @param center The center location to start the transformation
     */
    protected void transformStructureToDirt(Location center) {
        HashSet<Block> visited = new HashSet<>();
        java.util.LinkedList<Block> toVisit = new java.util.LinkedList<>();

        // Start with blocks in a 5x5x5 area around center
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block b = center.clone().add(x, y, z).getBlock();
                    if (b.getType() != Material.AIR && b.getType() != Material.CAVE_AIR) {
                        toVisit.add(b);
                    }
                }
            }
        }

        int maxBlocks = 10000;
        int blocksTransformed = 0;

        BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        while (!toVisit.isEmpty() && blocksTransformed < maxBlocks) {
            Block current = toVisit.poll();

            if (visited.contains(current)) continue;
            if (current.getType() == Material.AIR || current.getType() == Material.CAVE_AIR) continue;
            if (current.getLocation().distance(center) > 100) continue;

            visited.add(current);
            current.setType(Material.DIRT);
            blocksTransformed++;

            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (!visited.contains(neighbor) && neighbor.getType() != Material.AIR && neighbor.getType() != Material.CAVE_AIR) {
                    toVisit.add(neighbor);
                }
            }
        }

        center.getWorld().playSound(center, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.5f);
    }

    /**
     * Teleports the owner to the target location safely.
     *
     * @param center The target location
     * @param owner The player to teleport
     */
    protected void teleportOwnerToLocation(Location center, Player owner) {
        Location safeLoc = center.clone();
        safeLoc.setY(center.getWorld().getHighestBlockYAt(center) + 1);
        safeLoc.setYaw(owner.getLocation().getYaw());
        safeLoc.setPitch(owner.getLocation().getPitch());

        owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
        owner.teleport(safeLoc);
        owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
    }

    /**
     * Creates a tower of entities by making them ride each other.
     *
     * @param center The center location to search for entities
     */
    protected void allAboardEffect(Location center) {
        double radius = 6.0;
        List<org.bukkit.entity.Entity> nearbyEntities = center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
            .filter(e -> e instanceof org.bukkit.entity.LivingEntity && !(e instanceof org.bukkit.entity.ArmorStand))
            .toList();

        org.bukkit.entity.Entity previous = null;
        for (org.bukkit.entity.Entity entity : nearbyEntities) {
            if (previous != null) {
                entity.addPassenger(previous);
            }
            previous = entity;
        }
        center.getWorld().playSound(center, Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
    }

    /**
     * Spawns a fireball that falls from the sky.
     *
     * @param center The target location
     */
    protected void fireballFromSky(Location center) {
        World world = center.getWorld();
        Location spawnLoc = center.clone().add(0, 30, 0);

        org.bukkit.entity.Fireball fireball = world.spawn(spawnLoc, org.bukkit.entity.Fireball.class);
        fireball.setDirection(new Vector(0, -1, 0));
        fireball.setYield(3.0f);

        world.playSound(center, Sound.ENTITY_GHAST_WARN, 1.0f, 0.5f);
    }

    /**
     * Spawns hostile monsters at the target location.
     *
     * @param center The center location to spawn monsters
     */
    protected void spawnMonsters(Location center) {
        World world = center.getWorld();

        // Spawn 2 zombies with helmets
        for (int i = 0; i < 2; i++) {
            double offsetX = (Math.random() - 0.5) * 4;
            double offsetZ = (Math.random() - 0.5) * 4;
            Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);
            org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
            zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
        }

        // Spawn 1 skeleton with helmet
        double offsetX = (Math.random() - 0.5) * 4;
        double offsetZ = (Math.random() - 0.5) * 4;
        Location spawnLoc = center.clone().add(offsetX, 1, offsetZ);
        org.bukkit.entity.Skeleton skeleton = (org.bukkit.entity.Skeleton) world.spawnEntity(spawnLoc, EntityType.SKELETON);
        skeleton.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        // Spawn 1 spider
        offsetX = (Math.random() - 0.5) * 4;
        offsetZ = (Math.random() - 0.5) * 4;
        spawnLoc = center.clone().add(offsetX, 1, offsetZ);
        world.spawnEntity(spawnLoc, EntityType.SPIDER);

        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
        world.spawnParticle(Particle.SMOKE_LARGE, center, 30);
    }

    /**
     * Applies inverted controls to nearby players.
     *
     * @param center The center location
     */
    protected void applyInvertedControls(Location center) {
        double radius = 8.0;
        long duration = 8000; // 8 seconds effect
        long endTime = System.currentTimeMillis() + duration;

        List<Player> nearbyPlayers = center.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distance(center) <= radius)
            .toList();

        for (Player player : nearbyPlayers) {
            invertedPlayers.put(player.getUniqueId(), endTime);
            // CONFUSION effect (nausea) to distort the screen - level 1, 160 ticks = 8 seconds
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 1, false, true));
            player.sendMessage(Component.text("Vos contrôles sont inversés!", NamedTextColor.RED));
            player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 1, 0), 30);
        }

        center.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.5f);
    }

    /**
     * Clears inverted controls for a specific player.
     * @param playerId The UUID of the player
     */
    public static void clearInvertedPlayer(UUID playerId) {
        invertedPlayers.remove(playerId);
    }

    // ==================== Event Handlers ====================

    /**
     * Prevents sheep entities from dropping items when they die.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    /**
     * Prevents the owner from taking damage from their own sheep.
     */
    @EventHandler
    public void onGetEntityDamage(EntityDamageEvent event) {
        Player player = event.getEntity() instanceof Player p ? p : null;
        if(player == null) return;

        String uuid = player.getUniqueId().toString();

        EntityDamageByEntityEvent damager = event instanceof EntityDamageByEntityEvent e ? e : null;
        if(damager == null) return;

        if(damager.getDamager().getType() != EntityType.SHEEP) return;

        org.bukkit.entity.Sheep sheep = damager.getDamager().getType() == EntityType.SHEEP ?
            (org.bukkit.entity.Sheep) damager.getDamager() : null;

        String owner = sheep.getPersistentDataContainer().get(SHEEP_OWNER, PersistentDataType.STRING);

        Boolean isOwner = owner != null && owner.equals(uuid);

        if (isOwner) {
            player.setNoDamageTicks(0);
            event.setDamage(0);
            event.setCancelled(true);
            return;
        }

        // Reduce all other sheep damage to keep the game balanced.
        event.setDamage(event.getDamage() * 0.25);
    }

    /**
     * Handles inverted controls movement for affected players.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!invertedPlayers.containsKey(playerId)) return;

        // Check if the effect has expired
        if (System.currentTimeMillis() > invertedPlayers.get(playerId)) {
            invertedPlayers.remove(playerId);
            player.sendMessage(Component.text("Vos contrôles sont revenus à la normale!", NamedTextColor.GREEN));
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;

        // Calculate movement
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();

        // If player moves, invert the movement
        if (deltaX != 0 || deltaZ != 0) {
            Location inversedLoc = from.clone();
            inversedLoc.subtract(deltaX * 2, 0, deltaZ * 2);
            inversedLoc.setY(to.getY());
            inversedLoc.setYaw(to.getYaw());
            inversedLoc.setPitch(to.getPitch());

            event.setTo(inversedLoc);
        }
    }
}
