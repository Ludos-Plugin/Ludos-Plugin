package fr.ludos.role;

import java.util.LinkedHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.ludos.Ludos;
import fr.ludos.item.SpecialItem;
import fr.ludos.item.assassin.AssassinDagger;
import fr.ludos.item.assassin.AssassinBoots;
import fr.ludos.item.assassin.TeleportScroll;
import fr.ludos.game.Game;


public class AssassinRole extends Role {
    public static final String id = "assassin";

    private static final int INVISIBILITY_DURATION = 300;
    private static final int INVISIBILITY_TICK_THRESHOLD = 80;


    public AssassinRole(Builder builder, Game game) {
        super(builder, game);
    }

    @Override
    protected LinkedHashMap<String, SpecialItem.Events<?>> createItemEvents(Role.Builder builder, Game game) {
        switch (builder.getId()) {
            default:
                return new LinkedHashMap<>() {{
                    put("dagger", new AssassinDagger.Events(game));
                    put("boots", new AssassinBoots.Events(game));
                    put("teleport_scroll", new TeleportScroll.Events(game));
                }};
        }
    }

    @EventHandler
    public void onInvisibleDaggerHit(EntityDamageByEntityEvent event) {
        if (! (event.getDamager() instanceof Player player)) return;
        if (! Role.isPlayerRole(player, AssassinRole.id)) return;

        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;

        event.setDamage(event.getDamage() * 2.5);
    }


    public static class Builder extends Role.Builder {

        @Override
        public String getId() {
            return id;
        }

        public Builder(Ludos plugin) {
            super(plugin);
        }


        @Override
        public AssassinRole build(Game game) {
            return new AssassinRole(this, game);
        }

        @Override
        public TextComponent getDisplayName() {
            return Component.text("Assassin");
        }

        @Override
        public TextComponent getDescription() {
            return Component.text("Se camoufle pour surprendre ses ennemis et les éliminer");
        }
    }
}