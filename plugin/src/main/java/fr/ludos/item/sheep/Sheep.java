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

import fr.ludos.game.Game;
import fr.ludos.role.BurrowerRole;
import fr.ludos.role.Role;
import fr.ludos.item.ItemUtilities;

public class Sheep implements org.bukkit.event.Listener {

    @EventHandler 
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Component.text("Bienvenue sur le serveur Ludos!", NamedTextColor.AQUA, TextDecoration.BOLD));
        ItemStack sheepItem = new ItemStack(Material.SHEEP_SPAWN_EGG, 1);
        ItemMeta meta = sheepItem.getItemMeta();
        meta.setDisplayName("Sac a foutre");

        // List<Component> lore = meta.lore();
        // lore.add(Component.text("Gros mouton sa mère, хорошо, хорошо, хорошо", NamedTextColor.YELLOW, TextDecoration.BOLD));
        // meta.lore(lore);

        sheepItem.setItemMeta(meta);
        
        if (!player.getInventory().contains(sheepItem)) {
            player.getInventory().addItem(sheepItem);
        } 

    }

   

    // @Override
	// protected Component getName() {
	// 	return Component.text("хорошо", NamedTextColor.GOLD, TextDecoration.BOLD); 
	// }
}
//renommer l'objet

//event handler

