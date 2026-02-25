package fr.ludos.item.sheep;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.security.auth.callback.Callback;
import java.util.function.Function;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.Particle;
import org.bukkit.Color;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import fr.ludos.game.Game;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import fr.ludos.item.ItemUtilities;
import fr.ludos.Ludos;

public class Sheep implements org.bukkit.event.Listener {

    private final String name;
    private final String description;
    private final Material material;
    
    // Map des joueurs avec contrôles inversés (UUID -> timestamp de fin)
    private static final HashMap<UUID, Long> invertedPlayers = new HashMap<>();

    /**
     * Constructs a Sheep item with the specified name, description, and material.
     * @param name The name of the sheep item.
     * @param description The description of the sheep item.
     * @param material The material representing the sheep item.
     */
    public Sheep(String name, String description, Material material) {
        this.name = name;
        this.description = description;
        this.material = material;
    }

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

    private static final NamespacedKey SHEEP_OWNER =
        new NamespacedKey(JavaPlugin.getPlugin(Ludos.class), "sheep_owner");

    private void tagOwner(org.bukkit.entity.Sheep sheep, Player owner) {
        sheep.getPersistentDataContainer().set(
            SHEEP_OWNER,
            PersistentDataType.STRING,
            owner.getUniqueId().toString()
        );
    }

    private void untagOwner(org.bukkit.entity.Sheep sheep) {
        sheep.getPersistentDataContainer().remove(SHEEP_OWNER);
    }

    @FunctionalInterface
    public interface SheepAction {
        void run(Player player, PlayerInteractEvent event, ItemStack item);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        event.getDrops().clear();
    }

    @EventHandler
    public void onGetEntityDamage(EntityDamageEvent event) {
        Player player = event.getEntity() instanceof Player p ? p : null;
        if(player == null) return;

        String uuid = player.getUniqueId().toString();

        EntityDamageByEntityEvent damager = event instanceof EntityDamageByEntityEvent e ? e : null;
        if(damager == null) return;

        DamageCause damageCause = event.getCause();

        if(damager.getDamager().getType() != EntityType.SHEEP) return;

        org.bukkit.entity.Sheep sheep = damager.getDamager().getType() == EntityType.SHEEP ?
            (org.bukkit.entity.Sheep) damager.getDamager() : null;

        String owner = sheep.getPersistentDataContainer().get(SHEEP_OWNER, PersistentDataType.STRING);

        Boolean isOwner = owner != null && owner.equals(uuid);

        if(!isOwner) return;

        player.setNoDamageTicks(0);
        event.setDamage(0);
        event.setCancelled(true);
    }

    HashSet<Action> validActions = new HashSet<>(
        Arrays.asList(Action.RIGHT_CLICK_AIR, Action.LEFT_CLICK_AIR, Action.PHYSICAL)
    );

    List<SheepAction> functions = List.of(
        this::launchNukeSheep, this::launchFreezeSheep,
        this::launchMediumExplosionSheep, this::launchLavaSheep,
        this::launchLargeExplosionSheep, this::launchAnvilsSheep,
        this::launchWaterSheep, this::launchDirtSheep, this::launchBlindSheep,
        this:: launchPoisonSheep, this::launchTeleportSheep, this::launchHealSheep,
        this::launchAllAboardSheep, this::launchFireballSheep, 
        this::launchNauseatingSheep, this::launchMonsterSheep
    );

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if(player == null) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Integer isWheelItem = switch (item.getType()) {
            case BROWN_WOOL -> 1;
            case LIGHT_BLUE_WOOL -> 2;
            case ORANGE_WOOL -> 3;
            case YELLOW_WOOL -> 4;
            case RED_WOOL -> 5;
            case GRAY_WOOL -> 6;
            case BLUE_WOOL -> 7;
            case GREEN_WOOL -> 8; 
            case BLACK_WOOL -> 9;
            case MAGENTA_WOOL -> 10;
            case PURPLE_WOOL -> 11;
            case PINK_WOOL -> 12;
            case WHITE_WOOL -> 13;
            case CYAN_WOOL -> 14;
            case LIME_WOOL -> 15;
            case LIGHT_GRAY_WOOL -> 16;
            default -> 0;
        };

        if (isWheelItem == 0) return;
        
        if (!validActions.contains(event.getAction())) event.setCancelled(true);

