package fr.ludos.item.assassin;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.item.SpecialItem;
import fr.ludos.role.Role;
import fr.ludos.role.AssassinRole;
import fr.ludos.game.Game;


public class AssassinDagger extends SpecialItem {
    public AssassinDagger(ItemStack stack, Game game) {
        super(stack, game);
    }
    public AssassinDagger(Player owner, Game game) {
        super(new ItemStack(Material.IRON_SWORD), owner, game);
    }

    @Override
    public String getId() {
        return "assassinDagger";
    }

    @Override
    protected Component getName(){
        return Component.text("Dague d'Assassin")
            .decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getLore(){
        return new ArrayList<>(Arrays.asList(
            Component.text("Inflige des dégâts augmentés"),
            Component.text("Ralentit l'ennemi frappé")
        ));
    }


    public static class Events extends SpecialItem.Events<AssassinDagger> {
        public Events(Game game) {
            super(game);
        }

        @EventHandler
        public void onDaggerHit(EntityDamageByEntityEvent event) {
            if (! (event.getDamager() instanceof Player player)) return;
            if (! Role.isPlayerRole(player, AssassinRole.id)) return;

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (getItem(itemInHand, game) == null) return;

            // Dégâts augmentés
            event.setDamage(event.getDamage() * 1.5);

            // Ralentissement de l'ennemi
            if (event.getEntity() instanceof HumanEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 3, 1));
            }
        }

        @Override
        @Nullable
        protected AssassinDagger getItem(ItemStack stack, Game game) {
            try {
                AssassinDagger dagger = new AssassinDagger(stack, game);
                return dagger;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        @Override
        protected AssassinDagger createItem(Player owner, Game game) {
            return new AssassinDagger(owner, game);
        }
        @Override
        protected Boolean canPlayerHaveItem(HumanEntity owner) {
            return Role.isPlayerRole(owner, AssassinRole.id);
        }
    }
}