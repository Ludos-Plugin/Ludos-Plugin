// SoulVial.java

package fr.ludos.item;


import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

/**
 * SoulVial is a class that represents a special item, "Soul Vial," in Minecraft.
 * This item allows the necromancer player to collect souls from defeated monsters.
 * The Soul Vial changes color based on the number of souls collected and displays the soul count.
 * Souls can be used for various in-game mechanics or abilities.
 * <br><br>
 * Features:
 * <br><br>
 * - Provides a method to get the Soul Vial item for a specific player.
 * <br><br>
 * - Automatically increases the soul count when the player kills a monster.
 * <br><br>
 * - Bottle interaction, which allow to choose any effect, this enchantment increase in furthermore
 * <br><br>
 * Usage:
 * <br><br>
 * - Call getSoulVial(player) to obtain the Soul Vial ItemStack for a given player.
 * <br><br>
 * - Souls are automatically added when the player kills a monster, triggering the onEntityDeath event.
 * <br><br>
 * - The Soul Vial's appearance reflects the soul count, and lore displays the current soul count.
 * <br><br>
 * - Call openEffectSelectionMenu (player), open a new menu which allows you to choose a potion effect based on your soul counter
 * <br><br>
 * Example:
 * <br><br>
 * <pre>{@code
 * SoulVial.getSoulVial(player);
 * }</pre> --> Returns the Soul Vial ItemStack for the specified player.
 * <br><br>
 * @author feur25
 * @version 1.0
 * @see org.bukkit.entity.Player
 * @see org.bukkit.inventory.ItemStack
 * @see org.bukkit.event.Listener
 * @see org.bukkit.event.entity.EntityDeathEvent
 * @see org.bukkit.Color
 * @see org.bukkit.Material
 * @see org.bukkit.entity.EntityType
 * @see org.bukkit.event.EventHandler
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see org.bukkit.inventory.meta.LeatherArmorMeta
 * @see net.md_5.bungee.api.ChatColor
 * @see java.util.HashMap
 * @see java.util.Map
 */


public class SoulVial implements Listener {

    private static final String SOUL_VIAL_NAME = "Soul Vial";
    private static final int MAX_SOULS = 100;

    private static final Map<Player, Integer> soulCounts = new HashMap<>();

    /**
     * Gets the Soul Vial item for the specified player.
     *
     * @param player The player for whom the Soul Vial is obtained.
     * @return The Soul Vial ItemStack.
     */

    public static ItemStack getSoulVial(Player player) {
        ItemStack soulVial = new ItemStack(Material.GLASS_BOTTLE);

        soulCounts.putIfAbsent(player, 0);

        int soulCount = soulCounts.get(player);

        ItemMeta meta = soulVial.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + SOUL_VIAL_NAME);

        if (soulCount > 0) {
            int red = (int) ((double) soulCount / MAX_SOULS * 255);
            int green = 255 - red;
            int blue = 0;

            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
        }

        meta.setLore(java.util.Collections.singletonList("Souls: " + ChatColor.DARK_RED + soulCount));

        soulVial.setItemMeta(meta);

        return soulVial;
    }

    /**
     * Adds a specified number of souls to the player's soul count.
     *
     * @param player The player for whom souls are added.
     * @param souls  The number of souls to add.
     */

    public static void addSoul(Player player, int souls) {
        soulCounts.put(player, soulCounts.getOrDefault(player, 0) + souls);
    }

    /**
     * Handles the onEntityDeath event, adding souls to the player's count based on the type of monster killed.
     * Additionally, allows players to choose effects based on their soul count.
     *
     * @param event The EntityDeathEvent triggered when a living entity dies.
     */

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            int soulsToAdd = determineSoulsToAdd(event.getEntityType());
            addSoul(player, soulsToAdd);
            player.updateInventory();

            if (soulCounts.get(player) % 50 == 0) {
                player.sendMessage("You have reached a milestone of " + soulCounts.get(player) + " souls!");
                openEffectSelectionMenu(player);
            }
        }
    }

    /**
     * Opens a menu for the player to choose effects based on their soul count.
     *
     * @param player The player who will choose the effect.
     */

    private void openEffectSelectionMenu(Player player) {
        Inventory menu = Bukkit.createInventory(player, 9, "Soul Vial Effects");

        addPotionItem(menu, Material.POTION, "Speed", PotionEffectType.SPEED);
        addPotionItem(menu, Material.POTION, "Strength", PotionEffectType.INCREASE_DAMAGE);

        player.openInventory(menu);
    }

    /**
     * Adds a potion item to the menu.
     *
     * @param inventory   The inventory to add the item to.
     * @param potionType  The material representing the potion item.
     * @param displayName The display name of the potion item.
     * @param effectType  The PotionEffectType associated with the potion item.
     */

    private void addPotionItem(Inventory inventory, Material potionType, String displayName, PotionEffectType effectType) {
        ItemStack potionItem = new ItemStack(potionType);
        ItemMeta meta = potionItem.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + displayName);
        potionItem.setItemMeta(meta);

        inventory.addItem(potionItem);

        inventory.setItem(inventory.getSize() - 1, potionItem);
    }

    /** 
     * Handles inventory click events.
     *
     * @param event The InventoryClickEvent triggered when a player clicks in the inventory.
     */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() == Material.POTION) {
            PotionMeta potionMeta = (PotionMeta) clickedItem.getItemMeta();
            PotionData potionData = potionMeta.getBasePotionData();
            PotionEffectType effectType = potionData.getType().getEffectType();


            int souls = soulCounts.getOrDefault(player, 0);
            int level = souls / 50 + 1; 

            player.addPotionEffect(new PotionEffect(effectType, 20 * 60 * level, level - 1));

            player.sendMessage("You have chosen the effect: " + effectType.getName() + " Level " + level);
            player.closeInventory();
        }
    }

    /**
     * Determines the number of souls to add based on the type of monster killed.
     *
     * @param entityType The EntityType of the killed monster.
     * @return The number of souls to add.
     */

    private int determineSoulsToAdd(EntityType entityType) {
        switch (entityType) {
            case ZOMBIE:
                return 1;
            case SKELETON:
                return 2;
            case SPIDER:
                return 3;
            case ENDERMAN:
                return 4;
            default:
                return 1;
        }
    }
}
