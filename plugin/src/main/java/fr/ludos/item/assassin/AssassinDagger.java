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
    public static final String ID = "assassin_dagger";

    public static @Nullable AssassinDagger fromItemStack(ItemStack stack, Game game) throws IllegalArgumentException {
		Player owner = SpecialItem.getSpecialItemOwner(stack, ID, game);
		if (owner == null) return null;

		return new AssassinDagger(stack, owner, game);
	}

	public static AssassinDagger createItem(Player owner, Game game) {
		AssassinDagger dagger = new AssassinDagger(new ItemStack(Material.IRON_SWORD), owner, game);
		dagger.initializeItem();

		return dagger;
	}

    public AssassinDagger(ItemStack stack, Player player, Game game) {
        super(stack, player, game);
    }

    @Override
    public String getId() {
        return ID;
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
            return AssassinDagger.fromItemStack(stack, game);
        }
        @Override
        protected AssassinDagger createItem(Player owner, Game game) {
            return AssassinDagger.createItem(owner, game);
        }
        @Override
        protected Boolean canPlayerHaveItem(HumanEntity owner) {
            return Role.isPlayerRole(owner, AssassinRole.id);
        }
    }
}