        functions.get(isWheelItem - 1).run(player, event, item);
    }

    public Boolean eventLaunchSheepWool(PlayerInteractEvent event, ItemStack item) {
        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        return true;
    }

    public void launchNukeSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomName("jeb_");
        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 0, launcher);
    }

    public void launchFreezeSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.LIGHT_BLUE); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 1, launcher);
    }

    public void launchMediumExplosionSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.ORANGE); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 2, launcher);
    }

    public void launchLavaSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.YELLOW); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 3, launcher);
    }

    public void launchLargeExplosionSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.ORANGE); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 4, launcher);
    }

    public void launchAnvilsSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.GRAY); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 5, launcher);
    }

    
    public void launchWaterSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.BLUE); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 6, launcher);
    }

    public void launchDirtSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.GREEN); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 7, launcher);
    }

    public void launchBlindSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.BLACK); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 8, launcher);
    }

    public void launchPoisonSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.MAGENTA); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 9, launcher);
    }
    
    public void launchTeleportSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.PURPLE); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 10, launcher);
    }

    public void launchHealSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.PINK); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 11, launcher);
    }

    public void launchAllAboardSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.WHITE);
        sheep.setInvulnerable(true);
        
        // Le joueur monte sur le mouton
        sheep.addPassenger(launcher);
        
        Vector direction = launcher.getLocation().getDirection().multiply(2);
        sheep.setVelocity(direction);

        // Timer spécial pour le mouton volant avec passager
        startRidingSheepTimer(sheep, launcher);
    }  
    
    public void startRidingSheepTimer(org.bukkit.entity.Sheep sheep, Player rider) {
        new BukkitRunnable() {
            int flightTime = 0;
            final int maxFlightTime = 100; // 5 secondes

            @Override
            public void run() {
                if (sheep == null || sheep.isDead()) {
                    this.cancel();
                    return;
                }

                // Si le joueur n'est plus sur le mouton ou temps écoulé
                if (!sheep.getPassengers().contains(rider) || flightTime >= maxFlightTime) {
                    rider.leaveVehicle();
                    sheep.remove();
                    this.cancel();
                    return;
                }

                // Maintenir le mouton en vol et suivre la direction du regard du joueur
                Vector direction = rider.getLocation().getDirection().multiply(0.8);
                direction.setY(Math.max(direction.getY(), -0.1)); // Empêcher de tomber trop vite
                sheep.setVelocity(direction);
                
                sheep.getLocation().getWorld().playEffect(sheep.getLocation(), Effect.SMOKE, 0);
                flightTime++;
            }
        }.runTaskTimer(JavaPlugin.getPlugin(Ludos.class), 0L, 1L);
    }
    
    public void launchFireballSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.CYAN); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 13, launcher);
    }
    
    public void launchNauseatingSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {

        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.LIME); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 14, launcher);
    }

    public void launchMonsterSheep(Player launcher, @Nullable PlayerInteractEvent event, @Nullable ItemStack item) {
        World world = launcher.getWorld();
        Location location = launcher.getLocation();

        if (event != null && item != null) eventLaunchSheepWool(event, item);

        org.bukkit.entity.Sheep sheep = (org.bukkit.entity.Sheep) world.spawnEntity(location.add(0, 1.5, 0), EntityType.SHEEP);

        sheep.setCustomNameVisible(true);
        sheep.setAdult(); 
        sheep.setColor(DyeColor.LIGHT_GRAY); 
        
        Vector direction = launcher.getLocation().getDirection().multiply(3);
        sheep.setVelocity(direction);

        startSheepTimer(sheep, 15, launcher);
    }

    public void startSheepTimer(org.bukkit.entity.Sheep sheep, Integer option, Player owner) {
        tagOwner(sheep, owner);

        List<Runnable> effects = List.of(
            () -> sheep.getWorld().createExplosion(sheep.getLocation(), 70.0f, true, true, sheep),
            () -> transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.PACKED_ICE, Sound.BLOCK_GLASS_BREAK),
            () -> sheep.getWorld().createExplosion(sheep.getLocation(), 5.0f, true, true, sheep),
            () -> transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.LAVA, Sound.BLOCK_LAVA_EXTINGUISH),
            () -> sheep.getWorld().createExplosion(sheep.getLocation(), 10.0f, true, true, sheep),
            () -> spawnAnvils(sheep.getLocation(), 5),
            () -> transformToAnotherMaterial(sheep.getLocation(), 5, Material.AIR, Material.WATER, Sound.BLOCK_WATER_AMBIENT),
            () -> transformStructureToDirt(sheep.getLocation()),
            () -> createPotionEffectCloud(sheep.getLocation(), PotionEffectType.BLINDNESS, Particle.SMOKE_LARGE, Color.BLACK),
            () -> createPotionEffectCloud(sheep.getLocation(), PotionEffectType.POISON, Particle.SNEEZE, Color.LIME),
            () -> teleportOwnerToLocation(sheep.getLocation(), owner),
            () -> createPotionEffectCloud(sheep.getLocation(), PotionEffectType.REGENERATION, Particle.HEART, Color.FUCHSIA),
            () -> allAboardEffect(sheep.getLocation()),
            () -> fireballFromSky(sheep.getLocation()),
            () -> applyInvertedControls(sheep.getLocation()),
            () -> spawnMonsters(sheep.getLocation())
        );

        new BukkitRunnable() {
            int flightTime = 0; 

            @Override
            public void run() {

                if (sheep == null || sheep.isDead()) this.cancel();

                if (sheep.isOnGround() || sheep.getVelocity().length() < 0.1 || flightTime >= 100) {
                    effects.get(option).run();

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

        meta.displayName(Component.text(name, NamedTextColor.BLUE, TextDecoration.BOLD));
        meta.lore(List.of(Component.text(description, NamedTextColor.AQUA)));

        item.setItemMeta(meta);
        return item;
    }

    public void transformToAnotherMaterial(Location center, int radius, Material materialToReplace, 
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

    public void createPotionEffectCloud(Location loc, PotionEffectType effectType, Particle particle, Color color) {
        AreaEffectCloud cloud = (AreaEffectCloud) loc.getWorld().spawnEntity(loc, EntityType.AREA_EFFECT_CLOUD);
        
        cloud.setRadius(4.0f); // Rayon du nuage (ex: 4 blocs)
        cloud.setDuration(160); // Durée de présence du nuage au sol (en ticks, 160 = 8 secondes)
        cloud.setWaitTime(0); // Applique l'effet immédiatement
        
        PotionEffect effect = new PotionEffect(effectType, 60, 0);
        cloud.addCustomEffect(effect, true);
        
        cloud.setParticle(particle);
        cloud.setColor(color);
        
        // Optionnel : Un petit son de splash
        loc.getWorld().playSound(loc, Sound.ENTITY_SPLASH_POTION_BREAK, 1.0f, 0.5f);
    }

    public static Sheep fromJsonObject(JSONObject json) {
        return new Sheep(
            json.get("name").toString(),
            json.get("description").toString(),
            Material.valueOf(json.get("material").toString())
        );
    }

    public void spawnAnvils(Location center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.sqrt(x*x + z*z) <= radius) {
                    Block block = center.clone().add(x, 7, z).getBlock();
                        
                    if (block.getType() != material.ANVIL)
                        block.setType(Material.ANVIL);
                }
            }
        }
    }

    public void transformStructureToDirt(Location center) {
        HashSet<Block> visited = new HashSet<>();
        java.util.LinkedList<Block> toVisit = new java.util.LinkedList<>();
        
        Block startBlock = center.getBlock();
        
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

    public void teleportOwnerToLocation(Location center, Player owner) {
        Location safeLoc = center.clone();
        safeLoc.setY(center.getWorld().getHighestBlockYAt(center) + 1);
        safeLoc.setYaw(owner.getLocation().getYaw());
        safeLoc.setPitch(owner.getLocation().getPitch());
        
        owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
        owner.teleport(safeLoc);
        owner.getWorld().playSound(owner.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        owner.getWorld().spawnParticle(Particle.PORTAL, owner.getLocation(), 50);
    }

    public void allAboardEffect(Location center) {
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

    public void fireballFromSky(Location center) {
        World world = center.getWorld();
        Location spawnLoc = center.clone().add(0, 30, 0);
        
        org.bukkit.entity.Fireball fireball = world.spawn(spawnLoc, org.bukkit.entity.Fireball.class);
        fireball.setDirection(new Vector(0, -1, 0));
        fireball.setYield(3.0f);
        
        world.playSound(center, Sound.ENTITY_GHAST_WARN, 1.0f, 0.5f);
    }

    public void spawnMonsters(Location center) {
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

    public void applyInvertedControls(Location center) {
        double radius = 8.0;
        long duration = 8000; // 8 secondes d'effet
        long endTime = System.currentTimeMillis() + duration;
        
        List<Player> nearbyPlayers = center.getWorld().getPlayers().stream()
            .filter(p -> p.getLocation().distance(center) <= radius)
            .toList();
        
        for (Player player : nearbyPlayers) {
            invertedPlayers.put(player.getUniqueId(), endTime);
            // Effet CONFUSION (nausée) pour déformer l'écran - niveau 1, 160 ticks = 8 secondes
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 1, false, true));
            player.sendMessage(Component.text("Vos contrôles sont inversés!", NamedTextColor.RED));
            player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(0, 1, 0), 30);
        }
        
        center.getWorld().playSound(center, Sound.ENTITY_WITCH_AMBIENT, 1.0f, 0.5f);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!invertedPlayers.containsKey(playerId)) return;
        
        // Vérifier si l'effet est expiré
        if (System.currentTimeMillis() > invertedPlayers.get(playerId)) {
            invertedPlayers.remove(playerId);
            player.sendMessage(Component.text("Vos contrôles sont revenus à la normale!", NamedTextColor.GREEN));
            return;
        }
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Calculer le déplacement
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        
        // Si le joueur bouge, inverser le mouvement
        if (deltaX != 0 || deltaZ != 0) {
            Location inversedLoc = from.clone();
            inversedLoc.subtract(deltaX * 2, 0, deltaZ * 2);
            inversedLoc.setY(to.getY());
            inversedLoc.setYaw(to.getYaw());
            inversedLoc.setPitch(to.getPitch());
            
            event.setTo(inversedLoc);
        }
    }
    
    public static void clearInvertedPlayer(UUID playerId) {
        invertedPlayers.remove(playerId);
    }
}