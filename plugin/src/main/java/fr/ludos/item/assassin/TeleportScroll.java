package fr.ludos.item.assassin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.scheduler.BukkitRunnable;

import fr.ludos.Ludos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.event.entity.EntityDamageEvent;

import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.AssassinRole;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntAreaOptions;
import fr.ludos.game.Game;
import fr.ludos.game.manhunt.ManhuntGame;
import fr.ludos.game.manhunt.ManhuntAreaOptions;


public class TeleportScroll extends SpecialItem {
    public TeleportScroll(ItemStack stack, Game game) {
        super(stack, game);
    }
    public TeleportScroll(Player owner, Game game) {
        super(new ItemStack(Material.PAPER), owner, game);
    }

    @Override
    public String getId() {
        return "teleportScroll";
    }

    @Override
    protected Component getName(){
        return Component.text("Parchemin de Téléportation")
            .decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getLore(){
        return new ArrayList<>(Arrays.asList(
            Component.text("Téléporte-toi aléatoirement"),
            Component.text("Cooldown : 30 secondes")
        ));
    }


    public static class Events extends SpecialItem.Events<TeleportScroll> {
        private static final int COOLDOWN = 20 * 30; // 30 secondes
        private static final int MAX_ATTEMPTS = 100;
        private static final int INVULNERABILITY_DURATION = 20; // 1 seconde
        private final Random random = new Random();

        public Events(Game game) {
            super(game);
        }

        @EventHandler
        public void onScrollUse(PlayerInteractEvent event) {
            if (!event.getAction().isRightClick()) return;

            Player player = event.getPlayer();
            if (! Role.isPlayerRole(player, AssassinRole.id)) return;

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (getItem(itemInHand, game) == null) return;

            if (player.getCooldown(Material.ENDER_PEARL) > 0) return;

            event.setCancelled(true);

            Location randomLocation = findSafeLocation(player.getWorld());

            if (randomLocation != null) {
                player.teleport(randomLocation);

                // Annuler les dégâts pendant 1 seconde
                player.setInvulnerable(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.setInvulnerable(false);
                    }
                }.runTaskLater(game.getPlugin(), INVULNERABILITY_DURATION);

                player.setCooldown(Material.ENDER_PEARL, COOLDOWN);

                if (itemInHand.getAmount() > 1) {
                    itemInHand.setAmount(itemInHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            }
        }

        private Location findSafeLocation(World world) {
            // Récupérer la taille de la map depuis le jeu Manhunt
            int mapSize = 250; // Valeur par défaut

            Game currentGame = Game.getCurrent();
            if (currentGame != null && currentGame instanceof ManhuntGame) {
                ManhuntGame manhuntGame = (ManhuntGame) currentGame;
                //mapSize = manhuntGame.ManhuntAreaOptions.getSize();
            }

            for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
                int x = random.nextInt(-mapSize, mapSize);
                int z = random.nextInt(-mapSize, mapSize);

                int highestY = world.getHighestBlockYAt(x, z);
                Block blockAtY = world.getBlockAt(x, highestY, z);

                if (blockAtY.getType() == Material.WATER) {
                    continue;
                }

                Block block1 = world.getBlockAt(x, highestY + 1, z);
                Block block2 = world.getBlockAt(x, highestY + 2, z);

                if (block1.getType() != Material.AIR || block2.getType() != Material.AIR) {
                    continue;
                }

                Location safeLocation = new Location(world, x + 0.5, highestY + 1.5, z + 0.5);
                return safeLocation;
            }

            return null;
        }

        @Override
        @Nullable
        protected TeleportScroll getItem(ItemStack stack, Game game) {
            try {
                TeleportScroll scroll = new TeleportScroll(stack, game);
                return scroll;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        @Override
        protected TeleportScroll createItem(Player owner, Game game) {
            return new TeleportScroll(owner, game);
        }
        @Override
        protected Boolean canPlayerHaveItem(HumanEntity owner) {
            return Role.isPlayerRole(owner, AssassinRole.id);
        }
    }
}